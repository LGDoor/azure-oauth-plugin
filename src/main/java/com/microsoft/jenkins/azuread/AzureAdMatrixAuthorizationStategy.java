package com.microsoft.jenkins.azuread;

import com.microsoft.jenkins.azuread.api.AzureCachePool;
import com.microsoft.jenkins.azuread.api.AzureObject;
import com.microsoft.jenkins.azuread.api.AzureObjectType;
import com.microsoft.jenkins.azuread.scribe.AzureToken;
import hudson.Extension;
import hudson.model.AutoCompletionCandidates;
import hudson.model.Descriptor;
import hudson.security.AuthorizationStrategy;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.SecurityRealm;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class AzureAdMatrixAuthorizationStategy extends GlobalMatrixAuthorizationStrategy {

    @Extension
    public static final Descriptor<AuthorizationStrategy> DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends GlobalMatrixAuthorizationStrategy.DescriptorImpl {
        @Override
        protected GlobalMatrixAuthorizationStrategy create() {
            return new AzureAdMatrixAuthorizationStategy();
        }

        @Override
        @Nonnull
        public String getDisplayName() {
            return "Azure AD Matrix";
        }

        public AutoCompletionCandidates doAutoCompleteUserOrGroup(@QueryParameter String value)
                throws ExecutionException, IOException, InterruptedException {
            if (StringUtils.isEmpty(value))
                return null;
            AutoCompletionCandidates c = new AutoCompletionCandidates();

            SecurityRealm realm = Utils.JenkinsUtil.getSecurityRealm();
            if (!(realm instanceof AzureSecurityRealm)) return null;
            AzureToken appOnlyToken = AzureAuthenticationToken.getAppOnlyToken();
            Set<AzureObject> candidates = new HashSet<>();
            System.out.println("get all users");
            Set<AzureObject> users = AzureCachePool.getAllAzureObjects(AzureObjectType.User);
            if (users != null && !users.isEmpty()) candidates.addAll(users);
            System.out.println("get all groups");
            Set<AzureObject>  groups = AzureCachePool.getAllAzureObjects(AzureObjectType.Group);
            if (groups != null && !groups.isEmpty()) candidates.addAll(groups);

            for (AzureObject obj : candidates) {
                String candadateText = MessageFormat.format("{0} ({1})",obj.getDisplayName(), obj.getObjectId());
                if (StringUtils.startsWithIgnoreCase(candadateText, value))
                    c.add(candadateText);
            }

            return c;
        }
    };

    @Restricted(DoNotUse.class)
    public static class ConverterImpl extends GlobalMatrixAuthorizationStrategy.ConverterImpl {

        @Override
        public GlobalMatrixAuthorizationStrategy create() {
            return new AzureAdMatrixAuthorizationStategy();
        }

        @Override
        @SuppressWarnings("rawtypes")
        public boolean canConvert(Class type) {
            return type == AzureAdMatrixAuthorizationStategy.class;
        }
    }
}
