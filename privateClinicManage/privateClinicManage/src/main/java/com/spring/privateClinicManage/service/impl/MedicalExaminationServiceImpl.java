package com.spring.privateClinicManage.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.privateClinicManage.entity.MedicalExamination;
import com.spring.privateClinicManage.repository.MedicalExaminationRepository;
import com.spring.privateClinicManage.service.MedicalExaminationService;

import jakarta.transaction.Transactional;

@Service
public class MedicalExaminationServiceImpl implements MedicalExaminationService {

	@Autowired
	private MedicalExaminationRepository medicalExaminationRepository;

	@Override
	@Transactional
	public void saveMedicalExamination(MedicalExamination medicalExamination) {
		medicalExaminationRepository.save(medicalExamination);
	}

}
