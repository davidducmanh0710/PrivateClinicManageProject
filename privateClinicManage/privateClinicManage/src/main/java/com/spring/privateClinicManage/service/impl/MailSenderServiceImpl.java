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

import com.spring.privateClinicManage.entity.MedicalRegistryList;
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

	@Override
	public void sendStatusRegisterEmail(MedicalRegistryList mrl, String content)
			throws MessagingException, UnsupportedEncodingException {

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom(env.getProperty("spring.mail.username"), "Account Support");
		helper.setTo(mrl.getUser().getEmail());

		String subject = "Thư xác nhận đăng kí lịch khám";

		String header = "<p>Xin chào " + mrl.getUser().getEmail() + "</p>";
		String body = "";
		String footer = "";

		if (mrl.getStatusIsApproved().getStatus().equals("SUCCESS")) {
			header += "<p class='text-success'><strong>Quý khách đã đăng kí thành công lịch khám !</strong><p/>";
			body += "<p> Tên người khám : <strong>" + mrl.getName() + "</strong></p>" +
					"<p> Ngày hẹn khám : <strong>" + mrl.getSchedule().getDate() + "</strong></p>" +
					"<p> <strong>Khi đến khám hãy đến gặp quầy y tá , quét mã QR này để lấy số thứ tự :</strong></p>"
					+
					"<img src='" + mrl.getQrUrl() + "'/>";

			footer = "<h4>Xin chân thành cảm ơn quý khách đã đăng kí phòng khám của chúng tôi !</h4>";
		} else if (mrl.getStatusIsApproved().getStatus().equals("FAILED")) {
			header += "<p class='text-danger'><strong>Quý khách đã đăng kí thất bại lịch khám !</strong><p/>";
			body += "<p>" + content + "</p>";
			footer = "<h4>Xin chân thành xin lỗi sự bất tiện này và cảm ơn quý khách đã đăng kí phòng khám của chúng tôi !</h4>";
		}

		helper.setSubject(subject);
		String allContent = header + body + footer;
		helper.setText(allContent, true);

		mailSender.send(message);
	}

}
