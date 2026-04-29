package com.team12345.messenger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.team12345.messenger.entity.Call;
import java.util.List;

@Repository
public interface CallRepository extends JpaRepository<Call, Long>{
    public List<Call> findByCallerId(Long callerId);
}
