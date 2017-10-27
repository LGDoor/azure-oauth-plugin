package com.microsoft.jenkins.azuread.api;

import com.google.gson.annotations.SerializedName;

public class AzureGroup extends AzureObject {

    @SerializedName("description")
    public String description;

    public AzureGroup() {
        super();
    }

    public String getDescription() {
        return description;
    }
}


