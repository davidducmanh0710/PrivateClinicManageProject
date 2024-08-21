package com.spring.privateClinicManage.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "`user`")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "avatar", nullable = false)
	private String avatar;

	@Column(name = "password", nullable = false)
	@JsonIgnore
	private String password;

	@Column(name = "email", unique = true, nullable = false)
	private String email;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "gender", nullable = false)
	private String gender;

	@Column(name = "birthday", nullable = false)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthday;

	@Column(name = "phone", nullable = false)
	private String phone;

	@Column(name = "address", nullable = false)
	private String address;

	@Column(name = "is_actived")
	@JsonIgnore
	private Boolean active;

	@Transient
	@JsonIgnore
	private MultipartFile file;

	@ManyToOne(fetch = FetchType.EAGER, cascade = {
			CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH
	})
	@JoinColumn(name = "role_id", referencedColumnName = "id")
	private Role role;

	@OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JsonIgnore
	private List<MedicalRegistryList> medicalRegistryLists;

	@OneToMany(mappedBy = "userCreated", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JsonIgnore
	private List<MedicalExamination> medicalExaminations;


}
