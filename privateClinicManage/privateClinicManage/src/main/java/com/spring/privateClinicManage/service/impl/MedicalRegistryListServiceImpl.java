package com.spring.privateClinicManage.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.privateClinicManage.entity.MedicalRegistryList;
import com.spring.privateClinicManage.entity.Schedule;
import com.spring.privateClinicManage.entity.User;
import com.spring.privateClinicManage.repository.MedicalRegistryListRepository;
import com.spring.privateClinicManage.service.MedicalRegistryListService;

import jakarta.transaction.Transactional;

@Service
public class MedicalRegistryListServiceImpl implements MedicalRegistryListService {

	@Autowired
	private MedicalRegistryListRepository medicalRegistryListRepository;

	@Override
	@Transactional
	public void saveMedicalRegistryList(MedicalRegistryList medicalRegistryList) {
		medicalRegistryListRepository.save(medicalRegistryList);
	}

	@Override
	public MedicalRegistryList findMRLByUserAndSchedule(User user, Schedule schedule) {
		return medicalRegistryListRepository.findMRLByUserAndSchedule(user, schedule);
	}

}
