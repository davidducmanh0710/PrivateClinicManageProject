package com.spring.privateClinicManage.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	@Override
	public Integer countMRLByUserAndScheduleAndisCancelled(User user, Schedule schedule,
			Boolean isCanceled) {
		return medicalRegistryListRepository.countMRLByUserAndScheduleAndisCancelled(user, schedule,
				isCanceled);
	}

	@Override
	public List<MedicalRegistryList> findByUser(User user) {
		return medicalRegistryListRepository.findByUser(user);
	}

	@Override
	public MedicalRegistryList findById(Integer id) {
		Optional<MedicalRegistryList> optional = medicalRegistryListRepository.findById(id);
		if (optional.isEmpty())
			return null;
		return optional.get();
	}

	@Override
	public Page<MedicalRegistryList> findByUserPaginated(Pageable pageable) {
		return medicalRegistryListRepository.findAll(pageable);
	}


}
