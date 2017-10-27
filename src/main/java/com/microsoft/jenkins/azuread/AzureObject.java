package com.microsoft.jenkins.azuread;

public class AzureObject {

    private String displayName;

    private String objectId;

    public AzureObject(String objectId, String displayName) {
        this.objectId = objectId;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}


