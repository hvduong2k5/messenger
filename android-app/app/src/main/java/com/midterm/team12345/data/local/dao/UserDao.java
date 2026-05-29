package com.midterm.team12345.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.midterm.team12345.data.local.entity.UserEntity;
import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsers(List<UserEntity> users);

    @Update
    void updateUser(UserEntity user);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    LiveData<UserEntity> getUserById(Long userId);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    UserEntity getUserByIdSync(Long userId);

    @Query("SELECT * FROM users WHERE is_friend = 1 ORDER BY username ASC")
    LiveData<List<UserEntity>> getFriends();

    @Query("SELECT * FROM users WHERE is_friend = 1 ORDER BY username ASC")
    List<UserEntity> getFriendsSync();

    @Query("SELECT * FROM users WHERE is_friend = 1 ORDER BY friendship_established_at DESC")
    LiveData<List<UserEntity>> getFriendsByNewest();

    @Query("SELECT * FROM users WHERE is_friend = 1 ORDER BY friendship_established_at DESC")
    List<UserEntity> getFriendsByNewestSync();

    @Query("SELECT * FROM users WHERE is_friend = 1 AND (username LIKE '%' || :searchQuery || '%') ORDER BY username ASC")
    LiveData<List<UserEntity>> searchFriendsLocally(String searchQuery);

    @Query("DELETE FROM users")
    void deleteAllUsers();
}
