package com.spring.privateClinicManage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spring.privateClinicManage.entity.PaymentDetailPhase2;

@Repository
public interface PaymentDetailPhase2Repository extends JpaRepository<PaymentDetailPhase2, Integer> {

}