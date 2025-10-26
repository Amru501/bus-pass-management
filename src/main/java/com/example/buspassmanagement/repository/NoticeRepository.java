package com.example.buspassmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.buspassmanagement.model.Bus;
import com.example.buspassmanagement.model.Notice;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    
    // 🔑 ADDED: Fetches all notices ordered by timestamp, newest first.
    List<Notice> findAllByOrderByTimestampDesc();
    
    // Find all notices for a specific bus
    List<Notice> findByBus(Bus bus);
}