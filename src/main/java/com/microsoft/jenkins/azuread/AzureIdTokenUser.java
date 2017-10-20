package com.microsoft.jenkins.azuread;

import com.google.gson.annotations.SerializedName;
import hudson.security.SecurityRealm;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;

public class AzureIdTokenUser implements UserDetails {

    public class AzureUserResponce {
        public AzureIdTokenUser user;
    }

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

    public AzureIdTokenUser() {
        super();
    }

    @Override
    public GrantedAuthority[] getAuthorities() {
        return new GrantedAuthority[] { SecurityRealm.AUTHENTICATED_AUTHORITY };
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
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

