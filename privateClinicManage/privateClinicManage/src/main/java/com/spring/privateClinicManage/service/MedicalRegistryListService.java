package com.spring.privateClinicManage.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.spring.privateClinicManage.entity.MedicalRegistryList;
import com.spring.privateClinicManage.entity.Schedule;
import com.spring.privateClinicManage.entity.User;

public interface MedicalRegistryListService {

	void saveMedicalRegistryList(MedicalRegistryList medicalRegistryList);

	MedicalRegistryList findMRLByUserAndSchedule(User user, Schedule schedule);

	Integer countMRLByUserAndScheduleAndisCancelled(User user,
			Schedule schedule, Boolean isCanceled);

	List<MedicalRegistryList> findByUser(User user);

	MedicalRegistryList findById(Integer id);

	Page<MedicalRegistryList> findByUserPaginated(Pageable pageable);


}
