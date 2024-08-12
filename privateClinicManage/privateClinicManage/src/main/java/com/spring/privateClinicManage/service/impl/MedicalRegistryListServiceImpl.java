package com.spring.privateClinicManage.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.spring.privateClinicManage.entity.MedicalRegistryList;
import com.spring.privateClinicManage.entity.Schedule;
import com.spring.privateClinicManage.entity.StatusIsApproved;
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
	public Page<MedicalRegistryList> findByUserPaginated(Integer page, Integer size,
			List<MedicalRegistryList> mrls) {
		Pageable pageable = PageRequest.of(page - 1, size);

		mrls.sort(Comparator.comparing(MedicalRegistryList::getCreatedDate).reversed());

		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), mrls.size());
		List<MedicalRegistryList> pagedUsers = mrls.subList(start, end);

		return new PageImpl<>(pagedUsers, pageable, mrls.size());
	}

	@Override
	public Page<MedicalRegistryList> findByScheduleAndStatusIsApprovedPaginated(Integer page,
			Integer size,
			List<MedicalRegistryList> mrls) {

		Pageable pageable = PageRequest.of(page - 1, size);

		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), mrls.size());
		List<MedicalRegistryList> paged = mrls.subList(start, end);

		return new PageImpl<>(paged, pageable, mrls.size());
	}

	@Override
	public List<MedicalRegistryList> findByScheduleAndStatusIsApproved(
			Integer year, Integer month, Integer day, StatusIsApproved status){
		return medicalRegistryListRepository.findByScheduleAndStatusIsApproved(year, month, day,
				status);
	}

	@Override
	public List<MedicalRegistryList> findAllMrl() {
		return medicalRegistryListRepository.findAll();
	}

	@Override
	public Page<MedicalRegistryList> findMrlsPaginated(Integer page,
			Integer size,
			List<MedicalRegistryList> mrls) {

		mrls.sort(Comparator.comparing(MedicalRegistryList::getCreatedDate).reversed());

		Pageable pageable = PageRequest.of(page - 1, size);

		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), mrls.size());
		List<MedicalRegistryList> paged = mrls.subList(start, end);

		return new PageImpl<>(paged, pageable, mrls.size());
	}

	@Override
	public List<MedicalRegistryList> sortByStatusIsApproved(List<MedicalRegistryList> mrls,
			StatusIsApproved statusIsApproved) {
		return mrls.stream()
				.filter(user -> user.getStatusIsApproved().equals(statusIsApproved))
				.collect(Collectors.toList());
	}

	@Override
	public List<MedicalRegistryList> findByAnyKey(String key) {
		return medicalRegistryListRepository.findByAnyKey(key);
	}

}
