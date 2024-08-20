package com.spring.privateClinicManage.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medical_examination")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MedicalExamination implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "created_date")
	private Date createdDate;

	@Column(name = "predict")
	private String predict;

	@Column(name = "advance")
	private String advance;

	@Column(name = "symptom_process", nullable = false)
	private String symptomProcess;

	@Column(name = "treatment_process", nullable = false)
	private String treatmentProcess;

	@Column(name = "duration_day")
	private Integer durationDay;

	@Column(name = "follow_up_date")
	private Date followUpDate;

	@OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	@JoinColumn(name = "medical_register_list_id", referencedColumnName = "id")
	private MedicalRegistryList mrl;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "medicalExamination")
	private List<PrescriptionItems> prescriptionItems;

}
