package com.spring.privateClinicManage.api;

import java.io.IOException;
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

import com.spring.privateClinicManage.dto.ConfirmRegisterDto;
import com.spring.privateClinicManage.dto.DirectRegisterDto;
import com.spring.privateClinicManage.dto.EmailDto;
import com.spring.privateClinicManage.dto.MrlIdScanQrDto;
import com.spring.privateClinicManage.dto.OrderQrCodeDto;
import com.spring.privateClinicManage.dto.RegisterScheduleDto;
import com.spring.privateClinicManage.dto.RegisterStatusDto;
import com.spring.privateClinicManage.dto.UserLoginDto;
import com.spring.privateClinicManage.dto.UserRegisterDto;
import com.spring.privateClinicManage.entity.MedicalRegistryList;
import com.spring.privateClinicManage.entity.Schedule;
import com.spring.privateClinicManage.entity.StatusIsApproved;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.entity.VerifyEmail;
import com.spring.privateClinicManage.service.DownloadPDFService;
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
	private DownloadPDFService downloadPDFService;

	@Autowired
	public ApiUserRestController(JwtService jwtService, UserService userService,
			VerifyEmailService verifyEmailService, MailSenderService mailSenderService,
			Environment environment, MedicalRegistryListService medicalRegistryListService,
			ScheduleService scheduleService, StatusIsApprovedService statusIsApprovedService,
			DownloadPDFService downloadPDFService) {
		super();
		this.jwtService = jwtService;
		this.userService = userService;
		this.verifyEmailService = verifyEmailService;
		this.mailSenderService = mailSenderService;
		this.environment = environment;
		this.medicalRegistryListService = medicalRegistryListService;
		this.scheduleService = scheduleService;
		this.statusIsApprovedService = statusIsApprovedService;
		this.downloadPDFService = downloadPDFService;
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
		CalendarFormat c = CalendarFormatUtil.parseStringToCalendarFormat(date);
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
	@CrossOrigin
	public ResponseEntity<Object> getAllStatusIsApproved() {
		return new ResponseEntity<>(statusIsApprovedService.findAllStatus(), HttpStatus.OK);
	}

	@GetMapping(value = "/all-register-schedule")
	@CrossOrigin
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

		String createdDate = params.getOrDefault("createdDate", "");
		if (!createdDate.isBlank()) {
			CalendarFormat cd = CalendarFormatUtil.parseStringToCalendarFormat(createdDate);
			mrls = medicalRegistryListService.sortByCreatedDate(mrls, cd.getYear(), cd.getMonth(),
					cd.getDay());

		}

		String registerDate = params.getOrDefault("registerDate", "");
		if (!registerDate.isBlank()) {
			CalendarFormat c = CalendarFormatUtil.parseStringToCalendarFormat(registerDate);
			Schedule schedule = scheduleService.findByDayMonthYear(c.getYear(), c.getMonth(),
					c.getDay());
			if (schedule != null)
				mrls = medicalRegistryListService.sortBySchedule(mrls, schedule);
			else {
				mrls.clear();
				Page<MedicalRegistryList> mrlsPaginated = medicalRegistryListService
						.findMrlsPaginated(page,
								size, mrls);

				return new ResponseEntity<>(mrlsPaginated, HttpStatus.OK);
			}

		}

		String status = params.getOrDefault("status", "ALL");
		StatusIsApproved statusIsApproved = statusIsApprovedService.findByStatus(status);

		if (statusIsApproved != null) {
			mrls = medicalRegistryListService.sortByStatusIsApproved(mrls, statusIsApproved);
		}

		for (Integer i = 0; i < mrls.size(); i++)
			mrls.get(i).setOrder(i + 1);

		Page<MedicalRegistryList> mrlsPaginated = medicalRegistryListService.findMrlsPaginated(page,
				size, mrls);

		return new ResponseEntity<>(mrlsPaginated, HttpStatus.OK);
	}

	@GetMapping(value = "/get-all-users/")
	@CrossOrigin
	public ResponseEntity<Object> getAllUsers() {
		return new ResponseEntity<>(userService.findAllUsers(), HttpStatus.OK);
	}

	@PostMapping(value = "/get-users-schedule-status/")
	@CrossOrigin
	public ResponseEntity<Object> getUsersByScheduleAndStatus(
			@RequestBody RegisterStatusDto registerStatusDto) {
		StatusIsApproved statusIsApproved = statusIsApprovedService
				.findByStatus("CHECKING");

		Schedule schedule = scheduleService.findByDate(registerStatusDto.getRegisterDate());

		if (statusIsApproved == null || schedule == null)
			return new ResponseEntity<>("Không có email đăng kí khám ngày này",
					HttpStatus.NOT_FOUND);

		List<User> users = medicalRegistryListService.findUniqueUser(schedule, statusIsApproved);
		if (users.size() < 1)
			return new ResponseEntity<>("Không có email đăng kí khám ngày này",
					HttpStatus.NOT_FOUND);

		return new ResponseEntity<>(users, HttpStatus.OK);
	}

	@PostMapping(value = "/auto-confirm-registers/")
	@CrossOrigin
	public ResponseEntity<Object> autoConfirmRegisters(

			@RequestBody ConfirmRegisterDto confirmRegisterDto) {

		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		StatusIsApproved statusIsApproved = statusIsApprovedService
				.findByStatus(confirmRegisterDto.getStatus());

		Schedule schedule = scheduleService.findByDate(confirmRegisterDto.getRegisterDate());

		if (statusIsApproved == null || schedule == null)
			return new ResponseEntity<>("Trạng thái hoặc ngày này chưa có đơn đăng kí khám",
					HttpStatus.NOT_FOUND);

		List<MedicalRegistryList> mrls = medicalRegistryListService
				.findByScheduleAndStatusIsApproved2(schedule, statusIsApprovedService
						.findByStatus("CHECKING"));
		if (mrls.size() < 1)
			return new ResponseEntity<>("Không tồn tại đơn đăng kí để xét duyệt vào ngày này",
					HttpStatus.NOT_FOUND);
		List<String> emails = confirmRegisterDto.getEmails();

		// cần unique email khi lấy đc các list trong medicalRegistryList / 1 ngày
		if (!emails.isEmpty()) {
			mrls.forEach(mrl -> {
				if (emails.contains(mrl.getUser().getEmail())
						&& mrl.getStatusIsApproved().getStatus().equals("CHECKING")) {

					try {
						medicalRegistryListService.createQRCodeAndUpLoadCloudinaryAndSetStatus(mrl,
								statusIsApproved);
					} catch (Exception e) {

						System.out.println("Lỗi");
					}

					try {
						mailSenderService.sendStatusRegisterEmail(mrl,
								confirmRegisterDto.getEmailContent());
					} catch (UnsupportedEncodingException | MessagingException e1) {
						System.out.println("Không gửi được mail !");
					}
				}
			});
			return new ResponseEntity<>("Thành công", HttpStatus.OK);
		}

		mrls.forEach(mrl -> {
			if (mrl.getStatusIsApproved().getStatus().equals("CHECKING")) {
				try {
					medicalRegistryListService.createQRCodeAndUpLoadCloudinaryAndSetStatus(mrl,
							statusIsApproved);
				} catch (Exception e) {
					System.out.println("Lỗi");
				}

				try {
					mailSenderService.sendStatusRegisterEmail(mrl,
							confirmRegisterDto.getEmailContent());
				} catch (UnsupportedEncodingException | MessagingException e1) {
					System.out.println("Không gửi được mail !");
				}
			}
		});

		return new ResponseEntity<>(mrls, HttpStatus.OK);
	}

	@PostMapping(value = "/take-order-from-qrCode/")
	@CrossOrigin
	public ResponseEntity<Object> getOrderFromQrCode(@RequestBody MrlIdScanQrDto mrlIdScanQrDto) {

		MedicalRegistryList mrl = medicalRegistryListService.findById(mrlIdScanQrDto.getMrlId());
		if (mrl == null)
			return new ResponseEntity<Object>("Đơn đăng kí này không tồn tại trong hệ thống !",
					HttpStatus.NOT_FOUND);
		if (!mrl.getStatusIsApproved().getStatus().equals("SUCCESS"))
			return new ResponseEntity<Object>("Mã QR này đã qua sử dụng !", HttpStatus.NOT_FOUND);

		StatusIsApproved statusIsApproved = statusIsApprovedService.findByStatus("PROCESSING");
		mrl.setStatusIsApproved(statusIsApproved);
		medicalRegistryListService.saveMedicalRegistryList(mrl);

		Integer order = medicalRegistryListService
				.countMRLByScheduleAndProcessingStatus(mrl.getSchedule(), statusIsApproved);
		mrl.setOrder(order);

		OrderQrCodeDto orderQrCodeDto = new OrderQrCodeDto(order, mrl.getName(),
				mrl.getUser().getPhone(), mrl.getSchedule().getDate());

		try {
			downloadPDFService.generateAndSaveLocation(mrl);
		} catch (IOException e) {
			System.out.println("Lỗi lưu file");
		}

		return new ResponseEntity<>(orderQrCodeDto, HttpStatus.OK);
	}

	@PostMapping(value = "/direct-register/")
	@CrossOrigin
	public ResponseEntity<Object> directRegister(@RequestBody DirectRegisterDto directRegisterDto) {

		User currentUser = userService.getCurrentLoginUser();
		User registerUser = userService.findByEmail(directRegisterDto.getEmail());

		if (currentUser == null || registerUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		CalendarFormat c = CalendarFormatUtil
				.parseStringToCalendarFormat(String.valueOf(new Date()));
		Schedule schedule = scheduleService.findByDayMonthYear(c.getYear(), c.getMonth(),
				c.getDay());

		if (schedule == null) {
			schedule = new Schedule();
			schedule.setDate(new Date());
			schedule.setIsDayOff(false);
			scheduleService.saveSchedule(schedule);
		}

		Integer countMedicalRegistryList = medicalRegistryListService
				.countMRLByUserAndScheduleAndisCancelled(currentUser, schedule, false);

		if (countMedicalRegistryList >= Integer
				.parseInt(environment.getProperty("register_schedule_per_day_max")))
			return new ResponseEntity<>("Tài khoản này đã đăng kí hạn mức 4 lần / 1 ngày",
					HttpStatus.UNAUTHORIZED);

		StatusIsApproved statusIsApproved = statusIsApprovedService.findByStatus("SUCCESS");
		MedicalRegistryList mrl = new MedicalRegistryList();
		mrl.setCreatedDate(new Date());
		mrl.setStatusIsApproved(statusIsApproved);
		mrl.setFavor(directRegisterDto.getFavor());
		mrl.setIsCanceled(false);
		mrl.setUser(registerUser);

		mrl.setSchedule(schedule);
		mrl.setName(directRegisterDto.getName());
		medicalRegistryListService.saveMedicalRegistryList(mrl);

		try {
			medicalRegistryListService.createQRCodeAndUpLoadCloudinaryAndSetStatus(mrl,
					statusIsApproved);
		} catch (Exception e) {
			System.out.println("Lỗi");
		}

		try {
			mailSenderService.sendStatusRegisterEmail(mrl, "Direct regiter");
		} catch (UnsupportedEncodingException | MessagingException e1) {
			System.out.println("Không gửi được mail !");
		}

		return new ResponseEntity<>(
				"Đặt lịch thành công , vui lòng kiểm tra mail lấy mã QR để lấy số thứ tự",
				HttpStatus.CREATED);
	}

}
