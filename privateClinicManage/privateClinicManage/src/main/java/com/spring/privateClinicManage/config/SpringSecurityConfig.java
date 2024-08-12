package com.spring.privateClinicManage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Order(2)
public class SpringSecurityConfig {

	@Bean
	public SecurityFilterChain springFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth
				.requestMatchers("/login/**", "/error", "/public/resources/**")
				.permitAll()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated())
				.formLogin((form) -> form.loginPage("/login").loginProcessingUrl("/login")
						.usernameParameter("email").passwordParameter("password")
						.defaultSuccessUrl("/admin").permitAll())
				.logout((logout) -> logout.permitAll())
				.cors(cors -> cors.disable())
				.csrf(csrf -> csrf.disable());

		return http.build();
	}

}
