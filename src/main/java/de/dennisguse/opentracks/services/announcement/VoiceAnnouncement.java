/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package de.dennisguse.opentracks.services.announcement;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Spannable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Locale;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.data.ContentProviderUtils;
import de.dennisguse.opentracks.data.TrackPointIterator;
import de.dennisguse.opentracks.data.models.Distance;
import de.dennisguse.opentracks.data.models.Track;
import de.dennisguse.opentracks.data.models.TrackPoint;
import de.dennisguse.opentracks.settings.PreferencesUtils;
import de.dennisguse.opentracks.stats.SensorStatistics;
import de.dennisguse.opentracks.stats.TrackStatistics;
import de.dennisguse.opentracks.ui.intervals.IntervalStatistics;

/**
 * This class will announce the user's {@link TrackStatistics}.
 *
 * @author Sandor Dornbush
 */
public class VoiceAnnouncement {

    public static final int AUDIO_STREAM = TextToSpeech.Engine.DEFAULT_STREAM;

    private static final String TAG = VoiceAnnouncement.class.getSimpleName();

    private final Context context;

    private final AudioManager audioManager;

    private final ContentProviderUtils contentProviderUtils;
    private TrackPoint.Id startTrackPointId = null;

    private IntervalStatistics intervalStatistics;
    private Distance intervalDistance;

    private final AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            Log.d(TAG, "Audio focus changed to " + focusChange);

            boolean stop = false;
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:

                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    stop = true;
                    break;
                default :
                    throw new IllegalArgumentException("Invalid focusChange value" + foxusChange);
            }

            if (stop && tts != null && tts.isSpeaking()) {
                tts.stop();
                Log.i(TAG, "Aborting current tts due to focus change " + focusChange);
            }
        }
    };

    private final UtteranceProgressListener utteranceListener = new UtteranceProgressListener() {
        @Override
        public void onStart(String utteranceId) {
            int result = audioManager.requestAudioFocus(audioFocusChangeListener, AUDIO_STREAM, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                Log.w(TAG, "Failed to request audio focus.");
            }
        }

        @Override
        public void onDone(String utteranceId) {
            int result = audioManager.abandonAudioFocus(audioFocusChangeListener);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                Log.w(TAG, "Failed to relinquish audio focus.");
            }
        }

        @Override
        public void onError(String utteranceId) {
            Log.e(TAG, "An error occurred for utteranceId " + utteranceId);
        }
    };

    private TextToSpeech tts;
    // Response from TTS after its initialization
    private int ttsInitStatus = TextToSpeech.ERROR;

    private boolean ttsReady = false;

    private MediaPlayer ttsFallback;

    VoiceAnnouncement(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        contentProviderUtils = new ContentProviderUtils(context);
        intervalDistance = PreferencesUtils.getVoiceAnnouncementDistance();
        intervalStatistics = new IntervalStatistics(intervalDistance);
    }

    public void start() {
        Log.d(TAG, "Start");

        if (tts == null) {
            tts = new TextToSpeech(context, status -> {
                Log.i(TAG, "TextToSpeech initialized with status " + status);
                ttsInitStatus = status;
            });
        }
        if (ttsFallback == null) {
            ttsFallback = MediaPlayer.create(context, R.raw.tts_fallback);
            if (ttsFallback == null) {
                Log.w(TAG, "MediaPlayer for ttsFallback could not be created.");
            } else {
                ttsFallback.setAudioAttributes(
                        new AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build());
                ttsFallback.setLooping(false);
            }
        }
    }

    public void announce(@NonNull Track track) {
        synchronized (this) {
            if (!ttsReady) {
                ttsReady = ttsInitStatus == TextToSpeech.SUCCESS;
                if (ttsReady) {
                    onTtsReady();
                }
            }
        }

        if (Arrays.asList(AudioManager.MODE_IN_CALL, AudioManager.MODE_IN_COMMUNICATION)
                .contains(audioManager.getMode())) {
            Log.i(TAG, "Announcement is not allowed at this time.");
            return;
        }

        if (!ttsReady) {
            if (ttsFallback == null) {
                Log.w(TAG, "MediaPlayer for ttsFallback was not created.");
            } else {
                Log.i(TAG, "TTS not ready/available, just generating a tone.");
                ttsFallback.seekTo(0);
                ttsFallback.start();
            }
            return;
        }

        Distance currentIntervalDistance = PreferencesUtils.getVoiceAnnouncementDistance();
        if (currentIntervalDistance != intervalDistance) {
            intervalStatistics = new IntervalStatistics(currentIntervalDistance);
            intervalDistance = currentIntervalDistance;
            startTrackPointId = null;
        }

        TrackPointIterator trackPointIterator = new TrackPointIterator(contentProviderUtils, track.getId(), startTrackPointId);
        startTrackPointId = intervalStatistics.addTrackPoints(trackPointIterator);
        IntervalStatistics.Interval lastInterval = intervalStatistics.getLastInterval();
        SensorStatistics sensorStatistics = null;
        if (track.getId() != null) {
            sensorStatistics = contentProviderUtils.getSensorStats(track.getId());
        }

        Spannable announcement = VoiceAnnouncementUtils.getAnnouncement(context, track.getTrackStatistics(), PreferencesUtils.getUnitSystem(), PreferencesUtils.isReportSpeed(track.getCategory()), lastInterval, sensorStatistics);

        if (announcement.length() > 0) {
            // We don't care about the utterance id. It is supplied here to force onUtteranceCompleted to be called.
            tts.speak(announcement, TextToSpeech.QUEUE_FLUSH, null, "not used");
        }
    }

    public void stop() {
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }

        if (ttsFallback != null) {
            ttsFallback.release();
            ttsFallback = null;
        }
    }

    private void onTtsReady() {
        Locale locale = Locale.getDefault();
        int languageAvailability = tts.isLanguageAvailable(locale);
        if (languageAvailability == TextToSpeech.LANG_MISSING_DATA || languageAvailability == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Default locale not available, use English.");
            locale = Locale.ENGLISH;
        }
        tts.setLanguage(locale);
        tts.setSpeechRate(PreferencesUtils.getVoiceSpeedRate());
        tts.setOnUtteranceProgressListener(utteranceListener);
    }
}
