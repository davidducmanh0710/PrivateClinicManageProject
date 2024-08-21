package com.spring.privateClinicManage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spring.privateClinicManage.entity.PrescriptionItems;

@Repository
public interface PrescriptionItemsRepository extends JpaRepository<PrescriptionItems, Integer> {

}
