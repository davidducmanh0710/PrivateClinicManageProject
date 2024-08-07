package com.spring.privateClinicManage.service.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.spring.privateClinicManage.dto.UserRegisterDto;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.repository.UserRepository;
import com.spring.privateClinicManage.service.RoleService;
import com.spring.privateClinicManage.service.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

	private UserRepository userRepository;
	private PasswordEncoder encoder;
	private Environment env;
	private RoleService roleService;
	private Cloudinary cloudinary;

	@Autowired
	public UserServiceImpl(UserRepository userRepository, PasswordEncoder encoder,
			Environment environment, RoleService roleService, Cloudinary cloudinary) {
		super();
		this.userRepository = userRepository;
		this.encoder = encoder;
		this.env = environment;
		this.roleService = roleService;
		this.cloudinary = cloudinary;
	}

	@Override
	public User findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email);
		if (user == null)
			throw new UsernameNotFoundException("Không tồn tại!");
		Set<GrantedAuthority> authorities = new HashSet<>();
		authorities.add(new SimpleGrantedAuthority(user.getRole().getName()));

		return new org.springframework.security.core.userdetails.User(user.getEmail(),
				user.getPassword(), authorities);
	}

	@Override
	@Transactional
	public void saveUser(User user) {
		userRepository.save(user);
	}

	@Override
	public boolean authUser(String email, String password) {
		User user = userRepository.findByEmail(email);

		return user != null && this.encoder.matches(password, user.getPassword());
	}

	@Override
	@Transactional
	public void saveUserRegisterDto(UserRegisterDto registerDto) {

		User user = new User();

		user.setAvatar(env.getProperty("user.avatar.default"));
		user.setName(registerDto.getName());
		user.setEmail(registerDto.getEmail());
		user.setPassword(encoder.encode(registerDto.getPassword()));
		user.setGender(registerDto.getGender());
		user.setAddress(registerDto.getAddress());
		user.setBirthday(registerDto.getBirthday());
		user.setPhone(registerDto.getPhone());
		user.setActive(true);
		user.setRole(roleService.findByName("ROLE_BENHNHAN"));

		userRepository.save(user);
	}

	@Override
	public User getCurrentLoginUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
			User user = this.findByEmail((authentication.getName()));
			return user;
		}
		return null;
	}

	@Override
	public void setCloudinaryField(User user) {
		if (!user.getFile().isEmpty()) {
			try {
				Map res = this.cloudinary.uploader().upload(user.getFile().getBytes(),
						ObjectUtils.asMap("resource_type", "auto"));
				user.setAvatar(res.get("secure_url").toString());
				user.setFile(null);
				this.userRepository.save(user);

			} catch (IOException ex) {
				Logger.getLogger(UserServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

}
