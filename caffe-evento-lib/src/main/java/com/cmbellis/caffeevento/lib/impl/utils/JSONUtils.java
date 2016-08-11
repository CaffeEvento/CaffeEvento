package com.cmbellis.caffeevento.lib.impl.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.Reader;

/**
 * Created by chris on 7/18/16.
 */
public final class JSONUtils {
    private JSONUtils() {}
    private static final Gson gson = new GsonBuilder().create();

    public static <T> String convertToJson(T object) {
        return gson.toJson(object);
    }

    public static <T> T convertFromJson(String json, Class<? extends T> objectType) {
        return gson.fromJson(json, objectType);
    }

    public static <T> T convertFromJson(Reader json, Class<? extends T> objectType) {
        return gson.fromJson(json, objectType);
    }

    public static JsonObject convertToJsonObject(Reader json) {
        return gson.fromJson(json, JsonObject.class);
    }
}
