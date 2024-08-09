package com.spring.privateClinicManage.service.impl;

import java.util.Date;

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

}
