package com.midterm.team12345.data.local.database;

import android.content.Context;
import com.midterm.team12345.data.local.dao.AttachmentDao;
import com.midterm.team12345.data.local.dao.ConversationDao;
import com.midterm.team12345.data.local.dao.ConversationParticipantDao;
import com.midterm.team12345.data.local.dao.DraftDao;
import com.midterm.team12345.data.local.dao.MessageDao;
import com.midterm.team12345.data.local.dao.SyncQueueDao;
import com.midterm.team12345.data.local.dao.UserDao;

public class DatabaseProvider {

    private static volatile DatabaseProvider instance;
    private final MessengerDatabase database;

    private DatabaseProvider(Context context) {
        this.database = MessengerDatabase.getInstance(context);
    }

    public static DatabaseProvider getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseProvider.class) {
                if (instance == null) {
                    instance = new DatabaseProvider(context);
                }
            }
        }
        return instance;
    }

    public MessengerDatabase getDatabase() {
        return database;
    }

    public UserDao getUserDao() {
        return database.userDao();
    }

    public ConversationDao getConversationDao() {
        return database.conversationDao();
    }

    public MessageDao getMessageDao() {
        return database.messageDao();
    }

    public AttachmentDao getAttachmentDao() {
        return database.attachmentDao();
    }

    public ConversationParticipantDao getConversationParticipantDao() {
        return database.conversationParticipantDao();
    }

    public SyncQueueDao getSyncQueueDao() {
        return database.syncQueueDao();
    }

    public DraftDao getDraftDao() {
        return database.draftDao();
    }
}
