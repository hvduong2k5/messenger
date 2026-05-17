package com.midterm.team12345.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.midterm.team12345.data.local.entity.SyncQueueEntity;
import com.midterm.team12345.data.local.entity.SyncStatus;
import java.util.List;

@Dao
public interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertTask(SyncQueueEntity task);

    @Update
    void updateTask(SyncQueueEntity task);

    @Delete
    void deleteTask(SyncQueueEntity task);

    @Query("DELETE FROM sync_queue WHERE id = :id")
    void deleteTaskById(Long id);

    @Query("SELECT * FROM sync_queue WHERE status = :status ORDER BY created_at ASC")
    List<SyncQueueEntity> getTasksByStatusSync(SyncStatus status);

    @Query("SELECT * FROM sync_queue WHERE status = :status ORDER BY created_at ASC")
    LiveData<List<SyncQueueEntity>> getTasksByStatus(SyncStatus status);

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY created_at ASC")
    List<SyncQueueEntity> getPendingTasksSync();

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY created_at ASC")
    LiveData<List<SyncQueueEntity>> getPendingTasks();

    @Query("UPDATE sync_queue SET status = :status WHERE id = :id")
    void updateTaskStatus(Long id, SyncStatus status);

    @Query("UPDATE sync_queue SET retry_count = retry_count + 1, next_retry_at = :nextRetryAt, status = :status WHERE id = :id")
    void updateTaskFailure(Long id, Long nextRetryAt, SyncStatus status);

    @Query("DELETE FROM sync_queue WHERE status = 'PROCESSING'")
    void clearProcessingTasks();

    @Query("DELETE FROM sync_queue")
    void deleteAllTasks();
}
