package com.midterm.team12345.data.local.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.midterm.team12345.data.local.dao.AttachmentDao;
import com.midterm.team12345.data.local.dao.ConversationDao;
import com.midterm.team12345.data.local.dao.ConversationParticipantDao;
import com.midterm.team12345.data.local.dao.DraftDao;
import com.midterm.team12345.data.local.dao.MessageDao;
import com.midterm.team12345.data.local.dao.SyncQueueDao;
import com.midterm.team12345.data.local.dao.UserDao;
import com.midterm.team12345.data.local.entity.AttachmentEntity;
import com.midterm.team12345.data.local.entity.ConversationEntity;
import com.midterm.team12345.data.local.entity.ConversationParticipantEntity;
import com.midterm.team12345.data.local.entity.DraftMessageEntity;
import com.midterm.team12345.data.local.entity.MessageEntity;
import com.midterm.team12345.data.local.entity.SyncQueueEntity;
import com.midterm.team12345.data.local.entity.UserEntity;

@Database(
    entities = {
        UserEntity.class,
        ConversationEntity.class,
        MessageEntity.class,
        AttachmentEntity.class,
        ConversationParticipantEntity.class,
        SyncQueueEntity.class,
        DraftMessageEntity.class
    },
    version = 4,
    exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class MessengerDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "messenger_db";
    private static volatile MessengerDatabase instance;

    public static MessengerDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (MessengerDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.getApplicationContext(),
                        MessengerDatabase.class,
                        DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return instance;
    }

    public abstract UserDao userDao();
    public abstract ConversationDao conversationDao();
    public abstract MessageDao messageDao();
    public abstract AttachmentDao attachmentDao();
    public abstract ConversationParticipantDao conversationParticipantDao();
    public abstract SyncQueueDao syncQueueDao();
    public abstract DraftDao draftDao();
}
