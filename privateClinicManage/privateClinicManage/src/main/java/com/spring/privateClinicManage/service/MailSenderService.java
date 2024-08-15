package com.spring.privateClinicManage.service;


import java.io.UnsupportedEncodingException;

import com.spring.privateClinicManage.entity.MedicalRegistryList;

import jakarta.mail.MessagingException;

public interface MailSenderService {
	void sendOtpEmail(String email)
			throws MessagingException, UnsupportedEncodingException;

	void sendStatusRegisterEmail(MedicalRegistryList mrl, String content)
			throws MessagingException, UnsupportedEncodingException;

}
