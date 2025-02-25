package de.dennisguse.opentracks.ui.intervals;

import androidx.annotation.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import de.dennisguse.opentracks.data.TrackPointIterator;
import de.dennisguse.opentracks.data.models.Distance;
import de.dennisguse.opentracks.data.models.HeartRate;
import de.dennisguse.opentracks.data.models.Speed;
import de.dennisguse.opentracks.data.models.TrackPoint;
import de.dennisguse.opentracks.stats.TrackStatistics;
import de.dennisguse.opentracks.stats.TrackStatisticsUpdater;

public class IntervalStatistics {
    private TrackStatisticsUpdater trackStatisticsUpdater = new TrackStatisticsUpdater();
    private final List<Interval> intervalList;
    private final Distance distanceInterval;
    private Interval interval;
    private Interval lastInterval;

    /**
     * @param distanceInterval distance of every interval.
     */
    public IntervalStatistics(Distance distanceInterval) {
        this.distanceInterval = distanceInterval;

        interval = new Interval();
        lastInterval = new Interval();
        intervalList = new ArrayList<>();
        intervalList.add(lastInterval);
    }

    /**
     * Complete intervals with the tracks points from the iterator.
     *
     * @return the last track point's id used to compute the intervals.
     */
    public TrackPoint.Id addTrackPoints(TrackPointIterator trackPointIterator) {
        boolean newIntervalAdded = false;
        TrackPoint trackPoint = null;

        while (trackPointIterator.hasNext()) {
            trackPoint = trackPointIterator.next();
            trackStatisticsUpdater.addTrackPoint(trackPoint);

            if (trackStatisticsUpdater.getTrackStatistics().getTotalDistance().plus(interval.distance).greaterOrEqualThan(distanceInterval)) {
                interval.add(trackStatisticsUpdater.getTrackStatistics(), trackPoint);

                double adjustFactor = distanceInterval.dividedBy(interval.distance);
                Interval adjustedInterval = new Interval(interval, adjustFactor);

                intervalList.set(intervalList.size() - 1, adjustedInterval);

                interval = new Interval(interval.distance.minus(adjustedInterval.distance), interval.time.minus(adjustedInterval.time));
                trackStatisticsUpdater = new TrackStatisticsUpdater();
                trackStatisticsUpdater.addTrackPoint(trackPoint);

                lastInterval = new Interval(interval);
                intervalList.add(lastInterval);

                newIntervalAdded = true;
            }
        }

        if (newIntervalAdded) {
            lastInterval.add(trackStatisticsUpdater.getTrackStatistics(), null);
        } else {
            lastInterval.set(trackStatisticsUpdater.getTrackStatistics());
        }

        return trackPoint != null ? trackPoint.getId() : null;
    }

    public List<Interval> getIntervalList() {
        return intervalList;
    }

    /**
     * Return the last completed interval.
     * An interval is complete if its distance is equal to distanceInterval_m.
     *
     * @return the interval object or null if any interval is completed.
     */
    public Interval getLastInterval() {
        if (intervalList.size() == 1 && intervalList.get(0).getDistance().lessThan(distanceInterval)) {
            return null;
        }

        for (int i = intervalList.size() - 1; i >= 0; i--) {
            if (intervalList.get(i).getDistance().greaterOrEqualThan(distanceInterval)) {
                return this.intervalList.get(i);
            }
        }

        return null;
    }

    public static class Interval {
        private Distance distance = Distance.of(0);
        private Duration time = Duration.ofSeconds(0);
        private Float gainM;
        private Float lossM;
        private HeartRate avgHeartRate;

        public Interval() {
//            distance = null;
//            time = null;
//            gainM = 0.0f;
//            lossM = 0.0f;
//            avgHeartRate = null;
        }

        public Interval(Distance distance, Duration time) {
            this.distance = distance;
            this.time = time;
        }

        public Interval(Interval i, double adjustFactor) {
            distance = i.distance.multipliedBy(adjustFactor);
            time = Duration.ofMillis((long) (i.time.toMillis() * adjustFactor));
            gainM = i.gainM;
            lossM = i.lossM;
            avgHeartRate = i.avgHeartRate;
        }

        public Interval(Interval i) {
            distance = i.distance;
            time = i.time;
            gainM = i.gainM;
            lossM = i.lossM;
            avgHeartRate = i.avgHeartRate;
        }

        public Distance getDistance() {
            return distance;
        }

        public Speed getSpeed() {
            return Speed.of(distance, time);
        }

        public boolean hasGain() {
            return gainM != null;
        }

        public Float getGainM() {
            return gainM;
        }

        public boolean hasLoss() {
            return lossM != null;
        }

        public Float getLossM() {
            return lossM;
        }

        public boolean hasAverageHeartRate() {
            return avgHeartRate != null;
        }

        public HeartRate getAverageHeartRate() {
            return avgHeartRate;
        }

        private void add(TrackStatistics trackStatistics, @Nullable TrackPoint lastTrackPoint) {
            distance = distance.plus(trackStatistics.getTotalDistance());
            time = time.plus(trackStatistics.getTotalTime());
            gainM = trackStatistics.hasTotalAltitudeGain() ? (float)trackStatistics.getMaxAltitude() : gainM;
            lossM = trackStatistics.hasTotalAltitudeLoss() ? (float)trackStatistics.getMinAltitude() : lossM;
            avgHeartRate = trackStatistics.getAverageHeartRate();
            if (lastTrackPoint == null) {
                return;
            }
            if (hasGain() && lastTrackPoint.hasAltitudeGain()) {
                gainM = gainM - lastTrackPoint.getAltitudeGain();
            }
            if (hasLoss() && lastTrackPoint.hasAltitudeLoss()) {
                lossM = lossM - lastTrackPoint.getAltitudeLoss();
            }
        }

        private void set(TrackStatistics trackStatistics) {
            distance = trackStatistics.getTotalDistance();
            time = trackStatistics.getTotalTime();
            gainM = trackStatistics.hasTotalAltitudeGain() ? (float)trackStatistics.getMaxAltitude() : gainM;
            lossM = trackStatistics.hasTotalAltitudeLoss() ? (float)trackStatistics.getMinAltitude() : lossM;
            avgHeartRate = trackStatistics.getAverageHeartRate();
        }
    }
}
