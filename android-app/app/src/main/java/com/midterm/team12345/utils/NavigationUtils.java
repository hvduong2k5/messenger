package com.midterm.team12345.utils;

import android.content.Context;
import android.content.Intent;
import com.midterm.team12345.ui.settings.UserProfileActivity;
import com.midterm.team12345.data.local.TokenManager;

public class NavigationUtils {
    public static void navigateToProfile(Context context, Long targetUserId) {
        navigateToProfile(context, targetUserId, null);
    }

    public static void navigateToProfile(Context context, Long targetUserId, Long fromChatPartnerId) {
        if (targetUserId == null) return;
        
        Long currentUserId = TokenManager.getInstance(context).getUserId();
        // Nếu click trúng ID của chính mình thì không mở UserProfileActivity người khác
        if (!targetUserId.equals(currentUserId)) {
            Intent intent = new Intent(context, UserProfileActivity.class);
            intent.putExtra("TARGET_USER_ID", targetUserId);
            if (fromChatPartnerId != null) {
                intent.putExtra("FROM_CHAT_PARTNER_ID", fromChatPartnerId);
            }
            context.startActivity(intent);
        }
    }
}
