package com.team12345.messenger.repository;

import com.team12345.messenger.entity.UserFriend;
import com.team12345.messenger.entity.UserFriendId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFriendRepository extends JpaRepository<UserFriend, UserFriendId> {

    @EntityGraph(value = "UserFriend.withFriendInfo")
    List<UserFriend> findById_UserId(Long userId);

    @EntityGraph(value = "UserFriend.withFriendInfo")
    Page<UserFriend> findById_UserId(Long userId, Pageable pageable);

    boolean existsById_UserIdAndId_FriendId(Long userId, Long friendId);
}