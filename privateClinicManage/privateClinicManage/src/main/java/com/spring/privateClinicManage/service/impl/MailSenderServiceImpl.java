package com.spring.privateClinicManage.service.impl;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.spring.privateClinicManage.entity.VerifyEmail;
import com.spring.privateClinicManage.service.MailSenderService;
import com.spring.privateClinicManage.service.VerifyEmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailSenderServiceImpl implements MailSenderService {

	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private Environment env;
	@Autowired
	private VerifyEmailService verifyEmailService;


	@Override
	@Async
	public void sendOtpEmail(String email)
			throws MessagingException, UnsupportedEncodingException {
		Random r = new Random();
		Long c = r.nextLong(100000, 999999);
		String otp = c.toString();


		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(env.getProperty("spring.mail.username"), "Account Support");
		helper.setTo(email);

		String subject = "OTP FOR REGISTER ACCOUNT";

		String content = "<p>Hello " + email + "</p>"
				+ "<p>You have requested to verify your email.</p>"
				+ "<p> The code for verifying : <span style='color:red;'>" + otp
				+ "</span></p>"
				+ "<p> The code will be expired after :  <span style='color:red;'> "
				+ env.getProperty("spring.otp.expired-seconds") + " seconds ! </span> </p>"
				+ "<p style='color:red;'>DO NOT SHARE THIS CODE FOR ANYONE ELSE</p>";

		helper.setSubject(subject);

		helper.setText(content, true);

		VerifyEmail verifyEmail = verifyEmailService.findByEmail(email);
		if (verifyEmail == null)
			verifyEmail = new VerifyEmail();

		verifyEmail.setEmail(email);
		verifyEmail.setOtp(otp);
		verifyEmail.setExpriedTime(LocalDateTime.now()
				.plusSeconds(Long.parseLong(env.getProperty("spring.otp.expired-seconds"))));
		verifyEmailService.saveVerifyEmail(verifyEmail);

		mailSender.send(message);

	}


}
