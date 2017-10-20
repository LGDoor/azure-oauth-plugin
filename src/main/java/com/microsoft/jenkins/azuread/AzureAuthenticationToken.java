package com.microsoft.jenkins.azuread;

import com.google.gson.Gson;
import hudson.security.SecurityRealm;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.utils.OAuthEncoder;

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
    private AzureApiToken azureRmToken;
//    private AzureApiToken azureAdToken;
    private AzureIdTokenUser azureIdTokenUser;
    public static final String APP_ONLY_TOKEN_KEY = "APP_ONLY_TOKEN_KEY";
    public static final TimeUnit CACHE_EXPIRY = TimeUnit.HOURS;
    private static final Logger LOGGER = Logger.getLogger(AbstractAuthenticationToken.class.getName());
    private static String servicePrincipal;


    private static AzureApiToken appOnlyToken;

//    private static final Cache<String, AzureApiToken> userToken =
//            CacheBuilder.newBuilder().expireAfterAccess(1, CACHE_EXPIRY).build();

    public AzureAuthenticationToken(AzureApiToken token, String clientID, String clientSecret, int tokenType) {
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        appOnlyToken = null;

        // extract user
        Gson gson = new Gson();
        String userInfo = token.getUserInfo();
        this.azureIdTokenUser = gson.fromJson(userInfo, AzureIdTokenUser.class);

        // store token
        System.out.println("set rm token");
        this.azureRmToken = token;

        // get aad token by rm reshresh token
//        System.out.println("set ad token");
//        this.azureAdToken = getAccessTokenByRefreshToken();


        boolean authenticated = false;

        if (azureIdTokenUser != null) {
            authenticated = true;
        }
        setAuthenticated(authenticated);
    }

    @Override
    public GrantedAuthority[] getAuthorities() {
        return this.azureIdTokenUser != null ? this.azureIdTokenUser.getAuthorities() : new GrantedAuthority[0];
    }

    /**
     * @return the azureRmToken
     */
//    public static Token getAzureRmToken(String userID) throws ExecutionException {
//        return userToken.get(userID, new Callable<AzureApiToken>() {
//            @Override
//            public AzureApiToken call() throws Exception {
//                // TODO: refresh token
//                return null;
//            }
//        });
//    }


    public AzureApiToken getAzureRmToken() {
        if (azureRmToken == null) return null;
        if (azureRmToken.getExpiry().after(new Date()))
            return azureRmToken;
        else { // refresh token
            System.out.println("refresh rm user token");
            AzureApi api = new AzureApi();
            AzureApiToken newToken = api.refreshToken(azureRmToken, clientID, clientSecret, Constants.DEFAULT_RESOURCE);
            this.azureRmToken = newToken;
            return newToken;
        }
    }

//    public AzureApiToken getAccessTokenByRefreshToken() {
//            System.out.println("get ad user token by refresh_token of rm token");
//            AzureApi api = new AzureApi();
//            AzureApiToken newToken = api.getAccessTokenByRefreshToken(this.getAzureRmToken(), clientID, clientSecret, Constants.DEFAULT_GRAPH_ENDPOINT);
//
//            this.azureAdToken = newToken;
//            return newToken;
//    }

//    public AzureApiToken getAzureAdToken() {
//        if (azureAdToken == null) return null;
//        if (azureAdToken.getExpiry().after(new Date()))
//            return azureAdToken;
//        else { // refresh token
//            System.out.println("refresh ad user token");
//            AzureApi api = new AzureApi();
////            AzureApiToken newToken = api.getAccessTokenByRefreshToken(this.getAzureRmToken(), clientID, clientSecret, Constants.DEFAULT_GRAPH_ENDPOINT);
//            AzureApiToken newToken = api.refreshToken(azureAdToken, clientID, clientSecret, Constants.DEFAULT_GRAPH_ENDPOINT);
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


    public static AzureApiToken refreshAppOnlyToken(final String clientid, final String clientsecret, final String tenant) throws JSONException, ExecutionException, IOException {
        appOnlyToken = null;
        return getAppOnlyToken();
    }

    public static AzureApiToken getAppOnlyToken() throws ExecutionException, IOException, JSONException {

        SecurityRealm realm = Utils.JenkinsUtil.getSecurityRealm();

        if (!(realm instanceof AzureSecurityRealm)) return null;

        String clientId = ((AzureSecurityRealm) realm).getClientid();
        String clientSecret = ((AzureSecurityRealm) realm).getClientsecret();
        String tenant = ((AzureSecurityRealm) realm).getTenant();


        if (!Constants.DEBUG && appOnlyToken != null && appOnlyToken.getExpiry().after(new Date())) {
            return appOnlyToken;
        } else { // refresh token
            System.out.println("refresh app only token");
            org.apache.http.HttpResponse response = AzureAdApi.getAppOnlyAccessTokenResponce(clientId, clientSecret, tenant);
                    int statusCode = HttpHelper.getStatusCode(response);
                    String content = HttpHelper.getContent(response);
                    if (statusCode == 200) {
                        JSONObject json = new JSONObject(content);
                        String accessToken = OAuthEncoder.decode(json.getString("access_token"));
//                        String refreshToken = OAuthEncoder.decode(json.toStr("refresh_token"));
                        int lifeTime = Integer.parseInt(OAuthEncoder.decode(json.getString("expires_in")));
                        Date expiry = new Date(System.currentTimeMillis() + lifeTime * 1000);
                        appOnlyToken = new AzureApiToken(accessToken, "", expiry, content);
                        return appOnlyToken;
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
//        return getName();
        return azureIdTokenUser.getObjectID();
//        return azureIdTokenUser.getUniqueName();
    }

    @Override
    public String getName() {
        return (azureIdTokenUser != null ? azureIdTokenUser.getUsername() : null);
    }

    public AzureIdTokenUser getAzureIdTokenUser() {
        return azureIdTokenUser;
    }

//    public Set<String> getMemberGroups() throws ExecutionException {
//        String userId = azureIdTokenUser.getObjectId();
//        System.out.println("getMemberGroups ");
//        return groupsByUserId.get(userId, new Callable<Set<String>>() {
//            @Override
//            public Set<String> call() throws Exception {
////                String accessToken = azureRmToken.getToken();
//                String tenant = azureIdTokenUser.getTenantID();
//                String userId = azureIdTokenUser.getObjectId();
//                HttpResponse accessTokenResponce = AzureAdApi.getAppOnlyAccessTokenResponce(clientID, clientSecret, tenant);
//                int statusCode = HttpHelper.getStatusCode(accessTokenResponce);
//                String content = HttpHelper.getContent(accessTokenResponce);
//                if (statusCode != 200) {
//                    LOGGER.log(Level.SEVERE, "Cannot get app only access token!");
//                    return null;
//                }
//                JSONObject json = new JSONObject(content);
//                String accessToken = json.toStr("access_token");
//
//                System.out.println("Get member group via azure ad api");
//                AzureResponse response = AzureAdApi.getGroupsByUserId(accessToken);
//                return response.getGroupsByUserId();
//            }
//        });
//    }



}
