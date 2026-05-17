package com.midterm.team12345.data.local.database;

import androidx.room.TypeConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.midterm.team12345.data.local.entity.AttachmentUiState;
import com.midterm.team12345.data.local.entity.DeliveryStatus;
import com.midterm.team12345.data.local.entity.SyncOperationType;
import com.midterm.team12345.data.local.entity.SyncState;
import com.midterm.team12345.data.local.entity.SyncStatus;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class Converters {
    private static final Gson gson = new Gson();

    // 1. Date Converters
    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    // 2. LocalDateTime Converters
    @TypeConverter
    public static String fromLocalDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @TypeConverter
    public static LocalDateTime toLocalDateTime(String dateTimeString) {
        return dateTimeString == null ? null : LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    // 3. Enum Converters
    @TypeConverter
    public static String fromSyncState(SyncState state) {
        return state == null ? null : state.name();
    }

    @TypeConverter
    public static SyncState toSyncState(String state) {
        return state == null ? null : SyncState.valueOf(state);
    }

    @TypeConverter
    public static String fromDeliveryStatus(DeliveryStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static DeliveryStatus toDeliveryStatus(String status) {
        return status == null ? null : DeliveryStatus.valueOf(status);
    }

    @TypeConverter
    public static String fromAttachmentUiState(AttachmentUiState state) {
        return state == null ? null : state.name();
    }

    @TypeConverter
    public static AttachmentUiState toAttachmentUiState(String state) {
        return state == null ? null : AttachmentUiState.valueOf(state);
    }

    @TypeConverter
    public static String fromSyncOperationType(SyncOperationType type) {
        return type == null ? null : type.name();
    }

    @TypeConverter
    public static SyncOperationType toSyncOperationType(String type) {
        return type == null ? null : SyncOperationType.valueOf(type);
    }

    @TypeConverter
    public static String fromSyncStatus(SyncStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static SyncStatus toSyncStatus(String status) {
        return status == null ? null : SyncStatus.valueOf(status);
    }

    // 4. List to JSON String Converters
    @TypeConverter
    public static String fromStringList(List<String> list) {
        return list == null ? null : gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toStringList(String value) {
        if (value == null) return null;
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromLongList(List<Long> list) {
        return list == null ? null : gson.toJson(list);
    }

    @TypeConverter
    public static List<Long> toLongList(String value) {
        if (value == null) return null;
        Type listType = new TypeToken<List<Long>>() {}.getType();
        return gson.fromJson(value, listType);
    }
}
