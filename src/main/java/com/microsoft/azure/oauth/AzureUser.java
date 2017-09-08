package com.microsoft.azure.oauth;

import com.google.gson.annotations.SerializedName;

public class AzureUser {

    @SerializedName("name")
    public String userName;
    @SerializedName("given_name")
    public String givenName;
    @SerializedName("family_name")
    public String familyName;
    @SerializedName("unique_name")
    public String uniqueName; // real unique principal name
    @SerializedName("tid")
    public String tenantID;
    @SerializedName("oid")
    public String objectID;
    @SerializedName("upn")
    public String UderPrincipleName;

    public AzureUser() {
        super();
    }

    public String getTenantID() {
        return tenantID;
    }

    public String getObjectID() {
        return objectID;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getUserName() {
        return userName;
    }
}

