package com.spring.privateClinicManage.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medicalRegistryList")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MedicalRegistryList implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "name")
	private String name;

	@Column(name = "favor", nullable = false)
	private String favor;


	@Column(name = "is_canceled", nullable = false)
	private Boolean isCanceled;

	@ManyToOne(fetch = FetchType.LAZY, cascade = {
			CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH
	})
	@JoinColumn(name = "schedule_id", referencedColumnName = "id")
	@JsonIgnore
	private Schedule schedule;

	@ManyToOne(fetch = FetchType.LAZY, cascade = {
			CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH
	})
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	@JsonIgnore
	private User user;

	@Transient
	private Integer order;



}
