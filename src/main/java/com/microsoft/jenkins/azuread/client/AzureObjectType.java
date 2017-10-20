package com.microsoft.jenkins.azuread.client;

/**
 * Created by albertxavier on 2017/9/9.
 */
public enum AzureObjectType {
    User,
    Group;

    public String getStringType() {
        switch (this) {
            case User: return "users";
            case Group: return "groups";
            default:
                throw new java.lang.IllegalArgumentException("Unsupported Azure Object Type: " + this);

        }
    }

    public Class getClazz() {
        switch (this) {
            case User: return AzureUser.class;
            case Group: return AzureGroup.class;
            default:
                throw new java.lang.IllegalArgumentException("Unsupported Azure Object Type: " + this);

        }
    }
}
