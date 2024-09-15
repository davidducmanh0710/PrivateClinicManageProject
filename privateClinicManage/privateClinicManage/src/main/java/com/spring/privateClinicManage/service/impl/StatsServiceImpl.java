package com.spring.privateClinicManage.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.privateClinicManage.repository.MedicineRepository;
import com.spring.privateClinicManage.repository.PaymentDetailRepository;
import com.spring.privateClinicManage.service.StatsService;

@Service
public class StatsServiceImpl implements StatsService {

	@Autowired
	private MedicineRepository medicineRepository;
	@Autowired
	private PaymentDetailRepository paymentDetailRepository;

	@Override
	public List<Object[]> statsByPrognosisMedicine(Integer year, Integer month) {
		return medicineRepository.statsByPrognosisMedicine(year, month);
	}

	@Override
	public List<Object[]> statsByRevenue(Integer year) {
		return paymentDetailRepository.statsByRevenue(year);
	}

}
