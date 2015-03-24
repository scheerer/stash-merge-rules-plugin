package com.scheerer.stash.merge.checks.validations;

public class MergeCheckValidationResult {

    public enum Status { VALID, INVALID };

    private Status status = Status.INVALID;
    private String message = "";

    public MergeCheckValidationResult(String message, Status status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }
}
