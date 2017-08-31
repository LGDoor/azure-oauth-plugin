/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc., James Nord, Sam Gleske
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.microsoft.azure.oauth;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.security.SecurityRealm;
import jenkins.model.Jenkins;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

/**
 * A page that shows a simple message when the user logs out.
 * This prevents a logout -- login loop when using this security realm and Anonymous does not have {@code Overall.READ} permission.
 */
@Extension
public class AzureAuthFailAction implements UnprotectedRootAction {

    /** The URL of the action. */
    static final String POST_LOGOUT_URL = "azureAuthFail";

    @Override
    public String getDisplayName() {
        return "Azure Auth Fail";
    }

    @Override
    public String getIconFileName() {
        // hide it
        return null;
    }

    @Override
    public String getUrlName() {
        return POST_LOGOUT_URL;
    }

    @Restricted(NoExternalUse.class) // jelly only
    public String getAzureURL() {
        Jenkins j = Jenkins.getInstance();
        assert j != null;
        SecurityRealm r = j.getSecurityRealm();
        if (r instanceof AzureSecurityRealm) {
            AzureSecurityRealm ghsr = (AzureSecurityRealm) r;
            return ghsr.getAzurePortalUrl();
        }
        // only called from the Jelly if the GithubSecurityRealm is set...
        return "";
    }

    @Restricted(NoExternalUse.class) // jelly only
    public String getAzureText() {
        Jenkins j = Jenkins.getInstance();
        assert j != null;
        SecurityRealm r = j.getSecurityRealm();
        if (r instanceof AzureSecurityRealm) {
            AzureSecurityRealm ghsr = (AzureSecurityRealm) r;
            return "Azure";
        }
        // only called from the Jelly if the GithubSecurityRealm is set...
        return "";
    }
}