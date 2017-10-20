/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE file in the project root for license information.
 */
package com.microsoft.jenkins.azuread;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.Subscription;
import hudson.Extension;
import hudson.security.ACL;
import hudson.security.SecurityRealm;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;


/**
 * Generated localization support class.
 *
 */

@SuppressWarnings({
        "",
        "PMD",
        "all"
})
//class Messages {
//
//    /**
//     * The resource bundle reference
//     *
//     */
//    private final static ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);
//
//    /**
//     * Key {@code Azure_OAuthToken_Malformed}: {@code Error: OAuth 2.0 Token
//
//     * Endpoint is malformed.}.
//     *
//     * @return
//     *     {@code Error: OAuth 2.0 Token Endpoint is malformed.}
//     */
//    public static String Azure_OAuthToken_Malformed() {
//        return holder.format("Azure_OAuthToken_Malformed");
//    }
//
//    /**
//     * Key {@code Azure_OAuthToken_Malformed}: {@code Error: OAuth 2.0 Token
//
//     * Endpoint is malformed.}.
//     *
//     * @return
//     *     {@code Error: OAuth 2.0 Token Endpoint is malformed.}
//     */
//    public static Localizable _Azure_OAuthToken_Malformed() {
//        return new Localizable(holder, "Azure_OAuthToken_Malformed");
//    }
//
//    /**
//     * Key {@code Azure_Credentials_Binding_Diaplay_Name}: {@code Microsoft
//
//     * Azure Service Principal}.
//     *
//     * @return
//     *     {@code Microsoft Azure Service Principal}
//     */
//    public static String Azure_Credentials_Binding_Diaplay_Name() {
//        return holder.format("Azure_Credentials_Binding_Diaplay_Name");
//    }
//
//    /**
//     * Key {@code Azure_Credentials_Binding_Diaplay_Name}: {@code Microsoft
//
//     * Azure Service Principal}.
//     *
//     * @return
//     *     {@code Microsoft Azure Service Principal}
//     */
//    public static Localizable _Azure_Credentials_Binding_Diaplay_Name() {
//        return new Localizable(holder, "Azure_Credentials_Binding_Diaplay_Name");
//    }
//
//    /**
//     * Key {@code Azure_ClientSecret_Missing}: {@code Error: Client Secret is
//
//     * missing.}.
//     *
//     * @return
//     *     {@code Error: Client Secret is missing.}
//     */
//    public static String Azure_ClientSecret_Missing() {
//        return holder.format("Azure_ClientSecret_Missing");
//    }
//
//    /**
//     * Key {@code Azure_ClientSecret_Missing}: {@code Error: Client Secret is
//
//     * missing.}.
//     *
//     * @return
//     *     {@code Error: Client Secret is missing.}
//     */
//    public static Localizable _Azure_ClientSecret_Missing() {
//        return new Localizable(holder, "Azure_ClientSecret_Missing");
//    }
//
//    /**
//     * Key {@code Azure_Invalid_SubscriptionId}: {@code The subscription id
//
//     * is not valid}.
//     *
//     * @return
//     *     {@code The subscription id is not valid}
//     */
//    public static String Azure_Invalid_SubscriptionId() {
//        return holder.format("Azure_Invalid_SubscriptionId");
//    }
//
//    /**
//     * Key {@code Azure_Invalid_SubscriptionId}: {@code The subscription id
//
//     * is not valid}.
//     *
//     * @return
//     *     {@code The subscription id is not valid}
//     */
//    public static Localizable _Azure_Invalid_SubscriptionId() {
//        return new Localizable(holder, "Azure_Invalid_SubscriptionId");
//    }
//
//    /**
//     * Key {@code Azure_Config_Success}: {@code Successfully verified the
//
//     * Microsoft Azure Service Principal.}.
//     *
//     * @return
//     *     {@code Successfully verified the Microsoft Azure Service Principal.}
//     */
//    public static String Azure_Config_Success() {
//        return holder.format("Azure_Config_Success");
//    }
//
//    /**
//     * Key {@code Azure_Config_Success}: {@code Successfully verified the
//
//     * Microsoft Azure Service Principal.}.
//     *
//     * @return
//     *     {@code Successfully verified the Microsoft Azure Service Principal.}
//     */
//    public static Localizable _Azure_Config_Success() {
//        return new Localizable(holder, "Azure_Config_Success");
//    }
//
//    /**
//     * Key {@code Azure_OAuthToken_Missing}: {@code Error: OAuth 2.0 Token
//
//     * Endpoint is missing.}.
//     *
//     * @return
//     *     {@code Error: OAuth 2.0 Token Endpoint is missing.}
//     */
//    public static String Azure_OAuthToken_Missing() {
//        return holder.format("Azure_OAuthToken_Missing");
//    }
//
//    /**
//     * Key {@code Azure_OAuthToken_Missing}: {@code Error: OAuth 2.0 Token
//
//     * Endpoint is missing.}.
//     *
//     * @return
//     *     {@code Error: OAuth 2.0 Token Endpoint is missing.}
//     */
//    public static Localizable _Azure_OAuthToken_Missing() {
//        return new Localizable(holder, "Azure_OAuthToken_Missing");
//    }
//
//    /**
//     * Key {@code //}: {@code CHECKSTYLE:OFF}.
//     *
//     * @return
//     *     {@code CHECKSTYLE:OFF}
//     */
//    public static String __() {
//        return holder.format("//");
//    }
//
//    /**
//     * Key {@code //}: {@code CHECKSTYLE:OFF}.
//     *
//     * @return
//     *     {@code CHECKSTYLE:OFF}
//     */
//    public static Localizable ___() {
//        return new Localizable(holder, "//");
//    }
//
//    /**
//     * Key {@code Azure_SubscriptionID_Missing}: {@code Error: Subscription
//
//     * ID is missing.}.
//     *
//     * @return
//     *     {@code Error: Subscription ID is missing.}
//     */
//    public static String Azure_SubscriptionID_Missing() {
//        return holder.format("Azure_SubscriptionID_Missing");
//    }
//
//    /**
//     * Key {@code Azure_SubscriptionID_Missing}: {@code Error: Subscription
//
//     * ID is missing.}.
//     *
//     * @return
//     *     {@code Error: Subscription ID is missing.}
//     */
//    public static Localizable _Azure_SubscriptionID_Missing() {
//        return new Localizable(holder, "Azure_SubscriptionID_Missing");
//    }
//
//    /**
//     * Key {@code Azure_CantValidate}: {@code The provided credentials are
//
//     * not valid}.
//     *
//     * @return
//     *     {@code The provided credentials are not valid}
//     */
//    public static String Azure_CantValidate() {
//        return holder.format("Azure_CantValidate");
//    }
//
//    /**
//     * Key {@code Azure_CantValidate}: {@code The provided credentials are
//
//     * not valid}.
//     *
//     * @return
//     *     {@code The provided credentials are not valid}
//     */
//    public static Localizable _Azure_CantValidate() {
//        return new Localizable(holder, "Azure_CantValidate");
//    }
//
//    /**
//     * Key {@code Azure_ClientID_Missing}: {@code Error: Client ID is
//
//     * missing.}.
//     *
//     * @return
//     *     {@code Error: Client ID is missing.}
//     */
//    public static String Azure_ClientID_Missing() {
//        return holder.format("Azure_ClientID_Missing");
//    }
//
//    /**
//     * Key {@code Azure_ClientID_Missing}: {@code Error: Client ID is
//
//     * missing.}.
//     *
//     * @return
//     *     {@code Error: Client ID is missing.}
//     */
//    public static Localizable _Azure_ClientID_Missing() {
//        return new Localizable(holder, "Azure_ClientID_Missing");
//    }
//
//}



public class AzureCredentials extends BaseStandardCredentials {
    public static class ValidationException extends Exception {

        public ValidationException(String message) {
            super(message);
        }
    }

    public static class Constants {

        public static final String DEFAULT_MANAGEMENT_URL
                = "https://management.core.windows.net/";
        public static final String DEFAULT_AUTHENTICATION_ENDPOINT
                = "https://login.microsoftonline.com/";
        public static final String DEFAULT_RESOURCE_MANAGER_ENDPOINT
                = "https://management.azure.com/";
        public static final String DEFAULT_GRAPH_ENDPOINT
                = "https://graph.windows.net/";
        public static final String DEFAULT_OAUTH_PREFIX
                = "https://login.windows.net/<TenantId>";
    }

    public static class ServicePrincipal implements java.io.Serializable {

        private final Secret subscriptionId;
        private final Secret clientId;
        private final Secret clientSecret;
        private final Secret oauth2TokenEndpoint; //keeping this for backwards compatibility
        private final String serviceManagementURL;
        private final Secret tenant;
        private final String authenticationEndpoint;
        private final String resourceManagerEndpoint;
        private final String graphEndpoint;

        public String getSubscriptionId() {
            if (subscriptionId == null) {
                return "";
            } else {
                return subscriptionId.getPlainText();
            }
        }

        public String getClientId() {
            if (clientId == null) {
                return "";
            } else {
                return clientId.getPlainText();
            }
        }

        public String getClientSecret() {
            if (clientSecret == null) {
                return "";
            } else {
                return clientSecret.getPlainText();
            }
        }

        public String getTenant() {
            if (tenant == null || StringUtils.isBlank(tenant.getPlainText())) {
                if (oauth2TokenEndpoint != null) {
                    return ServicePrincipal.getTenantFromTokenEndpoint(
                            oauth2TokenEndpoint.getPlainText());
                } else {
                    return ServicePrincipal.getTenantFromTokenEndpoint("");
                }
            } else {
                return tenant.getPlainText();
            }
        }

        public String getServiceManagementURL() {
            if (serviceManagementURL == null) {
                return Constants.DEFAULT_MANAGEMENT_URL;
            } else {
                return serviceManagementURL;
            }
        }

        public String getAuthenticationEndpoint() {
            if (authenticationEndpoint == null) {
                return Constants.DEFAULT_AUTHENTICATION_ENDPOINT;
            } else {
                return authenticationEndpoint;
            }
        }

        public String getResourceManagerEndpoint() {
            if (resourceManagerEndpoint == null) {
                return Constants.DEFAULT_RESOURCE_MANAGER_ENDPOINT;
            } else {
                return resourceManagerEndpoint;
            }
        }

        public String getGraphEndpoint() {
            if (graphEndpoint == null) {
                return Constants.DEFAULT_GRAPH_ENDPOINT;
            } else {
                return graphEndpoint;
            }
        }

        public ServicePrincipal(
                String subscriptionId,
                String clientId,
                String clientSecret,
                String oauth2TokenEndpoint,
                String serviceManagementURL,
                String authenticationEndpoint,
                String resourceManagerEndpoint,
                String graphEndpoint) {
            this.subscriptionId = Secret.fromString(subscriptionId);
            this.clientId = Secret.fromString(clientId);
            this.clientSecret = Secret.fromString(clientSecret);
            this.oauth2TokenEndpoint = Secret.fromString(oauth2TokenEndpoint);
            this.tenant = Secret.fromString(ServicePrincipal.getTenantFromTokenEndpoint(
                    this.oauth2TokenEndpoint.getPlainText()));

            if (StringUtils.isBlank(serviceManagementURL)) {
                this.serviceManagementURL = Constants.DEFAULT_MANAGEMENT_URL;
            } else {
                this.serviceManagementURL = serviceManagementURL;
            }
            if (StringUtils.isBlank(authenticationEndpoint)) {
                this.authenticationEndpoint = Constants.DEFAULT_AUTHENTICATION_ENDPOINT;
            } else {
                this.authenticationEndpoint = authenticationEndpoint;
            }
            if (StringUtils.isBlank(resourceManagerEndpoint)) {
                this.resourceManagerEndpoint = Constants.DEFAULT_RESOURCE_MANAGER_ENDPOINT;
            } else {
                this.resourceManagerEndpoint = resourceManagerEndpoint;
            }
            if (StringUtils.isBlank(graphEndpoint)) {
                this.graphEndpoint = Constants.DEFAULT_GRAPH_ENDPOINT;
            } else {
                this.graphEndpoint = graphEndpoint;
            }
        }

        public ServicePrincipal() {
            this.subscriptionId = Secret.fromString("");
            this.clientId = Secret.fromString("");
            this.clientSecret = Secret.fromString("");
            this.oauth2TokenEndpoint = Secret.fromString("");
            this.tenant = Secret.fromString("");
            this.serviceManagementURL = Constants.DEFAULT_MANAGEMENT_URL;
            this.authenticationEndpoint = Constants.DEFAULT_AUTHENTICATION_ENDPOINT;
            this.resourceManagerEndpoint = Constants.DEFAULT_RESOURCE_MANAGER_ENDPOINT;
            this.graphEndpoint = Constants.DEFAULT_GRAPH_ENDPOINT;
        }

        public boolean isBlank() {
            return StringUtils.isBlank(subscriptionId.getPlainText())
                    || StringUtils.isBlank(clientId.getPlainText())
                    || StringUtils.isBlank(oauth2TokenEndpoint.getPlainText())
                    || StringUtils.isBlank(clientSecret.getPlainText());
        }

        public boolean validate() throws ValidationException {
            if (StringUtils.isBlank(subscriptionId.getPlainText())) {
                throw new ValidationException(Messages.Azure_SubscriptionID_Missing());
            }
            if (StringUtils.isBlank(clientId.getPlainText())) {
                throw new ValidationException(Messages.Azure_ClientID_Missing());
            }
            if (StringUtils.isBlank(clientSecret.getPlainText())) {
                throw new ValidationException(Messages.Azure_ClientSecret_Missing());
            }
            if (StringUtils.isBlank(oauth2TokenEndpoint.getPlainText())) {
                throw new ValidationException(Messages.Azure_OAuthToken_Missing());
            }
            if (StringUtils.isBlank(getTenant())) {
                throw new ValidationException(Messages.Azure_OAuthToken_Malformed());
            }

            try {
                final String credentialSubscriptionId = getSubscriptionId();
                Azure.Authenticated auth = Azure.authenticate(
                        new ApplicationTokenCredentials(
                                getClientId(),
                                getTenant(),
                                getClientSecret(),
//                                new AzureEnvironment(
//                                        getAuthenticationEndpoint(),
//                                        getServiceManagementURL(),
//                                        getResourceManagerEndpoint(),
//                                        getGraphEndpoint())
//                        )
                                AzureEnvironment.AZURE
                ));
                for (Subscription subscription : auth.subscriptions().list()) {
                    if (subscription.subscriptionId().equalsIgnoreCase(credentialSubscriptionId)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                throw new ValidationException(Messages.Azure_CantValidate());
            }
            throw new ValidationException(Messages.Azure_Invalid_SubscriptionId());
        }

        private static final int TOKEN_ENDPOINT_URL_ENDPOINT_POSTION = 3;

        private static String getTenantFromTokenEndpoint(String oauth2TokenEndpoint) {
            if (!oauth2TokenEndpoint.matches(
                    "https{0,1}://[a-zA-Z0-9\\.]*/[a-z0-9\\-]*/?.*$")) {
                return "";
            } else {
                String[] parts = oauth2TokenEndpoint.split("/");
                if (parts.length < TOKEN_ENDPOINT_URL_ENDPOINT_POSTION + 1) {
                    return "";
                } else {
                    return parts[TOKEN_ENDPOINT_URL_ENDPOINT_POSTION];
                }
            }
        }

    }

    private final ServicePrincipal data;

    @DataBoundConstructor
    public AzureCredentials(
            CredentialsScope scope,
            String id,
            String description,
            String subscriptionId,
            String clientId,
            String clientSecret,
            String oauth2TokenEndpoint,
            String serviceManagementURL,
            String authenticationEndpoint,
            String resourceManagerEndpoint,
            String graphEndpoint) {
        super(scope, id, description);
        data = new ServicePrincipal(
                subscriptionId,
                clientId,
                clientSecret,
                oauth2TokenEndpoint,
                serviceManagementURL,
                authenticationEndpoint,
                resourceManagerEndpoint,
                graphEndpoint);
    }

    public static AzureCredentials.ServicePrincipal getServicePrincipal(
            String credentialsId) {
        AzureCredentials creds = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        AzureCredentials.class,
                        Jenkins.getInstance(),
                        ACL.SYSTEM,
                        Collections.<DomainRequirement>emptyList()),
                CredentialsMatchers.withId(credentialsId));
        if (creds == null) {
            return new AzureCredentials.ServicePrincipal();
        }
        return creds.data;
    }

    public String getSubscriptionId() {
        return data.subscriptionId.getPlainText();
    }

    public String getClientId() {
        return data.clientId.getPlainText();
    }

    public String getClientSecret() {
        return data.clientSecret.getEncryptedValue();
    }

    public String getPlainClientSecret() {
        return data.clientSecret.getPlainText();
    }

    public String getTenant() {
        return data.getTenant();
    }

    public String getOauth2TokenEndpoint() {
        return data.oauth2TokenEndpoint.getPlainText();
    }

    public String getServiceManagementURL() {
        return data.serviceManagementURL;
    }

    public String getAuthenticationEndpoint() {
        return data.authenticationEndpoint;
    }

    public String getResourceManagerEndpoint() {
        return data.resourceManagerEndpoint;
    }

    public String getGraphEndpoint() {
        return data.graphEndpoint;

    }

    @Extension
    public static class DescriptorImpl
            extends BaseStandardCredentials.BaseStandardCredentialsDescriptor {
        private static final String SPID = "SPID";
        private static final String CLIENTID = "CLIENTID";
        private static final String SECRET = "SECRET";
        private static final String SUBS = "SUBS";
        private static final String TENANT = "TENANT";
        private String spId;
        private String clientId;
        private String clientSecret;
        private String subId;
        private String tenant;

        public String getClientId() {
            return clientId;
        }

        public static String getSPID() {
            return SPID;
        }

        public static String getSECRET() {
            return SECRET;
        }

        public static String getSUBS() {
            return SUBS;
        }

        public static String getTENANT() {
            return TENANT;
        }


        public static String getCLIENTID() {
            return CLIENTID;
        }

        public String getTenant() {
            return tenant;
        }

        @Override
        public String getDisplayName() {
            return "Microsoft Azure Service Principal";
        }

        public String getDefaultServiceManagementURL() {
            return Constants.DEFAULT_MANAGEMENT_URL;
        }

        public String getDefaultAuthenticationEndpoint() {
            return Constants.DEFAULT_AUTHENTICATION_ENDPOINT;
        }

        public String getDefaultResourceManagerEndpoint() {
            return Constants.DEFAULT_RESOURCE_MANAGER_ENDPOINT;
        }

        public String getDefaultGraphEndpoint() {
            return Constants.DEFAULT_GRAPH_ENDPOINT;
        }

        public String getDefaultOAuthPrefix() {
            return Constants.DEFAULT_OAUTH_PREFIX;
        }

        public FormValidation doVerifyConfiguration(
                @QueryParameter String subscriptionId,
                @QueryParameter String clientId,
                @QueryParameter String clientSecret,
                @QueryParameter String oauth2TokenEndpoint,
                @QueryParameter String serviceManagementURL,
                @QueryParameter String authenticationEndpoint,
                @QueryParameter String resourceManagerEndpoint,
                @QueryParameter String graphEndpoint) {

            AzureCredentials.ServicePrincipal servicePrincipal
                    = new AzureCredentials.ServicePrincipal(
                    subscriptionId,
                    clientId,
                    clientSecret,
                    oauth2TokenEndpoint,
                    serviceManagementURL,
                    authenticationEndpoint,
                    resourceManagerEndpoint,
                    graphEndpoint);
            try {
                servicePrincipal.validate();
            } catch (ValidationException e) {
                return FormValidation.error(e.getMessage());
            }

            return FormValidation.ok(Messages.Azure_Config_Success());
        }

        public String doMyFill() throws JSONException {
            JSONObject json = new JSONObject();
            json.put(SPID, spId);
            json.put(CLIENTID, clientId);
            json.put(SECRET, clientSecret);
            json.put(SUBS, subId);
            json.put(TENANT, tenant);
            return json.toString();
        }

        public FormValidation doGenerateServicePrincipal(@QueryParameter final String subscriptions) throws ExecutionException, IOException, JSONException {
            System.out.println("doGenerateServicePrincipal:");
            Jenkins jenkins = Jenkins.getInstance();
            if (jenkins == null) {
                throw new RuntimeException("Jenkins is not started yet.");
            }
            SecurityRealm realm = jenkins.getSecurityRealm();
            if (realm instanceof AzureSecurityRealm) {
                AzureSecurityRealm azureSecurityRealm = (AzureSecurityRealm) realm;
                clientId = azureSecurityRealm.getClientid();
                clientSecret = azureSecurityRealm.getClientsecret();
                tenant = azureSecurityRealm.getTenant();
                int beg = subscriptions.lastIndexOf('(') + 1;
                int end = subscriptions.lastIndexOf(')');
                String subscriptionID = subscriptions.substring(beg, end);
                subId = subscriptionID;
                AzureApiToken token = AzureAuthenticationToken.getAppOnlyToken();
                String appOnlyAccessToken = token.getToken();
                // get service principal oid
                AzureResponse<String> spResponse = AzureAdApi.getServicePrincipalIdByAppId(tenant, clientId, appOnlyAccessToken);
                if (!spResponse.isSuccess())
                    return FormValidation.error(spResponse.getResponseContent());
                spId = spResponse.get();

                // get user token
                Authentication auth = Jenkins.getAuthentication();
                if (!(auth instanceof AzureAuthenticationToken))
                    return FormValidation.error("Unauthentication");
                AzureAuthenticationToken azureAuth = (AzureAuthenticationToken) auth;
                String userToken = azureAuth.getAzureRmToken().getToken();

                // get role id
                AzureResponse<String> roleResponse = AzureAdApi.getAzureRbacRoleId(subId, userToken);
                if (!roleResponse.isSuccess())
                    return FormValidation.error(roleResponse.getResponseContent());
                String roleId = roleResponse.get();
                AzureResponse assignResult = AzureAdApi.assginRbacRoleToServicePrincipal(subId, userToken, roleId, spId);
                if (!assignResult.isSuccess() && assignResult.getStatusCode() != 409)
                    return FormValidation.error(assignResult.getResponseContent());
                if (assignResult.getStatusCode() == assignResult.getSuccessCode())
                    return FormValidation.ok("Successfully generate service principal");
                else if (assignResult.getStatusCode() == 409)
                    return FormValidation.ok("The role assignment already exists. Use the existing service principal.");
            }
            return FormValidation.error("Fail to get Security Realm");
        }

        public ListBoxModel doFillSubscriptionsItems() throws IOException, JSONException {
            ListBoxModel model = new ListBoxModel();

            // get user token
            Authentication auth = Jenkins.getAuthentication();
            if (!(auth instanceof AzureAuthenticationToken))
                return null;
            AzureAuthenticationToken azureAuth = (AzureAuthenticationToken) auth;
            String userToken = azureAuth.getAzureRmToken().getToken();

            AzureResponse<Map<String, String>> response = AzureAdApi.getSubscriptions(userToken);
            if (!response.isSuccess()) {
                return null;
            }

            Map<String, String> subscriptions = response.get();
            for (Map.Entry<String, String> subscription : subscriptions.entrySet()) {
                model.add(subscription.getValue() + " (" + subscription.getKey() + ")");
            }
            if (model.isEmpty()) {
                model.add("You don't have any subscription!");
                return model;
            }
            return model;
        }





    }
}
