package com.microsoft.jenkins.azuread.api;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.microsoft.jenkins.azuread.*;
import com.microsoft.jenkins.azuread.scribe.AzureToken;
import hudson.security.SecurityRealm;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.json.JSONException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by albertxavier on 2017/9/7.
 */
public class AzureCachePool {
    public static final TimeUnit CACHE_EXPIRY = TimeUnit.HOURS;
    private static final Logger LOGGER = Logger.getLogger(AzureCachePool.class.getName());
    private static final Cache<String, Set<String>> belongingGroupsByOid =
            CacheBuilder.newBuilder().expireAfterAccess(1, CACHE_EXPIRY).build();
    private static final Cache<AzureObjectType, Set<AzureObject>> allAzureObjects =
            CacheBuilder.newBuilder().expireAfterAccess(1, CACHE_EXPIRY).build();

    public static Set<String> getBelongingGroupsByOid(String oid) throws IOException, JSONException, ExecutionException {

//        if (Constants.DEBUG == true) {
//            Utils.TimeUtil.setBeginDate();
//
//            Authentication auth = Jenkins.getAuthentication();
//            if (!(auth instanceof AzureAuthenticationToken)) return new HashSet<String>();
////            String aadAccessToken = ((AzureAuthenticationToken) auth).getAzureAdToken().getToken();
//            AzureToken accessToken = AzureAuthenticationToken.getAppOnlyToken();
//            AzureResponse<Set<String>> res = AzureAdApiClient.getGroupsByUserId(accessToken.getToken());
//            if (!res.isSuccess()) {
//                System.out.println("getBelongingGroupsByOid: set is empty");
//                System.out.println("error: " + res.getResponseContent());
//                return new HashSet<String>();
//            }
//            Utils.TimeUtil.setEndDate();
//            System.out.println("getBelongingGroupsByOid time (debug) = " + Utils.TimeUtil.getTimeDifference() + "ms");
//            System.out.println("getBelongingGroupsByOid: set = " + res.<Set<String>>get());
//            return res.get();
//        }


        try {
            Set<String> set = belongingGroupsByOid.get(oid, () -> {
                Utils.TimeUtil.setBeginDate();
                Authentication auth = Jenkins.getAuthentication();
                if (!(auth instanceof AzureAuthenticationToken)) return new HashSet<>();
                AzureToken token = AzureAuthenticationToken.getAppOnlyToken();
                String oid1 = ((AzureAuthenticationToken) auth).getAzureAdUser().getObjectID();
                AzureResponse<Set<String>> res = AzureAdApiClient.getGroupsByUserId(token.getAccessToken(), oid1);
                if (!res.isSuccess()) {
                    System.out.println("getBelongingGroupsByOid: set is empty");
                    System.out.println("error: " + res.getResponseContent());
                    return new HashSet<>();
                }
                Utils.TimeUtil.setEndDate();
                System.out.println("getBelongingGroupsByOid time (debug) = " + Utils.TimeUtil.getTimeDifference() + "ms");
                System.out.println("getBelongingGroupsByOid: set = " + res.<Set<String>>get());
                return res.get();
            });
            if (Constants.DEBUG) {
                belongingGroupsByOid.invalidate(oid);
            }
            return set;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }


    }


    public static Set<AzureObject> getAllAzureObjects(final AzureObjectType type) {
        try {
            Set<AzureObject> set = allAzureObjects.get(type, new Callable<Set<AzureObject>>() {
                @Override
                public Set<AzureObject> call() throws Exception {
                    System.out.println("get new azure obj");
//                    Authentication auth = Jenkins.getAuthentication();
//                    if (!(auth instanceof AzureAuthenticationToken)) return null;
//                    String aadAccessToken = ((AzureAuthenticationToken) auth).getAzureAdToken().getToken();
                    SecurityRealm realm = Utils.JenkinsUtil.getSecurityRealm();
                    if (!(realm instanceof AzureSecurityRealm)) return null;
//                    AzureSecurityRealm azureRealm = (AzureSecurityRealm) realm;
//                    String clientId = azureRealm.getClientid();
//                    String clientSecret = azureRealm.getClientsecret();
//                    String tenant = azureRealm.getTenant();
                    AzureToken appOnlyToken = AzureAuthenticationToken.getAppOnlyToken();
                    if (appOnlyToken == null) return null;
                    AzureResponse<Set<AzureObject>> res = AzureAdApiClient.getAllAzureObjects(appOnlyToken.getAccessToken(), type);
                    if (res.isFail()) return null;
                    return res.get();
                }
            });
            if (Constants.DEBUG) allAzureObjects.invalidate(type);
            return set;
        } catch (ExecutionException e) {
            e.printStackTrace();
//            invalidateAllObject(type);
            return null;
        }
    }


    public static void invalidateBelongingGroupsByOid(String userId) {
        belongingGroupsByOid.invalidate(userId);
    }

    public static void invalidateAllObject(AzureObjectType type) {
        allAzureObjects.invalidate(type);
    }
}
