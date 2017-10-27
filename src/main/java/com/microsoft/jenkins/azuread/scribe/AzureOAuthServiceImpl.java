package com.microsoft.jenkins.azuread.scribe;

import com.github.scribejava.core.model.OAuthConfig;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.oauth.OAuth20Service;

public class AzureOAuthServiceImpl extends OAuth20Service {
    /**
     * Default constructor
     *
     * @param api    OAuth2.0 api information
     * @param config OAuth 2.0 configuration param object
     */
    public AzureOAuthServiceImpl(AzureApi api, OAuthConfig config) {
        super(api, config);
    }

    @Override
    protected OAuthRequest createAccessTokenRequest(String code) {
        OAuthRequest request = super.createAccessTokenRequest(code);
        request.addParameter("resource", getApi().getResource());
        return request;
    }

    @Override
    public AzureApi getApi() {
        return (AzureApi) super.getApi();
    }
}
