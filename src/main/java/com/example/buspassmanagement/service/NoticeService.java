package com.example.buspassmanagement.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.buspassmanagement.model.Notice;
import com.example.buspassmanagement.repository.NoticeRepository;

@Service
public class NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;

    public Notice addNotice(Notice notice) {
        // Business logic (e.g., notifying users) can go here
        return noticeRepository.save(notice);
    }

    // 🔑 UPDATED: Use the custom repository method to ensure notices are sorted
    public List<Notice> getAllNotices() {
        return noticeRepository.findAllByOrderByTimestampDesc();
    }

    public Optional<Notice> getNoticeById(Long id) {
        return noticeRepository.findById(id);
    }

    public void deleteNotice(Long id) {
        noticeRepository.deleteById(id);
    }
}