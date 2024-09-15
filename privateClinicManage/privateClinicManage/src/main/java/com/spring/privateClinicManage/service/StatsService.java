package com.spring.privateClinicManage.service;

import java.util.List;

public interface StatsService {

	List<Object[]> statsByPrognosisMedicine(Integer year, Integer month);

	List<Object[]> statsByRevenue(Integer year);

}
