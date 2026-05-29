package com.midterm.team12345.data.remote.interceptor;

import androidx.annotation.NonNull;

import com.midterm.team12345.data.local.TokenManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String token = tokenManager.getToken();

        if (token != null && !token.isEmpty()) {
            Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        }
        return chain.proceed(original);
    }
}
