package com.microsoft.jenkins.azuread;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.annotations.SerializedName;
import hudson.security.SecurityRealm;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.apache.commons.lang.StringUtils;

public class AzureAdUser implements UserDetails {

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

    public AzureAdUser() {
        super();
    }

    public static AzureAdUser createFromJwt(String jwt) {
        if (StringUtils.isEmpty(jwt))
            return null;

        DecodedJWT decode = JWT.decode(jwt);
        AzureAdUser user = new AzureAdUser();
        user.userName = decode.getClaim("name").asString();
        user.givenName = decode.getClaim("given_name").asString();
        user.familyName = decode.getClaim("family_name").asString();
        user.uniqueName = decode.getClaim("unique_name").asString();
        user.tenantID = decode.getClaim("tid").asString();
        user.objectID = decode.getClaim("oid").asString();
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AzureAdUser that = (AzureAdUser) o;
        return uniqueName.equals(that.uniqueName);
    }

    @Override
    public int hashCode() {
        return uniqueName.hashCode();
    }

    @Override
    public GrantedAuthority[] getAuthorities() {
        return new GrantedAuthority[] { SecurityRealm.AUTHENTICATED_AUTHORITY };
    }

    @Override
    public String getPassword() {
        return "";
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
}

