package com.microsoft.jenkins.azuread;

//
//import com.microsoft.azure.oauth.client.AzureActiveDirectoryApiService;
//import com.microsoft.azure.oauth.client.AzureActiveDirectoryConfig;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.microsoft.jenkins.azuread.api.AzureAdApiClient;
import com.microsoft.jenkins.azuread.api.AzureCachePool;
import com.microsoft.jenkins.azuread.scribe.AzureApi;
import com.microsoft.jenkins.azuread.scribe.AzureToken;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;


import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.User;
import hudson.security.GroupDetails;
import hudson.security.SecurityRealm;
import hudson.security.UserMayOrMayNotExistException;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.kohsuke.stapler.*;
import org.kohsuke.stapler.Header;
import org.kohsuke.stapler.HttpResponse;
import org.springframework.dao.DataAccessException;
//import sun.misc.Cache;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AzureSecurityRealm extends SecurityRealm {

    private static final String REFERER_ATTRIBUTE = AzureSecurityRealm.class.getName() + ".referer";
    private static final String ACCESS_TOKEN_ATTRIBUTE = AzureSecurityRealm.class.getName() + ".access_token";
    private static final Logger LOGGER = Logger.getLogger(AzureSecurityRealm.class.getName());
    private static final String azurePortalUrl = "https://ms.portal.azure.com";
    private Secret clientid;
    private Secret clientsecret;
    private Secret tenant;

    public String getClientIdSecret() {
        return clientid.getEncryptedValue();
    }

    public String getClientSecretSecret() {
        return clientsecret.getEncryptedValue();
    }

    public String getTenantSecret() {
        return tenant.getEncryptedValue();
    }

    public String getAzurePortalUrl() {
        return azurePortalUrl;
    }

    public String getClientid() {
        return clientid.getPlainText();
    }

    public void setClientid(String clientid) {
        this.clientid = Secret.fromString(clientid);
    }

    public String getClientsecret() {
        return clientsecret.getPlainText();
    }

    public void setClientsecret(String clientsecret) {
        this.clientsecret = Secret.fromString(clientsecret);
    }

    public String getTenant() {
        return tenant.getPlainText();
    }

    public void setTenant(String tenant) {
        this.tenant = Secret.fromString(tenant);
    }

    OAuth20Service getOAuthService() {
        OAuth20Service service = new ServiceBuilder(clientid.getPlainText()).apiSecret(clientsecret.getPlainText())
                .callback(getRootUrl() + "/securityRealm/finishLogin")
                .build(AzureApi.instance(Constants.DEFAULT_GRAPH_ENDPOINT, this.getTenant()));
        return service;
    }

    private String getRootUrl() {
        Jenkins jenkins = Jenkins.getActiveInstance();
        return StringUtils.stripEnd(jenkins.getRootUrl(), "/");
    }

    @DataBoundConstructor
    public AzureSecurityRealm(String tenant, String clientid, String clientsecret)
            throws ExecutionException, IOException, InterruptedException {
        super();
        this.clientid = Secret.fromString(clientid);
        this.clientsecret = Secret.fromString(clientsecret);
        this.tenant = Secret.fromString(tenant);

        // update app only token
        AzureAuthenticationToken.refreshAppOnlyToken();
    }



    public AzureSecurityRealm() {
        super();
        LOGGER.log(Level.FINE, "AzureSecurityRealm()");
    }



    public HttpResponse doCommenceLogin(StaplerRequest request, @Header("Referer") final String referer) throws IOException {
        request.getSession().setAttribute(REFERER_ATTRIBUTE, referer);
        OAuth20Service service = getOAuthService();
        Utils.TimeUtil.setBeginDate();
        return new HttpRedirect(service.getAuthorizationUrl());
    }

    public HttpResponse doFinishLogin(StaplerRequest request) throws Exception {
        Utils.TimeUtil.setEndDate();
        System.out.println("Requesting oauth code time = " + Utils.TimeUtil.getTimeDifference() + " ms");
        String code = request.getParameter("code");
//        int tokenType = Integer.parseInt(request.getParameter("state"));

        if (StringUtils.isBlank(code)) {
            LOGGER.log(Level.SEVERE, "doFinishLogin() code = null");
            return HttpResponses.redirectTo(this.getRootUrl() + AzureAuthFailAction.POST_LOGOUT_URL);
        }

        OAuth20Service service = getOAuthService();

        Utils.TimeUtil.setBeginDate();
        OAuth2AccessToken accessToken = service.getAccessToken(code);
        Utils.TimeUtil.setEndDate();
        System.out.println("Requesting access token time = " + Utils.TimeUtil.getTimeDifference() + " ms");

        if (accessToken != null) {
            AzureAuthenticationToken auth = null;
            if (accessToken instanceof AzureToken) {
                AzureToken azureToken = (AzureToken)accessToken;
                auth = new AzureAuthenticationToken(azureToken, clientid.getPlainText(), clientsecret.getPlainText(), 0);
//                if (tokenType == 0) {
//                    OAuthService service1 = getOAuthService(Constants.DEFAULT_GRAPH_ENDPOINT);
//                    HttpHelper.sendGet(service1.getAuthorizationUrl(EMPTY_TOKEN), null);
//                }
            }
            else
                return HttpResponses.redirectToContextRoot(); // TODO: redirect to token fail page

            SecurityContextHolder.getContext().setAuthentication(auth);

            User u = User.current();

            if (u != null) {
                String description = generateDescription(auth);
                u.setDescription(description);
                u.setFullName(auth.getName());
            }

        } else {
            LOGGER.log(Level.SEVERE, "doFinishLogin() accessToken = null");
        }

//        test(tenant, accessToken);

        // redirect to referer
        String referer = (String) request.getSession().getAttribute(REFERER_ATTRIBUTE);
        if (referer != null) {
            return HttpResponses.redirectTo(referer);
        } else {
            return HttpResponses.redirectToContextRoot();
        }
    }

    @Override
    protected String getPostLogOutUrl(StaplerRequest req, Authentication auth) {
        // if we just redirect to the root and anonymous does not have Overall read then we will start a login all over again.
        // we are actually anonymous here as the security context has been cleared

        // invalidateBelongingGroupsByOid
        if (auth instanceof AzureAuthenticationToken) {
            AzureAuthenticationToken azureToken = (AzureAuthenticationToken) auth;
            String oid = azureToken.getAzureAdUser().getObjectID();
            AzureCachePool.invalidateBelongingGroupsByOid(oid);
            System.out.println("invalidateBelongingGroupsByOid cache entry when sign out");
        }
        Jenkins j = Jenkins.getInstance();
        assert j != null;
        if (j.hasPermission(Jenkins.READ)) {
            return super.getPostLogOutUrl(req, auth);
        }
        return req.getContextPath()+ "/" + AzureLogoutAction.POST_LOGOUT_URL;
    }

    @Override
    public SecurityComponents createSecurityComponents() {
        return new SecurityComponents(new AuthenticationManager() {
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                if (authentication instanceof AzureToken) {
                    return authentication;
                }

                throw new BadCredentialsException("Unexpected authentication type: " + authentication);
            }
        }, new UserDetailsService() {
            public UserDetails loadUserByUsername(String username)  throws UserMayOrMayNotExistException, DataAccessException {
                throw new UserMayOrMayNotExistException("Cannot verify users in this context");
            }
        });
    }

//    @Override
//    public UserDetails loadUserByUsername(String userName) {
//        UserDetails result = null;
//        Authentication token = SecurityContextHolder.getContext().getAuthentication();
//        if (token == null) {
//            throw new UsernameNotFoundException("AzureAuthenticationToken = null, no known user: " + userName);
//        }
//        if (!(token instanceof AzureAuthenticationToken)) {
//          throw new UserMayOrMayNotExistException("Unexpected authentication type: " + token);
//        }
//        result = service.getUserByUsername(userName);
//        if (result == null) {
//            throw new UsernameNotFoundException("User does not exist for login: " + userName);
//        }
//        return result;
//    }

    @Override
    public GroupDetails loadGroupByGroupname(String groupName) {
        throw new UsernameNotFoundException("groups not supported");
    }

    @Override
    public boolean allowsSignup() {
        return false;
    }

    @Override
    public String getLoginUrl() {
        return "securityRealm/commenceLogin";
    }

    public static final class ConverterImpl implements Converter {

        public boolean canConvert(Class type) {
            return type == AzureSecurityRealm.class;
        }

        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {

            AzureSecurityRealm realm = (AzureSecurityRealm) source;

            writer.startNode("clientid");
            writer.setValue(realm.getClientIdSecret());
            writer.endNode();

            writer.startNode("clientsecret");
            writer.setValue(realm.getClientsecret());
            writer.endNode();

            writer.startNode("tenant");
            writer.setValue(realm.getTenantSecret());
            writer.endNode();
        }

        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {

            String node = reader.getNodeName();

            reader.moveDown();

            AzureSecurityRealm realm = new AzureSecurityRealm();

            node = reader.getNodeName();

            String value = reader.getValue();

            setValue(realm, node, value);

            reader.moveUp();

            //

            reader.moveDown();

            node = reader.getNodeName();

            value = reader.getValue();

            setValue(realm, node, value);

            reader.moveUp();

            //

            reader.moveDown();

            node = reader.getNodeName();

            value = reader.getValue();

            setValue(realm, node, value);

            reader.moveUp();



            if (reader.hasMoreChildren()) {
                reader.moveDown();

                node = reader.getNodeName();

                value = reader.getValue();

                setValue(realm, node, value);

                reader.moveUp();
            }
            return realm;
        }

        private void setValue(AzureSecurityRealm realm, String node, String value) {

            if (node.equalsIgnoreCase("clientid")) {
                realm.setClientid(value);
            } else if (node.equalsIgnoreCase("clientsecret")) {
                realm.setClientsecret(value);
            } else if (node.equalsIgnoreCase("tenant")) {
                realm.setTenant(value);
            } else {
                throw new ConversionException("invalid node value = " + node);
            }

        }
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<SecurityRealm> {

        @Override
        public String getHelpFile() {
            return "";
        }

        @Override
        public String getDisplayName() {
            return "Azure OAuth Plugin";
        }

        public DescriptorImpl() {
            super();
        }

        public DescriptorImpl(Class<? extends SecurityRealm> clazz) {
            super(clazz);
        }

        public FormValidation doVerifyConfiguration(@QueryParameter final String clientid,
                                                    @QueryParameter final String clientsecret,
                                                    @QueryParameter final String tenant) throws IOException, JSONException, ExecutionException {


            org.apache.http.HttpResponse response = AzureAdApiClient.getAppOnlyAccessTokenResponce(clientid, clientsecret, tenant);
            int statusCode = HttpHelper.getStatusCode(response);
            String content = HttpHelper.getContent(response);
            if (statusCode != 200) {
                return FormValidation.error(content);
            }

            return FormValidation.ok("Successfully verified");
        }
    }

    private String generateDescription(Authentication auth) {
        if (auth instanceof AzureAuthenticationToken) {
            AzureAdUser user = ((AzureAuthenticationToken) auth).getAzureAdUser();
            StringBuffer description  = new StringBuffer("Azure Active Directory User\n\n");
            description.append("Given Name: " + user.getGivenName() + "\n");
            description.append("Family Name: " + user.getFamilyName() + "\n");
            description.append("Unique Principal Name: " + user.getUniqueName() + "\n");
            description.append("Object ID: " + user.getObjectID() + "\n");
            description.append("Tenant ID: " + user.getTenantID() + "\n");
            return description.toString();
        }

        return "";
    }

}
