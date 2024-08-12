package com.spring.privateClinicManage.service;

import java.util.Date;

import com.spring.privateClinicManage.entity.Schedule;

public interface ScheduleService {
	void saveSchedule(Schedule schedule);

	Schedule findByDate(Date date);

	Schedule findById(Integer id);
}
