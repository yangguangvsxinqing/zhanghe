package com.fineos.fileexplorer.operations;

/**
 * Created by acmllaugh on 14-12-10.
 */
public class OperationResult {
    private boolean success;
    private OperationResultType result;
    private String resultDetails;
    public enum OperationResultType{
        FINISHED,FILE_NOT_EXIST, CREATE_DIR_FAILED,
        TARGET_STORAGE_IS_FULL, FILE_IO_ERROR, DESTATION_DIR_NOT_AVAILABLE,
        TARGET_FILE_CANNOT_WRITE, CANCEL,
        CANNOT_COPY_FILE_INTO_SELF, OTHER_REASONS
    }

    public OperationResult(boolean success, OperationResultType result, String resultDetails) {
        this.result = result;
        this.success = success;
        this.resultDetails = resultDetails;
    }

    public boolean isSuccess() {
        return success;
    }

    public OperationResultType getResult() {
        return result;
    }

    public String getResultDetails() {
        return resultDetails;
    }

    @Override
    public String toString() {
        switch (result) {
            case FINISHED:
                return "operation successfully finished.";
            case CREATE_DIR_FAILED:
                return "operation failed due to create directory failed.";
            case FILE_IO_ERROR:
                return "File input/output exception";
            case TARGET_STORAGE_IS_FULL:
                return "Target storage is full.";
            case CANCEL:
                return "User cancelled operation.";
            case DESTATION_DIR_NOT_AVAILABLE:
                return "Destination folder is not available";
            case FILE_NOT_EXIST:
                return "Operation failed due to file not exists exception";
        }
        return "Operation failed due to some unknown issues.";
    }
}
