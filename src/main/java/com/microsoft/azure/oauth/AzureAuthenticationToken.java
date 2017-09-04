package com.microsoft.azure.oauth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hudson.util.FormValidation;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.Token;
import com.microsoft.azure.oauth.AzureUser.AzureUserResponce;
import org.scribe.utils.OAuthEncoder;

import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by t-wanl on 8/9/2017.
 */


public class AzureAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 2L;

    private String clientID;
    private String clientSecret;
    private AzureApiToken azureApiToken;
    private AzureUser azureUser;
    public static final String APP_ONLY_TOKEN_KEY = "APP_ONLY_TOKEN_KEY";
    public static final TimeUnit CACHE_EXPIRY = TimeUnit.HOURS;
    private static final Logger LOGGER = Logger.getLogger(AbstractAuthenticationToken.class.getName());
    private static String servicePrincipal;

    private static final Cache<String, Set<String>> groupsByUserId =
            CacheBuilder.newBuilder().expireAfterAccess(1, CACHE_EXPIRY).build();

    private static AzureApiToken appOnlyToken;

//    private static final Cache<String, AzureApiToken> userToken =
//            CacheBuilder.newBuilder().expireAfterAccess(1, CACHE_EXPIRY).build();

    public AzureAuthenticationToken(AzureApiToken azureApiToken, String clientID, String clientSecret) {
        // extract user
        Gson gson = new Gson();
        String userInfo = azureApiToken.getUserInfo();
        this.azureUser = gson.fromJson(userInfo, AzureUser.class);

        // store token
        this.azureApiToken = azureApiToken;
//        userToken.put(this.azureUser.getObjectID(), azureApiToken);

        this.clientID = clientID;
        this.clientSecret = clientSecret;

        boolean authenticated = false;

        if (azureUser != null) {
            authenticated = true;
        }

        appOnlyToken = null;

        setAuthenticated(authenticated);
    }

    @Override
    public GrantedAuthority[] getAuthorities() {
        return this.azureUser != null ? this.azureUser.getAuthorities() : new GrantedAuthority[0];
    }

    /**
     * @return the azureApiToken
     */
//    public static Token getAzureApiToken(String userID) throws ExecutionException {
//        return userToken.get(userID, new Callable<AzureApiToken>() {
//            @Override
//            public AzureApiToken call() throws Exception {
//                // TODO: refresh token
//                return null;
//            }
//        });
//    }


    public AzureApiToken getAzureApiToken() {
        if (azureApiToken != null && azureApiToken.getExpiry().after(new Date()))
            return azureApiToken;
        else { // refresh token
            System.out.println("refresh user token");
            AzureApi api = new AzureApi();
            AzureApiToken newToken = (AzureApiToken) api.rereshToken(azureApiToken, clientID, clientSecret, Constants.DEFAULT_RESOURCE);
            this.azureApiToken = newToken;
            return newToken;
        }
    }

    public static String getServicePrincipal() {
        return servicePrincipal;
    }

    public static void setServicePrincipal(String servicePrincipal) {
        AzureAuthenticationToken.servicePrincipal = servicePrincipal;
    }

    public static AzureApiToken getAppOnlyToken(final String clientid, final String clientsecret, final String tenant) throws ExecutionException, IOException, JSONException {
//        String accessToken = appOnlyToken.get("", new Callable<String>() {
//                @Override
//                public String call() throws Exception {
//                    org.apache.http.HttpResponse response = AzureAdApi.getAppOnlyAccessTokenResponce(clientid, clientsecret, tenant);
//                    int statusCode = HttpHelper.getStatusCode(response);
//                    String content = HttpHelper.getContent(response);
//                    if (statusCode == 200) {
//                        JSONObject json = new JSONObject(content);
//                        String accessToken = json.getString("access_token");
//                        return accessToken;
//                    } else {
//                        return null;
//                    }
//                }
//        });

//        return accessToken;


        //
        if (appOnlyToken != null && appOnlyToken.getExpiry().after(new Date())) {
            return appOnlyToken;
        } else { // refresh token
            System.out.println("refresh app only token");
            org.apache.http.HttpResponse response = AzureAdApi.getAppOnlyAccessTokenResponce(clientid, clientsecret, tenant);
                    int statusCode = HttpHelper.getStatusCode(response);
                    String content = HttpHelper.getContent(response);
                    if (statusCode == 200) {
                        JSONObject json = new JSONObject(content);
                        String accessToken = OAuthEncoder.decode(json.getString("access_token"));
//                        String refreshToken = OAuthEncoder.decode(json.getString("refresh_token"));
                        int lifeTime = Integer.parseInt(OAuthEncoder.decode(json.getString("expires_in")));
                        Date expiry = new Date(System.currentTimeMillis() + lifeTime * 1000);
                        return new AzureApiToken(accessToken, "", expiry, content);
                    } else {
                        return null;
                    }
        }

    }

    @Override
    public Object getCredentials() {
        return StringUtils.EMPTY;
    }

    @Override
    public Object getPrincipal() {
        return getName();
    }

    @Override
    public String getName() {
        return (azureUser != null ? azureUser.getUsername() : null);
    }

    public AzureUser getAzureUser() {
        return azureUser;
    }

    public Set<String> getMemberGroups() throws ExecutionException {
        String userId = azureUser.getObjectID();
        System.out.println("getMemberGroups ");
        return groupsByUserId.get(userId, new Callable<Set<String>>() {
            @Override
            public Set<String> call() throws Exception {
//                String accessToken = azureApiToken.getToken();
                String tenant = azureUser.getTenantID();
                String userId = azureUser.getObjectID();
                HttpResponse accessTokenResponce = AzureAdApi.getAppOnlyAccessTokenResponce(clientID, clientSecret, tenant);
                int statusCode = HttpHelper.getStatusCode(accessTokenResponce);
                String content = HttpHelper.getContent(accessTokenResponce);
                if (statusCode != 200) {
                    LOGGER.log(Level.SEVERE, "Cannot get app only access token!");
                    return null;
                }
                JSONObject json = new JSONObject(content);
                String accessToken = json.getString("access_token");

                System.out.println("Get member group via azure ad api");
                return AzureAdApi.getGroupsByUserId(accessToken, tenant, userId);
            }
        });
    }

}
