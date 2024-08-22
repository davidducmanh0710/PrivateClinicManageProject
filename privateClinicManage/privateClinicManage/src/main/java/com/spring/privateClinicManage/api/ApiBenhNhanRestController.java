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

import com.spring.privateClinicManage.dto.RegisterScheduleDto;
import com.spring.privateClinicManage.entity.MedicalRegistryList;
import com.spring.privateClinicManage.entity.Schedule;
import com.spring.privateClinicManage.entity.StatusIsApproved;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.service.MedicalRegistryListService;
import com.spring.privateClinicManage.service.ScheduleService;
import com.spring.privateClinicManage.service.StatusIsApprovedService;
import com.spring.privateClinicManage.service.UserService;

@RestController
@RequestMapping("/api/benhnhan/")
public class ApiBenhNhanRestController {

	private UserService userService;
	private Environment environment;
	private ScheduleService scheduleService;
	private MedicalRegistryListService medicalRegistryListService;
	private StatusIsApprovedService statusIsApprovedService;
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	public ApiBenhNhanRestController(UserService userService, Environment environment,
			ScheduleService scheduleService, MedicalRegistryListService medicalRegistryListService,
			StatusIsApprovedService statusIsApprovedService,
			SimpMessagingTemplate messagingTemplate) {
		super();
		this.userService = userService;
		this.environment = environment;
		this.scheduleService = scheduleService;
		this.medicalRegistryListService = medicalRegistryListService;
		this.statusIsApprovedService = statusIsApprovedService;
		this.messagingTemplate = messagingTemplate;
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

		StatusIsApproved statusIsApproved = statusIsApprovedService.findByStatus("CANCELED");

		medicalRegistryList.setIsCanceled(true);
		medicalRegistryList.setStatusIsApproved(statusIsApproved);
		medicalRegistryListService.saveMedicalRegistryList(medicalRegistryList);

		return new ResponseEntity<>("Đã hủy lịch thành công !", HttpStatus.OK);

	}

}
