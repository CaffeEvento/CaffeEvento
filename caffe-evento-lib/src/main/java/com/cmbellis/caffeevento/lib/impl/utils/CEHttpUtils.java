package com.cmbellis.caffeevento.lib.impl.utils;

import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Created by chris on 7/22/16.
 */
public final class CEHttpUtils {
    private static HttpClient client = HttpClients.createDefault();

    private CEHttpUtils() {
    }

    public static HttpResponse doPost(String destination, String postData, Map<String, String> headers) throws IOException {
        HttpPost post = new HttpPost(destination);
        headers.entrySet().forEach(e -> post.setHeader(e.getKey(), e.getValue()));
        post.setEntity(new StringEntity(postData));
        return client.execute(post);
    }

    public static HttpResponse doGet(String destination, Map<String, String> headers) throws IOException {
        HttpGet get = new HttpGet(destination);
        headers.entrySet().forEach(e -> get.setHeader(e.getKey(), e.getValue()));
        return client.execute(get);
    }

    public static JsonObject doGetJson(String destination, Map<String, String> headers) throws IOException {
        HttpResponse response = doGet(destination, headers);
        return JSONUtils.convertToJsonObject(new InputStreamReader(response.getEntity().getContent()));
    }
}
