package com.microsoft.jenkins.azuread.api;

import com.google.gson.annotations.SerializedName;

public class AzureObject {
    @SerializedName("displayName")
    private String displayName;
    @SerializedName("id")
    private String objectId;

    public AzureObject() {}

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


