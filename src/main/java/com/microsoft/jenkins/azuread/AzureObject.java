package com.microsoft.jenkins.azuread;

import com.google.gson.annotations.SerializedName;

public abstract class AzureObject {
    public abstract String getObjectId();
    public abstract String getDisplayName();
}


