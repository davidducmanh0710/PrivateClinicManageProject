package com.spring.privateClinicManage.service;

import java.io.UnsupportedEncodingException;

import com.spring.privateClinicManage.entity.MedicalRegistryList;

public interface PaymentVNPAYDetailService {

	String generateUrlPayment(Long amount, MedicalRegistryList mrl)
			throws UnsupportedEncodingException;
}
