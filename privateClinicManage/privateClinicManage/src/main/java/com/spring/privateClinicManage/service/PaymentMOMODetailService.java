package com.spring.privateClinicManage.service;

import java.util.Map;

import com.spring.privateClinicManage.entity.MedicalRegistryList;

public interface PaymentMOMODetailService {
	Map<String, Object> generateUrlPayment(Long amount, MedicalRegistryList mrl);

}
