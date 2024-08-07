package com.spring.privateClinicManage.api;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.privateClinicManage.dto.EmailDto;
import com.spring.privateClinicManage.dto.UserLoginDto;
import com.spring.privateClinicManage.dto.UserRegisterDto;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.entity.VerifyEmail;
import com.spring.privateClinicManage.service.JwtService;
import com.spring.privateClinicManage.service.MailSenderService;
import com.spring.privateClinicManage.service.UserService;
import com.spring.privateClinicManage.service.VerifyEmailService;

import jakarta.mail.MessagingException;


@RestController
@RequestMapping("/api/users")
public class ApiUserRestController {

	private JwtService jwtService;
	private UserService userService;
	private VerifyEmailService verifyEmailService;
	private MailSenderService mailSenderService;


	@Autowired
	public ApiUserRestController(JwtService jwtService, UserService userService,
			VerifyEmailService verifyEmailService, MailSenderService mailSenderService) {
		super();
		this.jwtService = jwtService;
		this.userService = userService;
		this.verifyEmailService = verifyEmailService;
		this.mailSenderService = mailSenderService;
	}

	@PostMapping(path = "/login/")
	@CrossOrigin
	public ResponseEntity<String> login(@RequestBody UserLoginDto loginDto) {

		if (!userService.authUser(loginDto.getEmail(), loginDto.getPassword()))
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		String token = jwtService.generateTokenLogin(loginDto.getEmail());

		return new ResponseEntity<>(token, HttpStatus.OK);

	}

	@GetMapping(path = "/current-user/", produces = {
			MediaType.APPLICATION_JSON_VALUE
	})
	@CrossOrigin
	public ResponseEntity<User> getCurrentUserApi() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
			User user = userService.findByEmail((authentication.getName()));
			return new ResponseEntity<User>(user, HttpStatus.OK);
		}
		return null;
	}

	@PostMapping(path = "/verify-email/")
	@CrossOrigin
	public ResponseEntity<String> retrieveOtp(@RequestBody EmailDto emailDto)
			throws UnsupportedEncodingException, MessagingException {

		User existUser = userService.findByEmail(emailDto.getEmail());
		if (existUser != null)
			return new ResponseEntity<>("This email is existed !", HttpStatus.UNAUTHORIZED);

		mailSenderService.sendOtpEmail(emailDto.getEmail());

		return new ResponseEntity<>("Sent mail successfully !", HttpStatus.OK);
	}

	@PostMapping(path = "/register/")
	@CrossOrigin
	public ResponseEntity<User> register(@RequestBody UserRegisterDto registerDto) {

		User existUser = userService.findByEmail(registerDto.getEmail());
		VerifyEmail verifyEmail = verifyEmailService.findByEmail(registerDto.getEmail());

		if (existUser != null || verifyEmail == null)
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		if (verifyEmailService.isOtpExpiredTime(verifyEmail))
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		if (!verifyEmailService.isOtpMatched(registerDto.getOtp(), verifyEmail))
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

		userService.saveUserRegisterDto(registerDto);
		existUser = userService.findByEmail(registerDto.getEmail());

		return new ResponseEntity<>(existUser, HttpStatus.CREATED);

	}


}
