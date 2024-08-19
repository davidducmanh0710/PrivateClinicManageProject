package com.spring.privateClinicManage.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.spring.privateClinicManage.entity.Medicine;
import com.spring.privateClinicManage.entity.MedicineGroup;
import com.spring.privateClinicManage.entity.Role;
import com.spring.privateClinicManage.entity.UnitMedicineType;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.service.MedicineGroupService;
import com.spring.privateClinicManage.service.MedicineService;
import com.spring.privateClinicManage.service.RoleService;
import com.spring.privateClinicManage.service.UnitMedicineTypeService;
import com.spring.privateClinicManage.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AdminController {

	@Autowired
	private UserService userService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private PasswordEncoder encoder;
	@Autowired
	private UnitMedicineTypeService unitMedicineTypeService;
	@Autowired
	private MedicineGroupService medicineGroupService;
	@Autowired
	private MedicineService medicineService;

	@ModelAttribute
	public void addAttributes(Model model) {
		User user = userService.getCurrentLoginUser();
		model.addAttribute("currentUser", user);

		List<Role> roles = roleService.findAllRoles();
		model.addAttribute("roles", roles);

	}

	@GetMapping("/login")
	public String showSignInForm() {
		return "/admin/authenticate/login";
	}

	@GetMapping("/admin")
	public String index() {
		return "admin/index";
	}

	@GetMapping("/admin/usersList")
	public String getUsersList(Model model, @RequestParam Map<String, String> params) {

		Integer page = Integer.parseInt(params.getOrDefault("page", "1"));
		Integer size = Integer.parseInt(params.getOrDefault("size", "5"));
		String sortIsActived = params.getOrDefault("sortIsActived", "none");
		String sortRole = params.getOrDefault("sortRole", "none");
		String anyKey = params.getOrDefault("sortAnyText", "");

		List<User> users = userService.findAllUsers();

		if (!anyKey.isBlank())
			users = userService.findByAnyText(anyKey);

		if (!sortIsActived.isBlank() && !sortIsActived.equals("none")) {
			users = userService.sortByActive(users, sortIsActived);
		}

		if (!sortRole.isBlank() && !sortRole.equals("none")) {
			Role role = roleService.findByName(sortRole);
			if (role != null)
				users = userService.sortByRole(users, role);

		}

		Page<User> userListsPaginated;

		page = page > 0 ? page : 1;
		size = size > 0 ? size : 5;

		if (!anyKey.isBlank() || !sortIsActived.isBlank() && !sortIsActived.equals("none")
				|| !sortRole.isBlank() && !sortRole.equals("none")) {

			userListsPaginated = userService.findSortedPaginateUser(size, page, users);

			model.addAttribute("sortAnyText", anyKey);
			model.addAttribute("sortIsActived", sortIsActived);
			model.addAttribute("sortRole", sortRole);

		} else {
			userListsPaginated = userService
					.findAllUserPaginated(
							PageRequest.of(page - 1, size));
		}

		Integer totalPages = userListsPaginated.getTotalPages();
		if (totalPages > 0) {
			List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
					.boxed()
					.collect(Collectors.toList());
			model.addAttribute("pageNumbers", pageNumbers);
		}

		model.addAttribute("userListsPaginated", userListsPaginated);
		model.addAttribute("page", page);
		model.addAttribute("size", size);

		return "admin/user/usersList";
	}

	@GetMapping("/admin/addNewUser")
	public String getFormAddNewUser(Model model) {
		model.addAttribute("user", new User());
		return "admin/user/addOrUpdateUser";
	}

	@GetMapping("/admin/updateUser/{userId}")
	public String getFormUpdateUser(Model model, @PathVariable("userId") Integer userId) {
		User user = userService.findUserById(userId);
		model.addAttribute("user", user);
		return "admin/user/addOrUpdateUser";
	}

	@PostMapping("/admin/addOrUpdateUser")
	public String addOrUpdateUser(Model model, @ModelAttribute("user") User user,
			@RequestParam Map<String, String> params,
			@RequestPart("avatarFile") MultipartFile avatarFile) throws ParseException {

		String isActived = params.getOrDefault("active", "false");
		String gender = params.getOrDefault("inlineRadioOptions", "male");
		String roleName = params.getOrDefault("selectRole", "ROLE_BENHNHAN");
		Role role = roleService.findByName(roleName);

		if (user.getId() != null && user.getId() > 0) {
			User existUser = userService.findUserById(user.getId());
			if (isActived != null && isActived.equals("true"))
				existUser.setActive(true);
			else
				existUser.setActive(false);
			existUser.setGender(gender);
			existUser.setRole(role);
			existUser.setBirthday(user.getBirthday());
			userService.saveUser(existUser);

			if (avatarFile.getOriginalFilename() != ""
					|| !avatarFile.getOriginalFilename().isEmpty()) {
				existUser.setFile(avatarFile);
				userService.setCloudinaryField(existUser);
			}

			return "redirect:/admin/usersList";
		}

		if (isActived != null && isActived.equals("true"))
			user.setActive(true);
		else
			user.setActive(false);

		user.setGender(gender);

		user.setRole(role);

		user.setPassword(encoder.encode(user.getPassword()));
		user.setFile(avatarFile);
		userService.setCloudinaryField(user);

		return "redirect:/admin/usersList";
	}

	@GetMapping("/admin/unit-medicine-type-list")
	public String getUmtList(Model model, @RequestParam Map<String, String> params) {

		Integer page = Integer.parseInt(params.getOrDefault("page", "1"));
		Integer size = Integer.parseInt(params.getOrDefault("size", "5"));
		String name = params.getOrDefault("name", "");

		List<UnitMedicineType> umts = new ArrayList<>();

		UnitMedicineType umt;
		if (!name.isBlank()) {
			umt = unitMedicineTypeService.findUtmByUnitName(name);
			if (umt != null)
				umts.add(umt);
		} else {
			umts = unitMedicineTypeService.findAllUmt();
		}

		page = page > 0 ? page : 1;
		size = size > 0 ? size : 5;

		Page<UnitMedicineType> umtsPaginated = unitMedicineTypeService.paginateUmtList(size, page,
				umts);

		Integer totalPages = umtsPaginated.getTotalPages();
		if (totalPages > 0) {
			List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
					.boxed()
					.collect(Collectors.toList());
			model.addAttribute("pageNumbers", pageNumbers);
		}

		model.addAttribute("umtsPaginated", umtsPaginated);
		model.addAttribute("page", page);
		model.addAttribute("size", size);
		model.addAttribute("name", name);

		return "/admin/medicine/medicineUnitList";
	}

	@GetMapping("/admin/addNewUmt")
	public String getFormAddUmt(Model model) {
		model.addAttribute("umt", new UnitMedicineType());
		return "admin/medicine/addOrUpdateUmt";
	}

	@GetMapping("/admin/updateUmt/{umtId}")
	public String getFormUpdateUmt(Model model, @PathVariable("umtId") Integer umtId) {
		UnitMedicineType unitMedicineType = unitMedicineTypeService.findUtmById(umtId);
		model.addAttribute("umt", unitMedicineType);
		return "admin/medicine/addOrUpdateUmt";
	}

	@PostMapping("/admin/addOrUpdateUmt")
	public String addOrUpdateUmt(Model model, @Valid @ModelAttribute("umt") UnitMedicineType umt,
			BindingResult bindingResult, @RequestParam Map<String, String> params)
			throws ParseException {

		UnitMedicineType existUtm = unitMedicineTypeService
				.findUtmByUnitName(umt.getUnitName());

		if (umt.getId() == null && existUtm != null)
			bindingResult.rejectValue("unitName", null,
					"Đã tồn tại tên đơn vị này !");

		if (bindingResult.hasErrors()) {
			model.addAttribute("umt", umt);
			return "admin/medicine/addOrUpdateUmt";
		}

		unitMedicineTypeService.saveUnitMedicineType(umt);

		return "redirect:/admin/unit-medicine-type-list";

	}

	@GetMapping("/admin/medicine-group-list")
	public String getMedicineGroupList(Model model, @RequestParam Map<String, String> params) {

		Integer page = Integer.parseInt(params.getOrDefault("page", "1"));
		Integer size = Integer.parseInt(params.getOrDefault("size", "5"));
		String name = params.getOrDefault("name", "");

		List<MedicineGroup> medicineGroups = new ArrayList<>();
		MedicineGroup medicineGroup;
		if (!name.isBlank()) {
			medicineGroup = medicineGroupService.findMedicineByGroupByName(name);
			if (medicineGroup != null)
				medicineGroups.add(medicineGroup);
		} else {
			medicineGroups = medicineGroupService.findAllMedicineGroup();
		}

		page = page > 0 ? page : 1;
		size = size > 0 ? size : 5;

		Page<MedicineGroup> medicineGroupsPaginated = medicineGroupService
				.paginateMedicineGroupList(size, page,
						medicineGroups);

		Integer totalPages = medicineGroupsPaginated.getTotalPages();
		if (totalPages > 0) {
			List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
					.boxed()
					.collect(Collectors.toList());
			model.addAttribute("pageNumbers", pageNumbers);
		}

		model.addAttribute("medicineGroupsPaginated", medicineGroupsPaginated);
		model.addAttribute("page", page);
		model.addAttribute("size", size);
		model.addAttribute("name", name);

		return "/admin/medicine/medicineGroupList";
	}

	@GetMapping("/admin/addNewMedicineGroup")
	public String getFormAddMedicineGroup(Model model) {
		model.addAttribute("medicineGroup", new MedicineGroup());
		return "admin/medicine/addOrUpdateMedicineGroup";
	}

	@GetMapping("/admin/updateMedicineGroup/{medicineGroupId}")
	public String getFormUpdateMedicineGroup(Model model,
			@PathVariable("medicineGroupId") Integer medicineGroupId) {
		MedicineGroup medicineGroup = medicineGroupService.findMedicineGroupById(medicineGroupId);
		model.addAttribute("medicineGroup", medicineGroup);
		return "admin/medicine/addOrUpdateMedicineGroup";
	}

	@PostMapping("/admin/addOrUpdateMedicineGroup")
	public String addOrUpdateUmt(Model model,
			@Valid @ModelAttribute("medicineGroup") MedicineGroup medicineGroup,
			BindingResult bindingResult, @RequestParam Map<String, String> params)
			throws ParseException {

		MedicineGroup medicineGroupExist = medicineGroupService
				.findMedicineByGroupByName(medicineGroup.getGroupName());

		if (medicineGroup.getId() == null && medicineGroupExist != null)
			bindingResult.rejectValue("groupName", null,
					"Đã tồn tại tên nhóm thuốc này !");

		if (bindingResult.hasErrors()) {
			model.addAttribute("medicineGroup", medicineGroup);
			return "admin/medicine/addOrUpdateMedicineGroup";
		}

		medicineGroupService.saveMedicineGroup(medicineGroup);

		return "redirect:/admin/medicine-group-list";

	}

	@GetMapping("/admin/medicinesList")
	public String getMedicinesList(Model model, @RequestParam Map<String, String> params) {

		Integer page = Integer.parseInt(params.getOrDefault("page", "1"));
		Integer size = Integer.parseInt(params.getOrDefault("size", "5"));
		String name = params.getOrDefault("name", "");
		String sortByUmt = params.getOrDefault("sortByUmt", "none");
		String sortByGroup = params.getOrDefault("sortByGroup", "none");

		List<Medicine> medicines = new ArrayList<>();

		if (!name.isBlank()) {
			medicines = medicineService.findByName(name);

		} else
			medicines = medicineService.findAllMedicines();

		UnitMedicineType unitMedicineType = unitMedicineTypeService
				.findUtmByUnitName(sortByUmt);
		if (!sortByUmt.isBlank() && unitMedicineType != null) {
			medicines = medicineService.sortByUtm(medicines, unitMedicineType);
		}

		MedicineGroup medicineGroup = medicineGroupService
				.findMedicineByGroupByName(sortByGroup);
		if (!sortByGroup.isBlank() && medicineGroup != null) {
			medicines = medicineService.sortByGroup(medicines, medicineGroup);
		}

		Page<Medicine> medicinesPaginated = medicineService.medicinesPaginated(page, size,
				medicines);

		page = page > 0 ? page : 1;
		size = size > 0 ? size : 5;

		Integer totalPages = medicinesPaginated.getTotalPages();
		if (totalPages > 0) {
			List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
					.boxed()
					.collect(Collectors.toList());
			model.addAttribute("pageNumbers", pageNumbers);
		}

		model.addAttribute("medicinesPaginated", medicinesPaginated);
		model.addAttribute("sortByUmt", sortByUmt);
		model.addAttribute("sortByGroup", sortByGroup);
		model.addAttribute("page", page);
		model.addAttribute("size", size);
		model.addAttribute("name", name);

		List<UnitMedicineType> umtsInit = unitMedicineTypeService.findAllUmt();
		List<MedicineGroup> medicineGroupsInit = medicineGroupService.findAllMedicineGroup();

		model.addAttribute("umts", umtsInit);
		model.addAttribute("medicineGroups", medicineGroupsInit);

		return "admin/medicine/medicinesList";
	}

	@GetMapping("/admin/addNewMedicine")
	public String getFormAddNewMedicine(Model model) {
		Medicine medicine = new Medicine();
		model.addAttribute("medicine", medicine);

		List<UnitMedicineType> umts = unitMedicineTypeService.findAllUmt();
		List<MedicineGroup> medicineGroups = medicineGroupService.findAllMedicineGroup();

		model.addAttribute("umts", umts);
		model.addAttribute("medicineGroups", medicineGroups);

		return "/admin/medicine/addOrUpdateMedicine";
	}

	@GetMapping("/admin/updateMedicine/{medicineId}")
	public String getFormUpdateMedicine(Model model,
			@PathVariable("medicineId") Integer medicineId) {
		Medicine medicine = medicineService.findById(medicineId);
		model.addAttribute("medicine", medicine);

		List<UnitMedicineType> umts = unitMedicineTypeService.findAllUmt();
		List<MedicineGroup> medicineGroups = medicineGroupService.findAllMedicineGroup();

		model.addAttribute("umts", umts);
		model.addAttribute("medicineGroups", medicineGroups);

		return "admin/medicine/addOrUpdateMedicine";
	}

	@PostMapping("/admin/addOrUpdateMedicine")
	public String addOrUpdateUmt(Model model,
			@Valid @ModelAttribute("medicine") Medicine medicine,
			BindingResult bindingResult, @RequestParam Map<String, String> params)
			throws ParseException {

		List<Medicine> medicineExist = medicineService
				.findByName(medicine.getName());

		if (medicine.getId() == null && medicineExist.size() > 0)
			bindingResult.rejectValue("name", null,
					"Đã tồn tại tên thuốc này !");

		if (bindingResult.hasErrors()) {
			model.addAttribute("medicine", medicine);
			return "admin/medicine/addOrUpdateMedicine";
		}

		medicineService.saveMedicine(medicine);

		return "redirect:/admin/medicinesList";

	}

}
