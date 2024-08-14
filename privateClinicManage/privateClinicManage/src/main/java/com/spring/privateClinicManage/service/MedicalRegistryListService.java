package com.spring.privateClinicManage.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.spring.privateClinicManage.entity.MedicalRegistryList;
import com.spring.privateClinicManage.entity.Schedule;
import com.spring.privateClinicManage.entity.StatusIsApproved;
import com.spring.privateClinicManage.entity.User;

public interface MedicalRegistryListService {

	void saveMedicalRegistryList(MedicalRegistryList medicalRegistryList);

	MedicalRegistryList findMRLByUserAndSchedule(User user, Schedule schedule);

	Integer countMRLByUserAndScheduleAndisCancelled(User user,
			Schedule schedule, Boolean isCanceled);

	List<MedicalRegistryList> findByUser(User user);

	List<MedicalRegistryList> findByScheduleAndStatusIsApproved(Integer year, Integer month,
			Integer day, StatusIsApproved status);

	MedicalRegistryList findById(Integer id);

	Page<MedicalRegistryList> findByUserPaginated(Integer page, Integer size,
			List<MedicalRegistryList> mrls);

	Page<MedicalRegistryList> findByScheduleAndStatusIsApprovedPaginated(Integer page, Integer size,
			List<MedicalRegistryList> mrls);

	List<MedicalRegistryList> findAllMrl();

	Page<MedicalRegistryList> findMrlsPaginated(Integer page, Integer size,
			List<MedicalRegistryList> mrls);

	List<MedicalRegistryList> sortByStatusIsApproved(List<MedicalRegistryList> mrls,
			StatusIsApproved statusIsApproved);

	List<MedicalRegistryList> findByAnyKey(String key);

	List<MedicalRegistryList> sortBySchedule(List<MedicalRegistryList> mrls, Schedule schedule);

	List<MedicalRegistryList> sortByCreatedDate(List<MedicalRegistryList> mrls, Integer year,
			Integer month, Integer day);

	List<MedicalRegistryList> findByScheduleAndStatusIsApproved2(Schedule schedule,
			StatusIsApproved status);

	List<User> findUniqueUser(Schedule schedule, StatusIsApproved status);

}
