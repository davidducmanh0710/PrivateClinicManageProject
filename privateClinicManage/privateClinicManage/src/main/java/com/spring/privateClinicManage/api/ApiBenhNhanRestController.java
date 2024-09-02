package com.spring.privateClinicManage.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.privateClinicManage.dto.ApplyVoucherDto;
import com.spring.privateClinicManage.dto.RegisterScheduleDto;
import com.spring.privateClinicManage.entity.MedicalRegistryList;
import com.spring.privateClinicManage.entity.Schedule;
import com.spring.privateClinicManage.entity.StatusIsApproved;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.entity.UserVoucher;
import com.spring.privateClinicManage.entity.Voucher;
import com.spring.privateClinicManage.service.MedicalRegistryListService;
import com.spring.privateClinicManage.service.ScheduleService;
import com.spring.privateClinicManage.service.StatusIsApprovedService;
import com.spring.privateClinicManage.service.UserService;
import com.spring.privateClinicManage.service.UserVoucherService;
import com.spring.privateClinicManage.service.VoucherService;

@RestController
@RequestMapping("/api/benhnhan/")
public class ApiBenhNhanRestController {

	private UserService userService;
	private Environment environment;
	private ScheduleService scheduleService;
	private MedicalRegistryListService medicalRegistryListService;
	private StatusIsApprovedService statusIsApprovedService;
	private SimpMessagingTemplate messagingTemplate;
	private VoucherService voucherService;
	private UserVoucherService userVoucherService;

	@Autowired
	public ApiBenhNhanRestController(UserService userService, Environment environment,
			ScheduleService scheduleService, MedicalRegistryListService medicalRegistryListService,
			StatusIsApprovedService statusIsApprovedService,
			SimpMessagingTemplate messagingTemplate, VoucherService voucherService,
			UserVoucherService userVoucherService) {
		super();
		this.userService = userService;
		this.environment = environment;
		this.scheduleService = scheduleService;
		this.medicalRegistryListService = medicalRegistryListService;
		this.statusIsApprovedService = statusIsApprovedService;
		this.messagingTemplate = messagingTemplate;
		this.voucherService = voucherService;
		this.userVoucherService = userVoucherService;
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

		messagingTemplate.convertAndSend("/notify/registerContainer/",
				medicalRegistryList);

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

		if (!medicalRegistryList.getStatusIsApproved().getStatus().equals("CHECKING"))
			return new ResponseEntity<>("Đã thanh toán , không thể hủy !",
					HttpStatus.NOT_FOUND);

		StatusIsApproved statusIsApproved = statusIsApprovedService.findByStatus("CANCELED");

		medicalRegistryList.setIsCanceled(true);
		medicalRegistryList.setStatusIsApproved(statusIsApproved);
		medicalRegistryListService.saveMedicalRegistryList(medicalRegistryList);

		return new ResponseEntity<>("Đã hủy lịch thành công !", HttpStatus.OK);

	}

	@PostMapping(value = "/apply-voucher/")
	@CrossOrigin
	public ResponseEntity<Object> applyVoucher(@RequestBody ApplyVoucherDto applyVoucherDto) {
		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);
		
		String code = applyVoucherDto.getCode();
		
		Voucher voucher = voucherService.findVoucherByCode(code);

		if (voucher == null)
			return new ResponseEntity<>("Mã giảm giá này không tồn tại !", HttpStatus.NOT_FOUND);

		if (voucher.getIsActived() == false)
			return new ResponseEntity<>("Mã giảm giá này không có hiệu lực !",
					HttpStatus.UNAUTHORIZED);

		Date expiredDate = voucher.getVoucherCondition().getExpiredDate();
		if (expiredDate.compareTo(new Date()) < 0)
			return new ResponseEntity<>("Mã giảm giá này đã hết hạn sử dụng !",
					HttpStatus.UNAUTHORIZED);
		
		UserVoucher userVoucher = userVoucherService.findByUserAndVoucher(currentUser,
				voucher);
		if (userVoucher != null)
			if (userVoucher.getIsUsed())
				return new ResponseEntity<>("Bạn đã sử dụng mã giảm giá này !",
						HttpStatus.UNAUTHORIZED);

		return new ResponseEntity<>(voucher, HttpStatus.OK);

	}

}
