package com.spring.privateClinicManage.api;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
import com.spring.privateClinicManage.dto.RegisterScheduleDto;
import com.spring.privateClinicManage.dto.UserLoginDto;
import com.spring.privateClinicManage.dto.UserRegisterDto;
import com.spring.privateClinicManage.entity.MedicalRegistryList;
import com.spring.privateClinicManage.entity.Schedule;
import com.spring.privateClinicManage.entity.StatusIsApproved;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.entity.VerifyEmail;
import com.spring.privateClinicManage.service.JwtService;
import com.spring.privateClinicManage.service.MailSenderService;
import com.spring.privateClinicManage.service.MedicalRegistryListService;
import com.spring.privateClinicManage.service.ScheduleService;
import com.spring.privateClinicManage.service.StatusIsApprovedService;
import com.spring.privateClinicManage.service.UserService;
import com.spring.privateClinicManage.service.VerifyEmailService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.mail.MessagingException;


@RestController
@RequestMapping("/api/users")
public class ApiUserRestController {

	private JwtService jwtService;
	private UserService userService;
	private VerifyEmailService verifyEmailService;
	private MailSenderService mailSenderService;
	private Environment environment;
	private ScheduleService scheduleService;
	private MedicalRegistryListService medicalRegistryListService;
	private StatusIsApprovedService statusIsApprovedService;


	@Autowired
	public ApiUserRestController(JwtService jwtService, UserService userService,
			VerifyEmailService verifyEmailService, MailSenderService mailSenderService,
			Environment environment, MedicalRegistryListService medicalRegistryListService,
			ScheduleService scheduleService, StatusIsApprovedService statusIsApprovedService) {
		super();
		this.jwtService = jwtService;
		this.userService = userService;
		this.verifyEmailService = verifyEmailService;
		this.mailSenderService = mailSenderService;
		this.environment = environment;
		this.medicalRegistryListService = medicalRegistryListService;
		this.scheduleService = scheduleService;
		this.statusIsApprovedService = statusIsApprovedService;
	}

	@PostMapping(path = "/login/")
	@CrossOrigin
	public ResponseEntity<Object> login(@RequestBody UserLoginDto loginDto) {

		if (!userService.authUser(loginDto.getEmail(), loginDto.getPassword()))
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.UNAUTHORIZED);

		String token = jwtService.generateTokenLogin(loginDto.getEmail());

		return new ResponseEntity<>(token, HttpStatus.OK);

	}

	@GetMapping(path = "/current-user/", produces = {
			MediaType.APPLICATION_JSON_VALUE
	})
	@CrossOrigin
	public ResponseEntity<Object> getCurrentUserApi() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
			User user = userService.findByEmail((authentication.getName()));
			return new ResponseEntity<>(user, HttpStatus.OK);
		}
		return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);
	}

	@PostMapping(path = "/verify-email/")
	@CrossOrigin
	public ResponseEntity<Object> retrieveOtp(@RequestBody EmailDto emailDto)
			throws UnsupportedEncodingException, MessagingException {

		User existUser = userService.findByEmail(emailDto.getEmail());
		if (existUser != null)
			return new ResponseEntity<>("Email này đã được đăng kí !", HttpStatus.UNAUTHORIZED);

		mailSenderService.sendOtpEmail(emailDto.getEmail());

		return new ResponseEntity<>("Gửi mail thành công !", HttpStatus.OK);
	}

	@PostMapping(path = "/register/")
	@CrossOrigin
	public ResponseEntity<Object> register(@RequestBody UserRegisterDto registerDto) {

		User existUser = userService.findByEmail(registerDto.getEmail());
		VerifyEmail verifyEmail = verifyEmailService.findByEmail(registerDto.getEmail());

		if (existUser != null || verifyEmail == null || registerDto.getOtp().isEmpty()
				|| registerDto.getOtp() == "")
			return new ResponseEntity<>("Email này đã được đăng kí !",
					HttpStatus.UNAUTHORIZED);

		if (verifyEmailService.isOtpExpiredTime(verifyEmail))
			return new ResponseEntity<>("OTP đã hết hạn", HttpStatus.UNAUTHORIZED);

		if (!verifyEmailService.isOtpMatched(registerDto.getOtp(), verifyEmail))
			return new ResponseEntity<>("OTP không hợp lệ", HttpStatus.UNAUTHORIZED);

		userService.saveUserRegisterDto(registerDto);
		existUser = userService.findByEmail(registerDto.getEmail());

		return new ResponseEntity<>(existUser, HttpStatus.CREATED);

	}

	@GetMapping(value = "/sendSMS/")
	public ResponseEntity<String> sendSMS() {

		Twilio.init(environment.getProperty("TWILIO_ACCOUNT_SID"),
				environment.getProperty("TWILIO_AUTH_TOKEN"));

		Message.creator(new PhoneNumber("+840888232363"),
				new PhoneNumber(environment.getProperty("TWILIO_PHONE")), "Hello from Twilio 📞")
				.create();

		return new ResponseEntity<String>("Message sent successfully", HttpStatus.OK);
	}

	@PostMapping(value = "/register-schedule/")
	@CrossOrigin
	public ResponseEntity<Object> registerSchedule(@RequestBody RegisterScheduleDto registerScheduleDto) {
		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);
		
		Schedule schedule = scheduleService.findByDate(registerScheduleDto.getDate());
		if (schedule == null) {
			schedule = new Schedule();
			schedule.setDate(registerScheduleDto.getDate());
			schedule.setIsDayOff(false);
			scheduleService.saveSchedule(schedule);
		}
		
		if(schedule.getIsDayOff())
			return new ResponseEntity<>("Không thể chọn ngày lễ", HttpStatus.UNAUTHORIZED);

		MedicalRegistryList medicalRegistryList = medicalRegistryListService
				.findMRLByUserAndSchedule(currentUser, schedule);

		if (medicalRegistryList != null)
			return new ResponseEntity<>("Không thể đăng kí trùng ngày", HttpStatus.UNAUTHORIZED);
		
		StatusIsApproved statusIsApproved = statusIsApprovedService.findByStatus("CHECKING");
		
		medicalRegistryList = new MedicalRegistryList();
		medicalRegistryList.setStatusIsApproved(statusIsApproved);
		medicalRegistryList.setIsCanceled(false);
		medicalRegistryList.setUser(currentUser);
		medicalRegistryList.setName(registerScheduleDto.getName());
		medicalRegistryList.setFavor(registerScheduleDto.getFavor());
		medicalRegistryList.setSchedule(schedule);

		medicalRegistryListService.saveMedicalRegistryList(medicalRegistryList);

		return new ResponseEntity<>(medicalRegistryList, HttpStatus.CREATED);

	}


}
