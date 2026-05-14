package com.midterm.team12345.data.remote;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.util.LocalDateTimeAdapter;

import java.time.LocalDateTime;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://192.168.1.166:8080";
    private static Retrofit retrofit = null;

    public static synchronized Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            TokenManager tokenManager = new TokenManager(context);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(chain -> {
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
                    })
                    .build();

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static AuthApiService getAuthApiService(Context context) {
        return getClient(context).create(AuthApiService.class);
    }
    
    public static ConversationApiService getConversationApiService(Context context) {
        return getClient(context).create(ConversationApiService.class);
    }

    public static MessageApiService getMessageApiService(Context context) {
        return getClient(context).create(MessageApiService.class);
    }

    public static UserApiService getUserApiService(Context context) {
        return getClient(context).create(UserApiService.class);
    }
}
