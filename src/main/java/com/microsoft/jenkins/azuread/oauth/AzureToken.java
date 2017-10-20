package com.microsoft.jenkins.azuread.oauth;

import org.apache.commons.codec.binary.Base64;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.model.Token;
import org.scribe.utils.Preconditions;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href='mailto:donbeave@gmail.com'>Alexey Zhokhov</a>
 */
public class AzureToken extends Token {

    private Date expiry;
    private String userInfo;

    public AzureToken(java.lang.String token, java.lang.String secret, Date expiry, java.lang.String rawResponse) throws JSONException {
        super(token, secret, rawResponse);
        this.expiry = expiry;
        setUserInfo(rawResponse);
    }


    private void setUserInfoByIdToken(String idToken) throws JSONException {
            String parts[] = idToken.split("\\.");
            userInfo = base64UrlDecode(parts[1]);
//            AzureUserImpl userResponse = GSON.fromJson(base64UrlDecode(parts[1]), AzureUserImpl.class);
//            return userResponse;
    }

    private void setUserInfo(String response) throws JSONException {
        JSONObject  tokenResponse = new JSONObject(response);
        if (tokenResponse.has("id_token")) {
            String idToken = tokenResponse.getString("id_token");
            setUserInfoByIdToken(idToken);
        }

//        this.userInfo = userInfo;
    }

    public Date getExpiry() {
        return new Date(expiry.getTime());
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }

    private String base64UrlDecode(String input) {
        String result = null;
        Base64 decoder = new Base64(true);
        byte[] decodedBytes = decoder.decode(input);
        result = new String(decodedBytes);
        return result;
    }

    public String getUserInfo() {
        return userInfo;
    }
}