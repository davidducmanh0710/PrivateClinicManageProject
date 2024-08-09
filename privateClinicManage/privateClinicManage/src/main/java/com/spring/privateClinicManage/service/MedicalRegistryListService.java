package com.spring.privateClinicManage.service;

import com.spring.privateClinicManage.entity.MedicalRegistryList;
import com.spring.privateClinicManage.entity.Schedule;
import com.spring.privateClinicManage.entity.User;

public interface MedicalRegistryListService {

	void saveMedicalRegistryList(MedicalRegistryList medicalRegistryList);

	MedicalRegistryList findMRLByUserAndSchedule(User user, Schedule schedule);
}
