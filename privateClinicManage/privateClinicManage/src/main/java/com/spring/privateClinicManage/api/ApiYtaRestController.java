package com.spring.privateClinicManage.api;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.privateClinicManage.dto.ConfirmRegisterDto;
import com.spring.privateClinicManage.dto.DirectRegisterDto;
import com.spring.privateClinicManage.dto.RegisterStatusDto;
import com.spring.privateClinicManage.entity.MedicalRegistryList;
import com.spring.privateClinicManage.entity.Schedule;
import com.spring.privateClinicManage.entity.StatusIsApproved;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.service.DownloadPDFService;
import com.spring.privateClinicManage.service.MailSenderService;
import com.spring.privateClinicManage.service.MedicalRegistryListService;
import com.spring.privateClinicManage.service.ScheduleService;
import com.spring.privateClinicManage.service.StatusIsApprovedService;
import com.spring.privateClinicManage.service.UserService;
import com.spring.privateClinicManage.utilities.CalendarFormat;
import com.spring.privateClinicManage.utilities.CalendarFormatUtil;

import jakarta.mail.MessagingException;

@RestController
@RequestMapping("/api/yta")
public class ApiYtaRestController {

	private UserService userService;
	private MailSenderService mailSenderService;
	private Environment environment;
	private ScheduleService scheduleService;
	private MedicalRegistryListService medicalRegistryListService;
	private StatusIsApprovedService statusIsApprovedService;

	@Autowired
	public ApiYtaRestController(UserService userService, MailSenderService mailSenderService,
			Environment environment, ScheduleService scheduleService,
			MedicalRegistryListService medicalRegistryListService,
			StatusIsApprovedService statusIsApprovedService,
			DownloadPDFService downloadPDFService) {
		super();
		this.userService = userService;
		this.mailSenderService = mailSenderService;
		this.environment = environment;
		this.scheduleService = scheduleService;
		this.medicalRegistryListService = medicalRegistryListService;
		this.statusIsApprovedService = statusIsApprovedService;
	}

	// ROLE_YTA

	@GetMapping(value = "/all-register-schedule/")
	@CrossOrigin
	public ResponseEntity<Object> getAllRegisterSchedule(Model model,
			@RequestParam Map<String, String> params) {

		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		Integer page = Integer.parseInt(params.getOrDefault("page", "1"));
		Integer size = Integer.parseInt(params.getOrDefault("size", "6"));

		List<MedicalRegistryList> mrls;

		String key = params.getOrDefault("key", "");
		if (!key.isBlank())
			mrls = medicalRegistryListService.findByAnyKey(key);
		else
			mrls = medicalRegistryListService.findAllMrl();

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