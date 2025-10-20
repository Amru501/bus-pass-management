package com.example.buspassmanagement.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class OtpService {

	private static class OtpEntry {
		final String code;
		final Instant expiresAt;
		OtpEntry(String code, Instant expiresAt) {
			this.code = code;
			this.expiresAt = expiresAt;
		}
	}

	private final Map<String, OtpEntry> emailToOtp = new ConcurrentHashMap<>();

	public String generateOtpFor(String email) {
		String code = String.valueOf((int)(Math.random() * 900000) + 100000);
		emailToOtp.put(email, new OtpEntry(code, Instant.now().plus(5, ChronoUnit.MINUTES)));
		return code;
	}

	public boolean verify(String email, String code) {
		OtpEntry entry = emailToOtp.get(email);
		if (entry == null) return false;
		if (Instant.now().isAfter(entry.expiresAt)) {
			emailToOtp.remove(email);
			return false;
		}
		return entry.code.equals(code);
	}

	public void clear(String email) {
		emailToOtp.remove(email);
	}
}
