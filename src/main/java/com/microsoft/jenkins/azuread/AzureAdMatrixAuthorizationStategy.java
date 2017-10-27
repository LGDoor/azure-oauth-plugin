package com.microsoft.jenkins.azuread;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.implementation.ADGroupInner;
import com.microsoft.azure.management.graphrbac.implementation.UserInner;
import com.microsoft.jenkins.azuread.api.AzureObject;
import com.microsoft.jenkins.azuread.api.AzureObjectType;
import com.microsoft.jenkins.azuread.scribe.AzureToken;
import hudson.Extension;
import hudson.model.AutoCompletionCandidates;
import hudson.model.Descriptor;
import hudson.security.AuthorizationStrategy;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.SecurityRealm;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

            SecurityRealm realm = Jenkins.getActiveInstance().getSecurityRealm();
            if (!(realm instanceof AzureSecurityRealm)) return null;
            AzureTokenCredentials cred = ((AzureSecurityRealm) realm).getAzureCredential();

            System.out.println("search users with prefix: " + value);
            Azure.Authenticated authenticated = Azure.authenticate(cred);
            PagedList<UserInner> matchedUsers = authenticated.activeDirectoryUsers()
                    .inner().list("startswith(displayName,'" + value +"')");
            Stream<AzureObject> stream = matchedUsers.stream().map(u -> new AzureObject(u.objectId(), u.displayName()));

            if (!matchedUsers.hasNextPage()) {
                System.out.println("search groups with prefix " + value);
                PagedList<ADGroupInner> matchedGroups = authenticated.activeDirectoryGroups()
                        .inner().list("startswith(displayName,'" + value +"')");
                stream = Stream.concat(stream,
                        matchedGroups.stream().map(g -> new AzureObject(g.objectId(), g.displayName())));
            }
            List<AzureObject> candidates = stream.limit(20).collect(Collectors.toList());

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
