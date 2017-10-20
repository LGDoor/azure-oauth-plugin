package com.microsoft.jenkins.azuread;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by t-wanl on 8/24/2017.
 */
public class HttpHelper {
    public static HttpResponse sendGet(String url, String accessToken) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(url);

        if (accessToken != null) {
            Map<String, String> headers = generateHeaders(request, accessToken, null);
            request = (HttpGet) addHeaders(request, headers);
        }
        HttpResponse response = client.execute(request);
        return response;
    }

    public static HttpResponse sendPost(String url, String accessToken, Object body, ContentType contentType) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(url);

        Map<String, String> headers = generateHeaders(request, accessToken, contentType);
        request = (HttpPost) addHeaders(request, headers);
        if (body != null) {
            request = (HttpPost) addBody(request, body, contentType);
        }
        HttpResponse response = client.execute(request);
        return response;
    }

    public static HttpResponse sendPut(String url, String accessToken, Object body, ContentType contentType) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpPut request = new HttpPut(url);
        Map<String, String> headers = generateHeaders(request, accessToken, contentType);
        request = (HttpPut) addHeaders(request, headers);
        if (body != null) {
            request = (HttpPut) addBody(request, body, contentType);
        }
        HttpResponse response = client.execute(request);
        return response;
    }

    private static HttpEntityEnclosingRequestBase addBody(HttpEntityEnclosingRequestBase request, Object body, ContentType contentType) throws UnsupportedEncodingException {
        if (body instanceof JSONObject) request.setEntity(new StringEntity(((JSONObject) body).toString(), contentType));
        else if (body instanceof HttpEntity) request.setEntity((HttpEntity) body);
        return request;
    }

    private static Map<String, String> generateHeaders(HttpRequestBase request, String accessToken, ContentType contentType) {
        Map<String, String> headers = new HashMap<String, String>();
        if (request instanceof HttpGet) {
//            headers.put("client-version", "v1.0");
            if (accessToken != null) headers.put("Authorization", "Bearer " + accessToken);
            headers.put("Accept", "application/json");
        } else if (request instanceof HttpPost) {
            String contentTypeStr = null;
            String charSetStr = null;
            if (contentType != null) {
                contentTypeStr = contentType.getMimeType();
                charSetStr = "charset=" + contentType.getCharset().toString();
            } else {
                contentTypeStr = ContentType.APPLICATION_JSON.getMimeType();
                charSetStr = "charset=" + ContentType.APPLICATION_JSON.getCharset().toString();
            }
            if (accessToken != null) {
                headers.put("Authorization", "Bearer " + accessToken);
//                headers.put("client-version", "v1.0");
                headers.put("Accept", "application/json, text/plain, */*");
                headers.put("Content-Type", contentTypeStr + ";" + charSetStr);
            } else {
//                headers.put("client-version", "v1.0");
                headers.put("Accept", "application/json, text/plain, */*");
                headers.put("Content-Type", contentTypeStr + ";" + charSetStr);
            }
        } else if (request instanceof HttpPut) {
            String contentTypeStr = null;
            String charSetStr = null;
            if (contentType != null) {
                contentTypeStr = contentType.getMimeType();
                charSetStr = "charset=" + contentType.getCharset().toString();
            } else {
                contentTypeStr = ContentType.APPLICATION_JSON.getMimeType();
                charSetStr = "charset=" + ContentType.APPLICATION_JSON.getCharset().toString();
            }
            headers.put("Authorization", "Bearer " + accessToken);
//            headers.put("client-version", "v1.0");
            headers.put("Accept", "application/json, text/plain, */*");
            headers.put("Content-Type", contentTypeStr + ";" + charSetStr);
        }
        return headers;
    }
    private static HttpRequestBase addHeaders(HttpRequestBase request, Map<String, String> headers) {
        for(Map.Entry<String, String> h : headers.entrySet()) {
            String key = h.getKey();
            String val = h.getValue();
            request.addHeader(key, val);
        }
        return request;
    }

    public static int getStatusCode(HttpResponse response) {
        return response.getStatusLine().getStatusCode();
    }

    public static String getContent(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        return result.toString();
    }

}
