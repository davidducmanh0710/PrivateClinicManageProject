package com.spring.privateClinicManage.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spring.privateClinicManage.dto.MedicalExamDto;
import com.spring.privateClinicManage.entity.MedicalRegistryList;
import com.spring.privateClinicManage.entity.Medicine;
import com.spring.privateClinicManage.entity.MedicineGroup;
import com.spring.privateClinicManage.entity.Schedule;
import com.spring.privateClinicManage.entity.StatusIsApproved;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.service.MedicalRegistryListService;
import com.spring.privateClinicManage.service.MedicineGroupService;
import com.spring.privateClinicManage.service.MedicineService;
import com.spring.privateClinicManage.service.ScheduleService;
import com.spring.privateClinicManage.service.StatusIsApprovedService;
import com.spring.privateClinicManage.service.UserService;
import com.spring.privateClinicManage.utilities.CalendarFormat;
import com.spring.privateClinicManage.utilities.CalendarFormatUtil;

@RestController
@RequestMapping("/api/bacsi")
public class ApiBacsiRestController {

	private UserService userService;
	private ScheduleService scheduleService;
	private MedicalRegistryListService medicalRegistryListService;
	private StatusIsApprovedService statusIsApprovedService;
	private MedicineGroupService medicineGroupService;
	private MedicineService medicineService;

	@Autowired
	public ApiBacsiRestController(UserService userService, ScheduleService scheduleService,
			MedicalRegistryListService medicalRegistryListService,
			StatusIsApprovedService statusIsApprovedService,
			MedicineGroupService medicineGroupService, MedicineService medicineService) {
		super();
		this.userService = userService;
		this.scheduleService = scheduleService;
		this.medicalRegistryListService = medicalRegistryListService;
		this.statusIsApprovedService = statusIsApprovedService;
		this.medicineGroupService = medicineGroupService;
		this.medicineService = medicineService;
	}

	// ROLE_BACSI

	@GetMapping("/get-all-processing-user-today/")
	@CrossOrigin
	public ResponseEntity<Object> getAllProcessingUserToday(
			@RequestParam Map<String, String> params) {

		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		Integer page = Integer.parseInt(params.getOrDefault("page", "1"));
		Integer size = Integer.parseInt(params.getOrDefault("size", "3"));

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

		StatusIsApproved statusIsApproved = statusIsApprovedService.findByStatus("PROCESSING");

		List<MedicalRegistryList> mrls = medicalRegistryListService
				.findByScheduleAndStatusIsApproved2(schedule, statusIsApproved);

		for (Integer i = 0; i < mrls.size(); i++)
			mrls.get(i).setOrder(i + 1);

		Page<MedicalRegistryList> mrlsPaginated = medicalRegistryListService
				.findMrlsPaginated(page,
						size, mrls);

		return new ResponseEntity<>(mrlsPaginated, HttpStatus.OK);

	}

	@GetMapping(value = "/get-all-medicine-group/")
	@CrossOrigin
	public ResponseEntity<Object> getAllMedicineGroup() {
		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		return new ResponseEntity<>(medicineGroupService.findAllMedicineGroup(), HttpStatus.OK);
	}

	@GetMapping(value = "/get-all-medicine-by-group/{medicineGroupId}/")
	@CrossOrigin
	public ResponseEntity<Object> getAllMedicineByGroup(
			@PathVariable("medicineGroupId") Integer medicineGroupId) {

		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		MedicineGroup medicineGroup = medicineGroupService.findMedicineGroupById(medicineGroupId);

		if (medicineGroup == null)
			return new ResponseEntity<>("Nhóm thuốc này không tồn tại", HttpStatus.NOT_FOUND);

		List<Medicine> medicines = medicineService.findByMedicineGroup(medicineGroup);

		return new ResponseEntity<>(medicines, HttpStatus.OK);
	}

	@GetMapping(value = "/get-medicine-by-id/{medicineId}/")
	@CrossOrigin
	public ResponseEntity<Object> getMedicineById(@PathVariable("medicineId") Integer medicineId) {

		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		Medicine medicine = medicineService.findById(medicineId);

		if (medicine == null)
			return new ResponseEntity<>("Thuốc này không tồn tại", HttpStatus.NOT_FOUND);

		return new ResponseEntity<>(medicine, HttpStatus.OK);

	}

	@GetMapping(value = "/get-all-medicines/")
	@CrossOrigin
	public ResponseEntity<Object> getAllMedicines() {
		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		return new ResponseEntity<>(medicineService.findAllMedicines(), HttpStatus.OK);
	}

	@PostMapping(value = "/submit-medical-examination/")
	@CrossOrigin
	public ResponseEntity<Object> submitMedicalExamination(
			@RequestBody MedicalExamDto medicalExamDto) {

		User currentUser = userService.getCurrentLoginUser();
		if (currentUser == null)
			return new ResponseEntity<>("Người dùng không tồn tại", HttpStatus.NOT_FOUND);

		MedicalRegistryList mrl = medicalRegistryListService.findById(medicalExamDto.getMrlId());
		if (mrl == null)
			return new ResponseEntity<>("Không tồn tại đơn hẹn khám cho đơn thuốc này",
					HttpStatus.NOT_FOUND);

		return new ResponseEntity<>("Thành công !", HttpStatus.CREATED);
	}

}
