package com.team12345.messenger.repository;
import com.team12345.messenger.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import java.util.List;
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Slice<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isSeen = true WHERE n.user.id = :userId AND n.isSeen = false")
    int markAllAsSeenByUserId(@Param("userId") Long userId);
}