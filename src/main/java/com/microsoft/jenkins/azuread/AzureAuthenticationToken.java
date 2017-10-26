package com.microsoft.jenkins.azuread;

import com.github.scribejava.core.oauth.OAuth20Service;
import com.microsoft.jenkins.azuread.scribe.AzureToken;
import jenkins.model.Jenkins;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 * Created by t-wanl on 8/9/2017.
 */


public class AzureAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 2L;

    private AzureAdUser azureAdUser;
    private static final Logger LOGGER = Logger.getLogger(AbstractAuthenticationToken.class.getName());


    private static volatile AzureToken appOnlyToken;

    public AzureAuthenticationToken(AzureToken token) {
        this.azureAdUser = AzureAdUser.createFromJwt(token.getIdToken());
        setAuthenticated(azureAdUser != null);
    }

    @Override
    public GrantedAuthority[] getAuthorities() {
        return this.azureAdUser != null ? this.azureAdUser.getAuthorities() : new GrantedAuthority[0];
    }


    public static void refreshAppOnlyToken()
            throws ExecutionException, IOException, InterruptedException {
        appOnlyToken = null;
        getAppOnlyToken();
    }

    public static AzureToken getAppOnlyToken() throws ExecutionException, IOException, InterruptedException {
        if (Constants.DEBUG || appOnlyToken == null || appOnlyToken.isExpired()) {
            // refresh token
            System.out.println("refresh app only token");
            if (Jenkins.getActiveInstance().getSecurityRealm() instanceof AzureSecurityRealm) {
                AzureSecurityRealm securityRealm = (AzureSecurityRealm) Jenkins.getActiveInstance().getSecurityRealm();
                OAuth20Service oAuthService = securityRealm.getOAuthService();
                appOnlyToken = (AzureToken) oAuthService.refreshAccessToken(appOnlyToken.getRefreshToken());
            }
        }
        return appOnlyToken;
    }

    @Override
    public Object getCredentials() {
        return StringUtils.EMPTY;
    }

    @Override
    public Object getPrincipal() {
        return azureAdUser.getObjectID();
    }

    @Override
    public String getName() {
        return (azureAdUser != null ? azureAdUser.getUsername() : null);
    }

    public AzureAdUser getAzureAdUser() {
        return azureAdUser;
    }
}
