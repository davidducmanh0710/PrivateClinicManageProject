package com.spring.privateClinicManage.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.privateClinicManage.entity.PrescriptionItems;
import com.spring.privateClinicManage.repository.PrescriptionItemsRepository;
import com.spring.privateClinicManage.service.PrescriptionItemsService;

import jakarta.transaction.Transactional;

@Service
public class PrescriptionItemsServiceImpl implements PrescriptionItemsService {

	@Autowired
	private PrescriptionItemsRepository prescriptionItemsRepository;

	@Override
	@Transactional
	public void savePrescriptionItems(PrescriptionItems prescriptionItems) {
		prescriptionItemsRepository.save(prescriptionItems);
	}

}
