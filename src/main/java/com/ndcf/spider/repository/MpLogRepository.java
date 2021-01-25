package com.ndcf.spider.repository;


import com.ndcf.spider.bo.MpLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MpLogRepository extends JpaRepository<MpLog, Long> {

    @Modifying
    @Query(value = "update mp_log t1 set t1.status=1   where t1.username=?1 and (t1.status<>1 or t1.status is null ) ", nativeQuery = true)
    int updateStatusByUsername(String username);

}
