package com.spring.privateClinicManage.api;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.spring.privateClinicManage.utilities.CalendarFormat;
import com.spring.privateClinicManage.utilities.CalendarFormatUtil;
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

	// ROLE_BENHNHAN

	@PostMapping(value = "/register-schedule/")
	@CrossOrigin
	public ResponseEntity<Object> registerSchedule(
			@RequestBody RegisterScheduleDto registerScheduleDto) {
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

		if (schedule.getIsDayOff())
			return new ResponseEntity<>("Không thể chọn ngày lễ", HttpStatus.UNAUTHORIZED);

		Integer countMedicalRegistryList = medicalRegistryListService
				.countMRLByUserAndScheduleAndisCancelled(currentUser, schedule, false);

		if (countMedicalRegistryList >= Integer
				.parseInt(environment.getProperty("register_schedule_per_day_max")))
			return new ResponseEntity<>("Tài khoản này đã đăng kí đủ 4 lần / 1 ngày",
					HttpStatus.UNAUTHORIZED);

		StatusIsApproved statusIsApproved = statusIsApprovedService.findByStatus("CHECKING");

		MedicalRegistryList medicalRegistryList = new MedicalRegistryList();
		medicalRegistryList.setCreatedDate(new Date());
		medicalRegistryList.setStatusIsApproved(statusIsApproved);
		medicalRegistryList.setIsCanceled(false);
		medicalRegistryList.setUser(currentUser);
		medicalRegistryList.setName(registerScheduleDto.getName());
		medicalRegistryList.setFavor(registerScheduleDto.getFavor());
		medicalRegistryList.setSchedule(schedule);

		medicalRegistryListService.saveMedicalRegistryList(medicalRegistryList);

		return new ResponseEntity<>(medicalRegistryList, HttpStatus.CREATED);

	}

	@GetMapping(value = "/user-register-schedule-list/")
	@CrossOrigin
	public ResponseEntity<Object> getCurrentUserRegisterScheduleList(
			@RequestParam Map<String, String> params) {
		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		Integer page = Integer.parseInt(params.getOrDefault("page", "1"));
		Integer size = Integer.parseInt(params.getOrDefault("size",
				environment.getProperty("user_register_list_pagesize")));

		List<MedicalRegistryList> mrls = medicalRegistryListService.findByUser(currentUser);

		Page<MedicalRegistryList> registryListsPaginated = medicalRegistryListService
				.findByUserPaginated(page, size, mrls);

		return new ResponseEntity<>(registryListsPaginated, HttpStatus.OK);

	}

	@PatchMapping(value = "/cancel-register-schedule/{registerScheduleId}/")
	@CrossOrigin
	public ResponseEntity<Object> cancelRegisterSchedule(
			@PathVariable("registerScheduleId") Integer registerScheduleId) {

		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		MedicalRegistryList medicalRegistryList = medicalRegistryListService
				.findById(registerScheduleId);

		if (medicalRegistryList == null || medicalRegistryList.getIsCanceled())
			return new ResponseEntity<>("Phiếu đăng kí này không tồn tại hoặc đã được hủy !",
					HttpStatus.NOT_FOUND);

		StatusIsApproved statusIsApproved = statusIsApprovedService.findByStatus("CANCELED");

		medicalRegistryList.setIsCanceled(true);
		medicalRegistryList.setStatusIsApproved(statusIsApproved);
		medicalRegistryListService.saveMedicalRegistryList(medicalRegistryList);

		return new ResponseEntity<>("Đã hủy lịch thành công !", HttpStatus.OK);

	}

	// ROLE_YTA
	@GetMapping(value = "/censor-register-schedule")
	@CrossOrigin
	public ResponseEntity<Object> censorRegisterSchedule(@RequestParam Map<String, String> params)
			throws ParseException, NumberFormatException {

		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		Integer page = Integer.parseInt(params.getOrDefault("page", "1"));
		Integer size = Integer.parseInt(params.getOrDefault("size", "5"));

		String date = params.getOrDefault("date", "");
		CalendarFormat c = CalendarFormatUtil.parseStringToCalendar(date);
		String staus = params.getOrDefault("status", "CHECKING");

		StatusIsApproved statusIsApproved = statusIsApprovedService.findByStatus(staus);

		if (statusIsApproved == null)
			return new ResponseEntity<>("Giá trị tìm kiếm không đúng !", HttpStatus.NOT_FOUND);

		List<MedicalRegistryList> mrls = medicalRegistryListService
				.findByScheduleAndStatusIsApproved(c.getYear(),
						c.getMonth(), c.getDay(), statusIsApproved);

		Page<MedicalRegistryList> registryListsPaginated = medicalRegistryListService
				.findByScheduleAndStatusIsApprovedPaginated(page, size, mrls);

		return new ResponseEntity<>(registryListsPaginated, HttpStatus.OK);
	}

	@GetMapping(value = "/getAllStatusIsApproved/")
	public ResponseEntity<Object> getAllStatusIsApproved() {
		return new ResponseEntity<>(statusIsApprovedService.findAllStatus(), HttpStatus.OK);
	}

	@GetMapping(value = "/all-register-schedule")
	public ResponseEntity<Object> getAllRegisterSchedule(Model model,
			@RequestParam Map<String, String> params) {

		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		Integer page = Integer.parseInt(params.getOrDefault("page", "1"));
		Integer size = Integer.parseInt(params.getOrDefault("size", "6"));

		List<MedicalRegistryList> mrls = medicalRegistryListService.findAllMrl();

		String key = params.getOrDefault("key", "");
		if (!key.isBlank())
			mrls = medicalRegistryListService.findByAnyKey(key);

		String status = params.getOrDefault("status", "ALL");
		StatusIsApproved statusIsApproved = statusIsApprovedService.findByStatus(status);

		if (statusIsApproved != null) {
			mrls = medicalRegistryListService.sortByStatusIsApproved(mrls, statusIsApproved);
		}

		Page<MedicalRegistryList> mrlsPaginated = medicalRegistryListService.findMrlsPaginated(page,
				size, mrls);

		model.addAttribute("key", key);

		return new ResponseEntity<>(mrlsPaginated, HttpStatus.OK);
	}

}
