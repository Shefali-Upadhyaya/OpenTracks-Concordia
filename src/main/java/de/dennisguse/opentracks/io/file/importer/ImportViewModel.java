package de.dennisguse.opentracks.io.file.importer;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import de.dennisguse.opentracks.R;
import de.dennisguse.opentracks.data.models.Track;
import de.dennisguse.opentracks.util.FileUtils;

public class ImportViewModel extends AndroidViewModel implements ImportServiceResultReceiver.Receiver {

    private static final String TAG = ImportViewModel.class.getSimpleName();

    private MutableLiveData<Summary> importData;
    private final ImportServiceResultReceiver resultReceiver;
    private final Summary summary;
    private boolean cancel = false;
    private final List<DocumentFile> filesToImport = new ArrayList<>();

    public ImportViewModel(@NonNull Application application) {
        super(application);
        resultReceiver = new ImportServiceResultReceiver(new Handler(), this);
        summary = new Summary();
    }

    LiveData<Summary> getImportData(List<DocumentFile> documentFiles, String typeOfClick) {
        if (importData == null) {
            importData = new MutableLiveData<>();
            if (typeOfClick != null && typeOfClick.equals("cloud")) getCloudData(documentFiles);
            else loadData(documentFiles);
        }
        return importData;
    }

    private void getCloudData(List<DocumentFile> documentFiles) {
        List<DocumentFile> fileList = new ArrayList<>();
        fileList.addAll(documentFiles);
        while (fileList.size() != 0) {
            ImportService.enqueue(getApplication(), resultReceiver, fileList.get(0).getUri(), fileList.get(0).getName(), "cloud");
            fileList.remove(0);
        }
    }

    void cancel() {
        cancel = true;
    }

    private void loadData(List<DocumentFile> documentFiles) {
        List<ArrayList<DocumentFile>> nestedFileList = documentFiles.stream()
                .map(FileUtils::getFiles)
                // TODO flatMap(Collection::stream) fails with ClassCastException; try in the future again
                .collect(Collectors.toList());

        List<DocumentFile> fileList = new ArrayList<>();
        nestedFileList.forEach(fileList::addAll);

        summary.totalCount = fileList.size();
        filesToImport.addAll(fileList);
        importNextFile();
    }

    private void importNextFile() {
        if (cancel || filesToImport.isEmpty()) {
            return;
        }
        ImportService.enqueue(getApplication(), resultReceiver, filesToImport.get(0).getUri(), filesToImport.get(0).getName(), "local");
        filesToImport.remove(0);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultData == null) {
            throw new NullPointerException(TAG + ": onReceiveResult resultData NULL");
        }

        ArrayList<Track.Id> trackIds = resultData.getParcelableArrayList(ImportServiceResultReceiver.RESULT_EXTRA_LIST_TRACK_ID);
        String fileName = resultData.getString(ImportServiceResultReceiver.RESULT_EXTRA_FILENAME);
        String message = resultData.getString(ImportServiceResultReceiver.RESULT_EXTRA_MESSAGE);

        switch (resultCode) {
            case ImportServiceResultReceiver.RESULT_CODE_ERROR:
                summary.errorCount++;
                summary.fileErrors.add(getApplication().getString(R.string.import_error_info, fileName, message));
                break;
            case ImportServiceResultReceiver.RESULT_CODE_IMPORTED:
                summary.importedTrackIds.addAll(trackIds);
                summary.successCount++;
                break;
            case ImportServiceResultReceiver.RESULT_CODE_ALREADY_EXISTS:
                summary.existsCount++;
                break;
            default:
                throw new NullPointerException(TAG + ": import service result code invalid: " + resultCode);
        }

        importData.postValue(summary);
        importNextFile();
    }

    static class Summary {
        private int totalCount;
        private int successCount;
        private int existsCount;
        private int errorCount;
        private final ArrayList<Track.Id> importedTrackIds = new ArrayList<>();
        private final ArrayList<String> fileErrors = new ArrayList<>();

        public int getTotalCount() {
            return totalCount;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getExistsCount() {
            return existsCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public List<Track.Id> getImportedTrackIds() {
            return importedTrackIds;
        }

        public ArrayList<String> getFileErrors() {
            return fileErrors;
        }
    }
}
