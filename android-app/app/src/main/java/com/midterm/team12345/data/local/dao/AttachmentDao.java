package com.midterm.team12345.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.midterm.team12345.data.local.entity.AttachmentEntity;
import com.midterm.team12345.data.local.entity.AttachmentUiState;
import java.util.List;

@Dao
public interface AttachmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertAttachment(AttachmentEntity attachment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAttachments(List<AttachmentEntity> attachments);

    @Update
    void updateAttachment(AttachmentEntity attachment);

    @Query("SELECT * FROM attachments WHERE client_message_id = :clientMessageId")
    LiveData<List<AttachmentEntity>> getAttachmentsByClientMessageId(String clientMessageId);

    @Query("SELECT * FROM attachments WHERE client_message_id = :clientMessageId")
    List<AttachmentEntity> getAttachmentsByClientMessageIdSync(String clientMessageId);

    @Query("UPDATE attachments SET id = :serverId, url = :url, ui_state = :uiState WHERE client_message_id = :clientMessageId AND local_uri = :localUri")
    void updateUploadSuccess(String clientMessageId, String localUri, Long serverId, String url, AttachmentUiState uiState);

    @Query("UPDATE attachments SET ui_state = :uiState WHERE client_message_id = :clientMessageId")
    void updateUploadState(String clientMessageId, AttachmentUiState uiState);

    @Query("DELETE FROM attachments WHERE local_id = :localId")
    void deleteAttachmentById(Long localId);

    @Query("DELETE FROM attachments WHERE client_message_id = :clientMessageId")
    void deleteAttachmentsByClientMessageId(String clientMessageId);

    @Query("DELETE FROM attachments")
    void deleteAllAttachments();
}
