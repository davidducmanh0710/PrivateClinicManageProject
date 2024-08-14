package com.spring.privateClinicManage.service;


import java.io.UnsupportedEncodingException;

import jakarta.mail.MessagingException;

public interface MailSenderService {
	void sendOtpEmail(String email)
			throws MessagingException, UnsupportedEncodingException;

	void sendStatusRegisterEmail(String email, String content)
			throws MessagingException, UnsupportedEncodingException;

}
