package com.spring.privateClinicManage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spring.privateClinicManage.entity.MedicalRegistryList;
import com.spring.privateClinicManage.entity.Schedule;
import com.spring.privateClinicManage.entity.StatusIsApproved;
import com.spring.privateClinicManage.entity.User;

@Repository
public interface MedicalRegistryListRepository extends JpaRepository<MedicalRegistryList, Integer>,
		PagingAndSortingRepository<MedicalRegistryList, Integer> {

	@Query("SELECT mrl FROM MedicalRegistryList mrl WHERE mrl.user = :user and mrl.schedule = :schedule")
	MedicalRegistryList findMRLByUserAndSchedule(@Param("user") User user,
			@Param("schedule") Schedule schedule);

	@Query("SELECT COUNT(mrl) FROM MedicalRegistryList mrl " +
			"WHERE mrl.user = :user and mrl.schedule = :schedule and mrl.isCanceled = :isCanceled")
	Integer countMRLByUserAndScheduleAndisCancelled(@Param("user") User user,
			@Param("schedule") Schedule schedule, @Param("isCanceled") Boolean isCanceled);

	List<MedicalRegistryList> findByUser(User user);

	@Query("SELECT mrl FROM MedicalRegistryList mrl " +
			"LEFT JOIN mrl.schedule s " +
			"WHERE YEAR(s.date) = :year and MONTH(s.date) = :month and DAY(s.date) = :day " +
			"and mrl.statusIsApproved = :statusIsApproved ")
	List<MedicalRegistryList> findByScheduleAndStatusIsApproved(
			@Param("year") Integer year, @Param("month") Integer month,
			@Param("day") Integer day,
			@Param("statusIsApproved") StatusIsApproved status);

	@Query("SELECT mrl FROM MedicalRegistryList mrl " +
			"LEFT JOIN mrl.user u " +
			"WHERE u.name LIKE %:key% "
			+ "OR u.phone LIKE %:key% "
			+ "OR u.address LIKE %:key% "
			+ "OR u.email LIKE %:key% ")
	List<MedicalRegistryList> findByAnyKey(@Param("key") String key);

}
