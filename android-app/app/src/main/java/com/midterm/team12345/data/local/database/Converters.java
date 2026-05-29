package com.midterm.team12345.data.local.database;

import androidx.room.TypeConverter;
import com.midterm.team12345.data.remote.dto.response.FriendshipStatus;

public class Converters {
    @TypeConverter
    public static String fromFriendshipStatus(FriendshipStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static FriendshipStatus toFriendshipStatus(String status) {
        return status == null ? null : FriendshipStatus.valueOf(status);
    }
}
