package com.microsoft.jenkins.azuread;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.jenkins.azuread.api.AzureAdApiClient;
import com.microsoft.jenkins.azuread.api.AzureObject;
import com.microsoft.jenkins.azuread.api.AzureObjectType;
import com.microsoft.jenkins.azuread.api.AzureResponse;
import com.microsoft.jenkins.azuread.scribe.AzureToken;
import hudson.security.SecurityRealm;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.json.JSONException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by albertxavier on 2017/9/7.
 */
public class AzureCachePool {
    private static final Logger LOGGER = Logger.getLogger(AzureCachePool.class.getName());
    private static final Cache<String, Collection<String>> belongingGroupsByOid =
            CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
    private static final Cache<AzureObjectType, Set<AzureObject>> allAzureObjects =
            CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();

    public static Collection<String> getBelongingGroupsByOid(String oid) throws IOException, JSONException, ExecutionException {
        try {
            Collection<String> result = belongingGroupsByOid.get(oid, () -> {
                Utils.TimeUtil.setBeginDate();
                AzureSecurityRealm securityRealm = (AzureSecurityRealm) Jenkins.getActiveInstance().getSecurityRealm();
                List<String> groups = Azure.authenticate(securityRealm.getAzureCredential())
                        .activeDirectoryUsers().inner().getMemberGroups(oid, false);
                Azure.authenticate(securityRealm.getAzureCredential()).activeDirectoryUsers().list().loadAll();

                Utils.TimeUtil.setEndDate();
                System.out.println("getBelongingGroupsByOid time (debug) = " + Utils.TimeUtil.getTimeDifference() + "ms");
                System.out.println("getBelongingGroupsByOid: set = " + groups);
                return groups;
            });
            if (Constants.DEBUG) {
                belongingGroupsByOid.invalidate(oid);
            }
            return result;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }


    }


//    public static Set<AzureObject> getAllAzureObjects(final AzureObjectType type) {
//        try {
//            Set<AzureObject> set = allAzureObjects.get(type, () -> {
//                System.out.println("get new azure obj");
//                SecurityRealm realm = Utils.JenkinsUtil.getSecurityRealm();
//                if (!(realm instanceof AzureSecurityRealm)) return null;
//                AzureToken appOnlyToken = AzureAuthenticationToken.getAppOnlyToken();
//                if (appOnlyToken == null) return null;
//                AzureResponse<Set<AzureObject>> res = AzureAdApiClient.getAllAzureObjects(appOnlyToken.getAccessToken(), type);
//                if (res.isFail()) return null;
//                return res.get();
//            });
//            if (Constants.DEBUG) allAzureObjects.invalidate(type);
//            return set;
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }


    public static void invalidateBelongingGroupsByOid(String userId) {
        belongingGroupsByOid.invalidate(userId);
    }

    public static void invalidateAllObject(AzureObjectType type) {
        allAzureObjects.invalidate(type);
    }
}
