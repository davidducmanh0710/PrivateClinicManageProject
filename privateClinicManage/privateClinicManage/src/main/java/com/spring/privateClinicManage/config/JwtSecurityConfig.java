package com.spring.privateClinicManage.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.spring.privateClinicManage.filter.CustomAccessDeniedHandler;
import com.spring.privateClinicManage.filter.JwtAuthenticationTokenFilter;
import com.spring.privateClinicManage.filter.RestAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableTransactionManagement
@Order(1)
public class JwtSecurityConfig {

	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter() throws Exception {
		JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter = new JwtAuthenticationTokenFilter();
		return jwtAuthenticationTokenFilter;
	}

	@Bean
	public RestAuthenticationEntryPoint restServicesEntryPoint() {
		return new RestAuthenticationEntryPoint();
	}

	@Bean
	public CustomAccessDeniedHandler customAccessDeniedHandler() {
		return new CustomAccessDeniedHandler();
	}

	@Bean
	public SecurityFilterChain jwtSecurityfilterChain(HttpSecurity http) throws Exception {

		http.securityMatcher("/api/**").authorizeHttpRequests(auth -> auth

				.requestMatchers(HttpMethod.GET,
						"/api/users/getAllStatusIsApproved/",
						"/api/qr/barcodes/zxing/qrcode/",
						"/api/payment/momo/phase1-return/",
						"/api/payment/vnpay/phase1-return/")
				.permitAll()

				.requestMatchers(HttpMethod.POST,
						"/api/users/login/",
						"/api/users/register/",
						"/api/users/verify-email/",
						"/api/pdf/generate/",
						"/api/users/take-order-from-qrCode/")
				.permitAll()

				.requestMatchers(HttpMethod.POST,
						"/api/benhnhan/register-schedule/",
						"/api/benhnhan/user-register-schedule-list/",
						"/api/benhnhan/apply-voucher/",
						"/api/payment/**")
				.hasRole("BENHNHAN")

				.requestMatchers(HttpMethod.PATCH,
						"/api/benhnhan/cancel-register-schedule/{registerScheduleId}/")
				.hasRole("BENHNHAN")

				.requestMatchers(HttpMethod.GET,
						"/api/yta/all-register-schedule/",
						"/api/yta/get-all-users/")
				.hasRole("YTA")

				.requestMatchers(HttpMethod.POST,
						"/api/yta/get-users-schedule-status/",
						"/api/yta/auto-confirm-registers/",
						"/api/yta/direct-register/")
				.hasRole("YTA")

				.requestMatchers(HttpMethod.GET,
						"/api/bacsi/get-all-processing-user-today/",
						"/api/bacsi/get-all-medicine-group/",
						"/api/bacsi/get-all-medicine-by-group/{medicineGroupId}/",
						"/api/bacsi/get-medicine-by-id/{medicineId}/",
						"/api/bacsi/get-all-medicines/",
						"/api/bacsi/get-prescriptionItems-by-medicalExam-id/{medicalExamId}/")
				.hasRole("BACSI")

				.requestMatchers(HttpMethod.POST,
						"/api/bacsi/submit-medical-examination/",
						"/api/bacsi/get-history-user-register/")
				.hasRole("BACSI")

				.anyRequest().authenticated())
				.httpBasic(httpbc -> httpbc
						.authenticationEntryPoint(restServicesEntryPoint()))
				.sessionManagement(smc -> smc
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exp -> exp.authenticationEntryPoint(restServicesEntryPoint())
						.accessDeniedHandler(customAccessDeniedHandler()))
				.addFilterBefore(
						jwtAuthenticationTokenFilter(),
						UsernamePasswordAuthenticationFilter.class)
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable());
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("*"));
		configuration.addAllowedHeader("*");
		configuration.addExposedHeader("*");
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

}
