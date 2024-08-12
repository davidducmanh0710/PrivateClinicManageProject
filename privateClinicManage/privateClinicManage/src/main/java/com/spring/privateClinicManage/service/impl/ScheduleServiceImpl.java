package com.spring.privateClinicManage.service.impl;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.privateClinicManage.entity.Schedule;
import com.spring.privateClinicManage.repository.ScheduleRepository;
import com.spring.privateClinicManage.service.ScheduleService;

import jakarta.transaction.Transactional;

@Service
public class ScheduleServiceImpl implements ScheduleService {

	@Autowired
	private ScheduleRepository scheduleRepository;

	@Override
	@Transactional
	public void saveSchedule(Schedule schedule) {
		scheduleRepository.save(schedule);
	}

	@Override
	public Schedule findByDate(Date date) {
		return scheduleRepository.findByDate(date);
	}

	@Override
	public Schedule findById(Integer id) {
		Optional<Schedule> optional = scheduleRepository.findById(id);
		if (optional.isEmpty())
			return null;
		return optional.get();
	}

	@Override
	public Schedule findByDayMonthYear(Integer year, Integer month, Integer day) {
		return scheduleRepository.findByDayMonthYear(year, month, day);
	}

}
