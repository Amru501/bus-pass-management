package com.example.buspassmanagement.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

	public void sendEmail(String to, String subject, String body) {
		System.out.println("EMAIL TO: " + to + " | SUBJECT: " + subject + " | BODY: " + body);
	}

	public void sendSms(String phone, String message) {
		System.out.println("SMS TO: " + phone + " | MESSAGE: " + message);
	}
}
