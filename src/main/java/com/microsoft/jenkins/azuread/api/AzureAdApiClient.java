package com.microsoft.jenkins.azuread.api;

import com.microsoft.jenkins.azuread.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by t-wanl on 8/24/2017.
 */
public class AzureAdApiClient {
    /*
        get all members (user ID, user type) from group list
     */



    public static final String RESPONSE_CONTENT_KEY = "responseContent";
    public static final String STATUS_CODE_KEY = "statusCode";
    public static Map<String, String> getGroupMembers(List<String> groupList, String accessToken, String tenant)
            throws IOException, JSONException {
        Map<String, String> members = new HashMap<String, String>();
        for (String groupID : groupList) {
            members.putAll(getGroupMembers(groupID, accessToken, tenant, true));
        }
        return members;
    }

    /*
        get group members from single group
     */
    public static Map<String, String> getGroupMembers(String groupID, String accessToken, String tenant, boolean recursive) throws IOException, JSONException {
        Map<String, String> members = new HashMap<String, String>();

        String url = String.format(Constants.DEFAULT_GRAPH_ENDPOINT + Constants.DEFAULT_GRAPH_VERSION + "%s/groups/%s/members?api-version=1.6", tenant, groupID);
        HttpResponse response = HttpHelper.sendGet(url, accessToken);
        String responseContent = HttpHelper.getContent(response);

        JSONObject json = new JSONObject(responseContent);
        JSONArray memberArray = json.getJSONArray("value");
        for (int i = 0; i < memberArray.length(); i++) {
            JSONObject member =  memberArray.getJSONObject(i);
            if (recursive && member.getString("objectType").equals("Group")) {
                String subGroupID = member.getString("objectId");
                Map<String, String> subMembers = getGroupMembers(subGroupID, accessToken, tenant, recursive);
                members.putAll(subMembers);
            } else if (member.getString("objectType").equals("User")) {
                String userID = member.getString("objectId");
                members.put(userID, "User");
            }
        }

        return members;
    }

    public static Map<String, String> getAllAadGroupsNameIdPair(String accessToken, String tenant)
            throws IOException, JSONException {
        String url = String.format(Constants.DEFAULT_GRAPH_ENDPOINT + Constants.DEFAULT_GRAPH_VERSION + "%s/groups?api-version=1.6", tenant);

        HttpResponse response = HttpHelper.sendGet(url, accessToken);
        String responseContent = HttpHelper.getContent(response);

        JSONObject json = new JSONObject(responseContent);
        JSONArray groups = json.getJSONArray("value");
        Map<String, String> groupNameId = new HashMap<String, String>();
        for (int i = 0; i < groups.length(); i++) {
            String aadGroupName = groups.getJSONObject(i).getString("displayName");
            String aadGroupId = groups.getJSONObject(i).getString("objectId");
            groupNameId.put(aadGroupName, aadGroupId);
        }
        return groupNameId;
    }

    public static Set<String> getAllAadGroupsId(String accessToken, String tenant)
            throws IOException, JSONException {
        String url = String.format(Constants.DEFAULT_GRAPH_ENDPOINT + Constants.DEFAULT_GRAPH_VERSION + "%s/groups?api-version=1.6", tenant);

        HttpResponse response = HttpHelper.sendGet(url, accessToken);
        String responseContent = HttpHelper.getContent(response);

        JSONObject json = new JSONObject(responseContent);
        JSONArray groups = json.getJSONArray("value");
        Set<String> groupId = new HashSet<String>();
        for (int i = 0; i < groups.length(); i++) {
            String aadGroupId = groups.getJSONObject(i).getString("objectId");
            groupId.add(aadGroupId);
        }
        return groupId;
    }

    /*
    * delegate permission: Directory.AccessAsUser.All
    * */
    public static AzureResponse<Set<String>> getGroupsByUserId(String accessToken, String oid) throws IOException, JSONException {
//        Utils.TimeUtil.setBeginDate();
        String url = String.format(Constants.DEFAULT_GRAPH_ENDPOINT + Constants.DEFAULT_GRAPH_VERSION + "users/%s/getMemberGroups", oid);
//        String url = String.format(Constants.DEFAULT_GRAPH_ENDPOINT + Constants.DEFAULT_GRAPH_VERSION + "%s/users/%s/getMemberGroups?client-version=1.6", tenant, userID);
//        System.out.println("getGroupsByUserId url = \n" + url);
        JSONObject body = new JSONObject();
        body.put("securityEnabledOnly", false);
        HttpResponse response = HttpHelper.sendPost(url, accessToken, body, ContentType.APPLICATION_JSON);

        return new AzureResponse<Set<String>>(response, 200) {
            @Override
            public Set<String> perform(String responseContent) throws JSONException {
//                public Set<String> getGroupsByUserId() throws JSONException {
                Utils.TimeUtil.setEndDate();

                if (!isSuccess()) {
                    System.out.println("Require delegate permission: Directory.AccessAsUser.All");
                    return null;
                }
                    JSONObject json = new JSONObject(responseContent);
                    JSONArray groups = json.getJSONArray("value");

                    Set<String> groupId = new HashSet<String>();
                    for (int i = 0; i < groups.length(); i++) {
                        String aadGroupId = groups.getString(i);
                        groupId.add(aadGroupId);
                    }
                    Utils.TimeUtil.setEndDate();
                    System.out.println("time for getGroupsByUserId = " + Utils.TimeUtil.getTimeDifference() + " ms");
                    return groupId;
//                }
            }
        };




    }


    public static HttpResponse getAppOnlyAccessTokenResponce(String clientID, String clientSecret, String tenant) throws IOException {
        // try to get app-only token
        String url = String.format(Constants.DEFAULT_AUTHENTICATION_ENDPOINT+ "%s/oauth2/token", tenant);

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("client_id", (clientID)));
        urlParameters.add(new BasicNameValuePair("resource", (Constants.DEFAULT_GRAPH_ENDPOINT)));
        urlParameters.add(new BasicNameValuePair("client_secret", (clientSecret)));
        urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
        HttpEntity formEntity=new UrlEncodedFormEntity(urlParameters,ContentType.APPLICATION_FORM_URLENCODED.getCharset());

        HttpResponse response = HttpHelper.sendPost(url, null, formEntity, ContentType.APPLICATION_FORM_URLENCODED);

        return response;
    }

    public static HttpResponse getUserResponse(String tenant, String userID, String accessToken) throws IOException {
        return getAadObjectResponse(tenant, userID, accessToken, "users");
    }

    public static HttpResponse getGroupResponse(String tenant, String groupID, String accessToken) throws IOException {
        return getAadObjectResponse(tenant, groupID, accessToken, "groups");
    }

    private static HttpResponse getAadObjectResponse(String tenant, String id, String accessToken, String type) throws IOException {
        String url = String.format(Constants.DEFAULT_GRAPH_ENDPOINT + Constants.DEFAULT_GRAPH_VERSION + "%s/%s/%s",
                tenant, type, URLEncoder.encode(id, "UTF-8"));
        HttpResponse response = HttpHelper.sendGet(url, accessToken);
        return response;
    }

    public static AzureResponse<String> getServicePrincipalIdByAppId(String tenant, final String appId, String accessToken) throws IOException, JSONException {
        String url = String.format(Constants.DEFAULT_GRAPH_ENDPOINT + Constants.BETA_GRAPH_VERSION + "servicePrincipals");
        HttpResponse response = HttpHelper.sendGet(url, accessToken);

        return new AzureResponse<String>(response, 200) {
            @Override
            public String perform(String responseContent) throws JSONException {
                if (!isSuccess()) return null;
                JSONObject json = new JSONObject(responseContent);
                JSONArray arr = json.getJSONArray("value");
                for (int i = 0; i < arr.length(); i++) {
                    if (appId.equals(arr.getJSONObject(i).getString("appId"))) {
                        return arr.getJSONObject(i).getString("id");
                    }
                }
                return null;

            }
        };
    }

    public static AzureResponse<String> getAzureRbacRoleId(String subscription, String accessToken) throws IOException, JSONException {
        String url = String.format(Constants.DEFAULT_RESOURCE_MANAGER_ENDPOINT + "subscriptions/%s/providers/Microsoft.Authorization/roleDefinitions?api-version=2015-07-01", subscription);
        HttpResponse response = HttpHelper.sendGet(url, accessToken);

        return new AzureResponse<String>(response, 200) {
            @Override
            public String perform(String responseContent) throws JSONException {
//                public String getRoleId() throws JSONException {
                    if (!isSuccess()) return null;
                    JSONObject json = new JSONObject(responseContent);
                    JSONArray roleList = json.getJSONArray("value");
                    //get Contributer id
                    for(int i = 0; i < roleList.length(); i++) {
                        System.out.println("role = " + roleList.getJSONObject(i).toString());
                        if (roleList.getJSONObject(i).getJSONObject("properties").getString("roleName").equals("Contributor")) {
                            return roleList.getJSONObject(i).getString("id");
                        }
                    }
                    return null;
//                }
            }
        };
    }

    public static AzureResponse<Void> assginRbacRoleToServicePrincipal(String subscription, String accessToken, String roleDefinitionId, String principalId) throws JSONException, IOException {
        UUID guid = java.util.UUID.randomUUID();
        String url = String.format(Constants.DEFAULT_RESOURCE_MANAGER_ENDPOINT + "subscriptions/%s/providers/microsoft.authorization/roleassignments/%s?api-version=2015-07-01", subscription, guid);

        JSONObject body = new JSONObject();
        JSONObject properties = new JSONObject();
        properties.put("roleDefinitionId", roleDefinitionId);
        properties.put("principalId", principalId);
        body.put("properties", properties);
        HttpResponse response = HttpHelper.sendPut(url, accessToken, body, ContentType.APPLICATION_JSON);

        return new AzureResponse<Void>(response, 201) {
            @Override
            public Void perform(String responseContent) throws JSONException {
                return null;
            }
        };
    }

    public static AzureResponse<Map<String, String>> getSubscriptions(String accessToken) throws IOException, JSONException {
        String url = Constants.DEFAULT_RESOURCE_MANAGER_ENDPOINT +  "subscriptions?api-version=2016-06-01";
        HttpResponse response = HttpHelper.sendGet(url, accessToken);

        return new AzureResponse<Map<String, String>>(response, 200) {
            @Override
            public Map<String, String> perform(String responseContent) throws JSONException {
//                public Map<String, String> getSubscriptions() throws JSONException {
                    if (!isSuccess()) return null;
                    JSONObject json = new JSONObject(responseContent);
                    JSONArray subcriptionsList = json.getJSONArray("value");
                    Map<String, String> subscriptions = new HashMap<String, String>();
                    for (int i = 0; i < subcriptionsList.length(); i++) {
                        String id = subcriptionsList.getJSONObject(i).getString("subscriptionId");
                        String name = subcriptionsList.getJSONObject(i).getString("displayName");
                        subscriptions.put(id, name);
                    }
                    return subscriptions;
//                }
            }
        };
    }

    public static AzureResponse<Set<AzureObject>> getAllAzureObjects(String accessToken, final AzureObjectType type) throws IOException, JSONException {
        String url = Constants.DEFAULT_GRAPH_ENDPOINT + Constants.DEFAULT_GRAPH_VERSION + type.getStringType();
        HttpResponse response = HttpHelper.sendGet(url, accessToken);
        return new AzureResponse<Set<AzureObject>>(response, 200) {
            @Override
            public Set<AzureObject> perform(String responseContent) throws JSONException {
                if (isFail()) return null;
                JSONObject json = new JSONObject(responseContent);
                JSONArray objs = json.getJSONArray("value");
                Set<AzureObject> set = new HashSet<>();
                for (int i = 0; i < objs.length(); i++) {
                    AzureObject obj = (AzureObject) Utils.GsonUtil.generateFromJsonString(objs.getString(i), type.getClazz());
                    set.add(obj);
                }
                return set;
            }

        };
    }


}
