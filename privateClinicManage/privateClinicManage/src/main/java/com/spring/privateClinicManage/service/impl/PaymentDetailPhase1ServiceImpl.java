package com.spring.privateClinicManage.service.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.privateClinicManage.entity.PaymentDetailPhase1;
import com.spring.privateClinicManage.repository.PaymentDetailPhase1Repository;
import com.spring.privateClinicManage.service.PaymentDetailPhase1Service;

@Service
public class PaymentDetailPhase1ServiceImpl implements PaymentDetailPhase1Service {

	@Autowired
	private PaymentDetailPhase1Repository paymentDetailPhase1Repository;

	@Override
	public void savePdp1(PaymentDetailPhase1 paymentDetailPhase1) {
		paymentDetailPhase1Repository.save(paymentDetailPhase1);
	}

	@Override
	public Integer getMrlIdFromMSPDKLK(String des) {

		String regex = "#MSPDKLK([1-1000])";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(des);

		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return null;
	}

}
