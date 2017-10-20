package com.microsoft.jenkins.azuread.oauth;

import com.microsoft.jenkins.azuread.Constants;
import org.json.*;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.exceptions.OAuthException;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthConstants;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

import java.util.Date;

/**
 * Azure OAuth2.0
 * Released under the same license as scribe (MIT License)
 *
 * @author houman001
 *         This code borrows from and modifies changes made by @yincrash and @donbeave
 * @author yincrash
 * @author <a href='mailto:donbeave@gmail.com'>Alexey Zhokhov</a>
 */
public class AzureApi extends DefaultApi20 {

    private static final String AUTHORIZE_PARAMS = "response_type=code&client_id=%s&redirect_uri=%s&resource=%s&state=%s";
    private static final String SCOPED_AUTHORIZE_PARAMS = AUTHORIZE_PARAMS + "&scope=%s";
    private static final String SUFFIX_OFFLINE = "&access_type=offline&approval_prompt=force";

    private boolean offline = false;

//    protected static String tenant = "72f988bf-86f1-41af-91ab-2d7cd011db47";
    protected static String tenant = "common";
//    protected static final String tenant = "15a9950e-5270-4308-a56e-f446137a868e";
//    protected static final String tenant = (String)Holders.getFlatConfig().get("oauth.providers.azure.tenant");


    public static String getTenant() {
        return tenant;
    }

    public static void setTenant(String tenant) {
        AzureApi.tenant = tenant;
    }


//    public AzureApi(final String tenant){
//        super();
//        this.tenant = tenant;
//    }

    @Override
    public String getAccessTokenEndpoint() {
        StringBuilder url = new StringBuilder();
        url.append(Constants.DEFAULT_AUTHENTICATION_ENDPOINT);
        if ((tenant != null)&&(!tenant.isEmpty())) {
            url.append(tenant);
        } else {
            url.append("common");
        }
//        // TODO: debug
//        url.append("715a9950e-5270-4308-a56e-f446137a868e");
        url.append("/oauth2/token");
        return url.toString();
    }

    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new AccessTokenExtractor() {
            @Override
            public Token extract(String response) {
                Preconditions.checkEmptyString(response, "Response body is incorrect. Can't extract a token from an empty string");

//                JSONObject tokenResponse = (JSONObject) JSON.parse(response);

                JSONObject tokenResponse = null;
                try {
                    tokenResponse = new JSONObject(response);
                    if (tokenResponse.get("access_token")!=null) {
                        String token = OAuthEncoder.decode(tokenResponse.getString("access_token"));
                        String refreshToken = "";
                        if (tokenResponse.get("refresh_token")!=null)
                            refreshToken = OAuthEncoder.decode(tokenResponse.getString("refresh_token"));
                        Date expiry = null;
                        if (tokenResponse.get("expires_in")!=null) {
                            int lifeTime = Integer.parseInt(OAuthEncoder.decode(tokenResponse.getString("expires_in")));
                            expiry = new Date(System.currentTimeMillis() + lifeTime * 1000);
                        }
                        return new AzureToken(token, refreshToken, expiry, response);
                    } else {
                        throw new OAuthException("Response body is incorrect. Can't extract a token from this: '" + response + "'", null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        StringBuilder url = new StringBuilder();
        url.append(Constants.DEFAULT_AUTHENTICATION_ENDPOINT);
        if ((tenant != null)&&(!tenant.isEmpty())) {
            url.append(tenant);
        } else {
            url.append("common");
        }
        url.append("/oauth2/authorize");
        // Append scope if present
        if (false/*config.hasScope()*/) {
//            return String.format(offline ?
//                            url.toString() +
//                                    "?" +
//                                    SCOPED_AUTHORIZE_PARAMS + SUFFIX_OFFLINE
//                            :
//                            url.toString() +
//                                    "?" +
//                                    SCOPED_AUTHORIZE_PARAMS, OAuthEncoder.encode(config.getApiKey()),
////                    OAuthEncoder.encode(config.getApiSecret()),
//                    OAuthEncoder.encode(config.getCallback()),
//                    OAuthEncoder.encode(Constants.DEFAULT_RESOURCE),
//                    OAuthEncoder.encode(config.getScope())
//            );
            return null;
        } else { // without scope
            String authorizationUrl =  String.format(offline ?
                            url.toString() +
                                    "?" +
                                    AUTHORIZE_PARAMS + SUFFIX_OFFLINE
                            :
                            url.toString() +
                                    "?" +
                                    AUTHORIZE_PARAMS, OAuthEncoder.encode(config.getApiKey()),
//                    OAuthEncoder.encode(config.getApiSecret()),
                    OAuthEncoder.encode(config.getCallback()),
                    OAuthEncoder.encode(config.getScope()/*Constants.DEFAULT_RESOURCE*/),
            config.getScope().equals(Constants.DEFAULT_RESOURCE) ? "0" : "1"
            );
            return authorizationUrl;
        }
    }


    public AzureToken refreshToken(Token accessToken, String clientID, String clientSecret, String resource) {
        OAuthRequest request = new OAuthRequest(Verb.POST,Constants.DEFAULT_AUTHENTICATION_ENDPOINT + tenant +"/oauth2/token");
        request.addBodyParameter("grant_type", "refresh_token");
        request.addBodyParameter("refresh_token", accessToken.getSecret()); // were accessToken is the Token object you want to refresh.
        request.addBodyParameter("client_id", clientID);
        request.addBodyParameter("client_secret", clientSecret);
        request.addBodyParameter("resource", resource);
        Response response = request.send();
        return (AzureToken) getAccessTokenExtractor().extract(response.getBody());

    }


    public AzureToken getAccessTokenByRefreshToken(Token token, String clientID, String clientSecret, String resource) {
        return refreshToken(token, clientID, clientSecret, resource);
    }




    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public OAuthService createService(OAuthConfig config) {
        return new Service(this, config);
    }

    public class Service extends OAuth20ServiceImpl {

        private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
        private static final String GRANT_TYPE = "grant_type";
        private DefaultApi20 api;
        private OAuthConfig config;

        public Service(DefaultApi20 api, OAuthConfig config) {
            super(api, config);
            this.api = api;
            this.config = config;
        }

        public void setOffline(boolean offline) {
            AzureApi.this.offline = offline;
        }

        public boolean isOffline() {
            return offline;
        }

        @Override
        public void signRequest(Token accessToken, OAuthRequest request) {
            request.addHeader("Authorization","Bearer "+accessToken.getToken());
            request.addHeader("Accept", "application/json");
        }

        @Override
        public Token getAccessToken(Token requestToken, Verifier verifier) {
            OAuthRequest request = new OAuthRequest(api.getAccessTokenVerb(), api.getAccessTokenEndpoint());
            switch (api.getAccessTokenVerb()) {
                case POST:
                    request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
                    request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
                    request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
                    request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
                    request.addBodyParameter(GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);
                    break;
                case GET:
                default:
                    request.addQuerystringParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
                    request.addQuerystringParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
                    request.addQuerystringParameter(OAuthConstants.CODE, verifier.getValue());
                    request.addQuerystringParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
                    if (config.hasScope()) request.addQuerystringParameter(OAuthConstants.SCOPE, config.getScope());
            }
            Response response = request.send();
            return api.getAccessTokenExtractor().extract(response.getBody());
        }


    }

}