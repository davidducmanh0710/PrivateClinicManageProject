package com.spring.privateClinicManage.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.spring.privateClinicManage.dto.UserRegisterDto;
import com.spring.privateClinicManage.entity.User;


public interface UserService extends UserDetailsService {

	User findByEmail(String email);

	void saveUser(User user);

	void saveUserRegisterDto(UserRegisterDto registerDto);

	boolean authUser(String email, String password);

	User getCurrentLoginUser();

	void setCloudinaryField(User user);

}
