package com.microsoft.jenkins.azuread.api;

import com.google.gson.annotations.SerializedName;

public class AzureUser extends AzureObject {

    @SerializedName("givenName")
    public String givenName;
    @SerializedName("surname")
    public String surname;
    @SerializedName("userPrincipalName")
    public String userPrincipalName;

    public AzureUser() {
        super();
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

