package com.microsoft.jenkins.azuread;

import com.google.gson.annotations.SerializedName;

public class AzureUser extends AzureObject {

    @SerializedName("displayName")
    public String displayName;
    @SerializedName("givenName")
    public String givenName;
    @SerializedName("id")
    public String objectId;
    @SerializedName("surname")
    public String surname;
    @SerializedName("userPrincipalName")
    public String userPrincipalName;


    public AzureUser() {
        super();
    }

    @Override
    public String getObjectId() {
        return objectId;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getSurname() {
        return surname;
    }

    public String getUserPrincipalName() {
        return userPrincipalName;
    }
}

