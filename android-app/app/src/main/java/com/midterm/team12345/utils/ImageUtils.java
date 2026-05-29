package com.midterm.team12345.utils;

public class ImageUtils {
    public static String getFullUrl(String url) {
        if (url == null || url.isEmpty()) return null;
        if (url.startsWith("http")) return url;
        return com.midterm.team12345.data.remote.RetrofitClient.getBaseUrl() 
                + (url.startsWith("/") ? "" : "/") + url;
    }

    public static String optimizeAvatarUrl(String url) {
        String fullUrl = getFullUrl(url);
        if (fullUrl == null) return null;
        if (fullUrl.contains("cloudinary.com") && fullUrl.contains("/upload/")) {
            return fullUrl.replaceFirst("/upload/", "/upload/c_fill,g_face,w_150,h_150,q_auto,f_auto/");
        }
        return fullUrl;
    }

    public static String optimizeMediaUrl(String url) {
        String fullUrl = getFullUrl(url);
        if (fullUrl == null) return null;
        if (fullUrl.contains("cloudinary.com") && fullUrl.contains("/upload/")) {
            return fullUrl.replaceFirst("/upload/", "/upload/c_limit,w_600,q_auto,f_auto/");
        }
        return fullUrl;
    }
}
