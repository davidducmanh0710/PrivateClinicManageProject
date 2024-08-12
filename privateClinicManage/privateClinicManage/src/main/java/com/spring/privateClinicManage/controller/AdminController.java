package com.spring.privateClinicManage.controller;

import java.text.ParseException;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.spring.privateClinicManage.entity.Role;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.service.RoleService;
import com.spring.privateClinicManage.service.UserService;

@Controller
public class AdminController {

	@Autowired
	private UserService userService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private PasswordEncoder encoder;

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

		return "admin/usersList";
	}

	@GetMapping("/admin/addNewUser")
	public String getFormAddNewUser(Model model) {
		model.addAttribute("user", new User());
		return "admin/addOrUpdateUser";
	}

	@GetMapping("/admin/updateUser/{userId}")
	public String getFormUpdateUser(Model model, @PathVariable("userId") Integer userId) {
		User user = userService.findUserById(userId);
		model.addAttribute("user", user);
		return "admin/addOrUpdateUser";
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

}
