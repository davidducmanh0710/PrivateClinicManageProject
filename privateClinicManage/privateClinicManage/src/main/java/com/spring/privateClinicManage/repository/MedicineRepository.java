package com.spring.privateClinicManage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.spring.privateClinicManage.entity.Medicine;
import com.spring.privateClinicManage.entity.MedicineGroup;

public interface MedicineRepository
		extends JpaRepository<Medicine, Integer>, PagingAndSortingRepository<Medicine, Integer> {

	List<Medicine> findByName(String name);

	List<Medicine> findByMedicineGroup(MedicineGroup medicineGroup);
}
