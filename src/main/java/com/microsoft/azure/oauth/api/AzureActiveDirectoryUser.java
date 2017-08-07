package com.microsoft.azure.oauth.api;

import com.google.gson.annotations.SerializedName;
import hudson.security.SecurityRealm;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.apache.commons.lang.StringUtils;

public class AzureActiveDirectoryUser implements UserDetails {

    public class BitbucketUserResponce {
        public AzureActiveDirectoryUser user;
    }

    public String username = StringUtils.EMPTY;
    @SerializedName("first_name")
    public String firstName;
    @SerializedName("last_name")
    public String lastName;
    @SerializedName("is_team")
    public boolean isTeam;
    public String avatar;
    @SerializedName("resource_uri")
    public String resourceUri;

    public AzureActiveDirectoryUser() {
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
        return this.username;
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

}
