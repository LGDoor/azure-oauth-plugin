package com.microsoft.jenkins.azuread;

import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.microsoft.jenkins.azuread.api.AzureAdApiClient;
import com.microsoft.jenkins.azuread.scribe.AzureApi;
import com.microsoft.jenkins.azuread.scribe.AzureToken;
import hudson.security.SecurityRealm;
import jenkins.model.Jenkins;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 * Created by t-wanl on 8/9/2017.
 */


public class AzureAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 2L;

    private String clientID;
    private String clientSecret;
    private AzureToken azureRmToken;
    private AzureAdUser azureAdUser;
    public static final String APP_ONLY_TOKEN_KEY = "APP_ONLY_TOKEN_KEY";
    public static final TimeUnit CACHE_EXPIRY = TimeUnit.HOURS;
    private static final Logger LOGGER = Logger.getLogger(AbstractAuthenticationToken.class.getName());
    private static String servicePrincipal;


    private static volatile AzureToken appOnlyToken;

//    private static final Cache<String, AzureToken> userToken =
//            CacheBuilder.newBuilder().expireAfterAccess(1, CACHE_EXPIRY).build();

    public AzureAuthenticationToken(AzureToken token, String clientID, String clientSecret, int tokenType) {
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        appOnlyToken = null;

        // extract user
        this.azureAdUser = AzureAdUser.createFromJwt(token.getIdToken());

        // store token
        System.out.println("set rm token");
        this.azureRmToken = token;

        // get aad token by rm reshresh token
//        System.out.println("set ad token");
//        this.azureAdToken = getAccessTokenByRefreshToken();


        boolean authenticated = false;

        if (azureAdUser != null) {
            authenticated = true;
        }
        setAuthenticated(authenticated);
    }

    @Override
    public GrantedAuthority[] getAuthorities() {
        return this.azureAdUser != null ? this.azureAdUser.getAuthorities() : new GrantedAuthority[0];
    }

    /**
     * @return the azureRmToken
     */
//    public static Token getAzureRmToken(String userID) throws ExecutionException {
//        return userToken.get(userID, new Callable<AzureToken>() {
//            @Override
//            public AzureToken call() throws Exception {
//                // TODO: refresh token
//                return null;
//            }
//        });
//    }


//    public AzureToken getAzureRmToken() {
//        if (azureRmToken == null) return null;
//        if (!azureRmToken.isExpired())
//            return azureRmToken;
//        else { // refresh token
//            System.out.println("refresh rm user token");
//            AzureToken newToken = api.refreshToken(azureRmToken, clientID, clientSecret, Constants.DEFAULT_RESOURCE);
//            this.azureRmToken = newToken;
//            return newToken;
//        }
//    }

//    public AzureToken getAccessTokenByRefreshToken() {
//            System.out.println("get ad user token by refresh_token of rm token");
//            AzureApi client = new AzureApi();
//            AzureToken newToken = client.getAccessTokenByRefreshToken(this.getAzureRmToken(), clientID, clientSecret, Constants.DEFAULT_GRAPH_ENDPOINT);
//
//            this.azureAdToken = newToken;
//            return newToken;
//    }

//    public AzureToken getAzureAdToken() {
//        if (azureAdToken == null) return null;
//        if (azureAdToken.getExpiry().after(new Date()))
//            return azureAdToken;
//        else { // refresh token
//            System.out.println("refresh ad user token");
//            AzureApi client = new AzureApi();
////            AzureToken newToken = client.getAccessTokenByRefreshToken(this.getAzureRmToken(), clientID, clientSecret, Constants.DEFAULT_GRAPH_ENDPOINT);
//            AzureToken newToken = client.refreshToken(azureAdToken, clientID, clientSecret, Constants.DEFAULT_GRAPH_ENDPOINT);
//
//            this.azureAdToken = newToken;
//            return newToken;
//        }
//    }

    public static String getServicePrincipal() {
        return servicePrincipal;
    }

    public static void setServicePrincipal(String servicePrincipal) {
        AzureAuthenticationToken.servicePrincipal = servicePrincipal;
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
//        return getName();
        return azureAdUser.getObjectID();
//        return azureAdUser.getUniqueName();
    }

    @Override
    public String getName() {
        return (azureAdUser != null ? azureAdUser.getUsername() : null);
    }

    public AzureAdUser getAzureAdUser() {
        return azureAdUser;
    }

//    public Set<String> getMemberGroups() throws ExecutionException {
//        String userId = azureAdUser.getObjectId();
//        System.out.println("getMemberGroups ");
//        return groupsByUserId.get(userId, new Callable<Set<String>>() {
//            @Override
//            public Set<String> call() throws Exception {
////                String accessToken = azureRmToken.getToken();
//                String tenant = azureAdUser.getTenantID();
//                String userId = azureAdUser.getObjectId();
//                HttpResponse accessTokenResponce = AzureAdApiClient.getAppOnlyAccessTokenResponce(clientID, clientSecret, tenant);
//                int statusCode = HttpHelper.getStatusCode(accessTokenResponce);
//                String content = HttpHelper.getContent(accessTokenResponce);
//                if (statusCode != 200) {
//                    LOGGER.log(Level.SEVERE, "Cannot get app only access token!");
//                    return null;
//                }
//                JSONObject json = new JSONObject(content);
//                String accessToken = json.toStr("access_token");
//
//                System.out.println("Get member group via azure ad client");
//                AzureResponse response = AzureAdApiClient.getGroupsByUserId(accessToken);
//                return response.getGroupsByUserId();
//            }
//        });
//    }



}
