package com.microsoft.jenkins.azuread;

/**
 * Created by albertxavier on 2017/8/30.
 */

public class Constants {
    public static final String DEFAULT_MANAGEMENT_URL = "https://management.core.windows.net/";
        public static final String DEFAULT_AUTHENTICATION_ENDPOINT = "https://login.microsoftonline.com/";
//    public static final String DEFAULT_AUTHENTICATION_ENDPOINT = "https://login.windows.net/";
    public static final String DEFAULT_RESOURCE_MANAGER_ENDPOINT = "https://management.azure.com/";

//    public static final String DEFAULT_GRAPH_ENDPOINT = "https://graph.windows.net/";
//    public static final String DEFAULT_GRAPH_VERSION = "";

    public static final String DEFAULT_GRAPH_ENDPOINT = "https://graph.microsoft.com/";
    public static final String DEFAULT_GRAPH_VERSION = "v1.0/";
    public static final String BETA_GRAPH_VERSION = "beta/";
    public static final String DEFAULT_OAUTH_PREFIX = "https://login.windows.net/<TenantId>";
    public static final String DEFAULT_RESOURCE = DEFAULT_RESOURCE_MANAGER_ENDPOINT;
//    public static final String DEFAULT_RESOURCE = DEFAULT_GRAPH_ENDPOINT;

    public static final boolean DEBUG = false;
    public Constants() {
    }
}