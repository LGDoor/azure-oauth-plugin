package com.microsoft.jenkins.azuread.client;

import com.google.gson.annotations.SerializedName;
import com.microsoft.jenkins.azuread.client.AzureObject;

public class AzureGroup extends AzureObject {

    @SerializedName("id")
    public String objectId;
    @SerializedName("displayName")
    public String displayName;
    @SerializedName("description")
    public String description;


    public AzureGroup() {
        super();
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getObjectId() {
        return objectId;
    }
}


