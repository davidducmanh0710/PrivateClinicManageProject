package com.spring.privateClinicManage.service;


import java.io.UnsupportedEncodingException;

import jakarta.mail.MessagingException;

public interface MailSenderService {
	void sendOtpEmail(String email)
			throws MessagingException, UnsupportedEncodingException;

}
