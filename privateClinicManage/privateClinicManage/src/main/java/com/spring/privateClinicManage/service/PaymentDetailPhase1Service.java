package com.spring.privateClinicManage.service;

import com.spring.privateClinicManage.entity.PaymentDetailPhase1;

public interface PaymentDetailPhase1Service {

	void savePdp1(PaymentDetailPhase1 paymentDetailPhase1);

	Integer getMrlIdFromMSPDKLK(String des);
}
