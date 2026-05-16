package com.midterm.team12345.data.remote;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.midterm.team12345.data.local.TokenManager;
import com.midterm.team12345.data.remote.api.AttachmentApiService;
import com.midterm.team12345.data.remote.api.AuthApiService;
import com.midterm.team12345.data.remote.api.CallApiService;
import com.midterm.team12345.data.remote.api.ConversationApiService;
import com.midterm.team12345.data.remote.api.FriendApiService;
import com.midterm.team12345.data.remote.api.MessageApiService;
import com.midterm.team12345.data.remote.api.MessageStatusApiService;
import com.midterm.team12345.data.remote.api.NotificationApiService;
import com.midterm.team12345.data.remote.api.UserApiService;
import com.midterm.team12345.data.remote.interceptor.AuthInterceptor;
import com.midterm.team12345.utils.LocalDateTimeAdapter;

import java.time.LocalDateTime;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.38.249.57:8080";
    private static Retrofit retrofit = null;

    public static synchronized Retrofit getClient(Context context) {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            TokenManager tokenManager = new TokenManager(context.getApplicationContext());

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(new AuthInterceptor(tokenManager))
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

    public static CallApiService getCallApiService(Context context) {
        return getClient(context).create(CallApiService.class);
    }

    public static FriendApiService getFriendApiService(Context context) {
        return getClient(context).create(FriendApiService.class);
    }

    public static MessageStatusApiService getMessageStatusApiService(Context context) {
        return getClient(context).create(MessageStatusApiService.class);
    }

    public static NotificationApiService getNotificationApiService(Context context) {
        return getClient(context).create(NotificationApiService.class);
    }

    public static AttachmentApiService getAttachmentApiService(Context context) {
        return getClient(context).create(AttachmentApiService.class);
    }
}
