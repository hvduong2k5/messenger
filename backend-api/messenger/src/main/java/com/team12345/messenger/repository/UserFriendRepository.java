package com.team12345.messenger.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.team12345.messenger.entity.UserFriend;
import com.team12345.messenger.entity.UserFriendId;

@Repository
public interface UserFriendRepository extends JpaRepository<UserFriend, UserFriendId> {
    @EntityGraph(value = "UserFriend.withFriendInfo", type = EntityGraph.EntityGraphType.FETCH)
    Page<UserFriend> findById_UserId(Long userId, Pageable pageable);
}
