import axios from "axios";

export const BASE_URL = 'http://localhost:8888';

export let endpoints = {
	login: "/api/users/login/",
    currentUser: "/api/users/current-user/",
    register : "/api/users/register/",
    sendOtp : "/api/users/verify-email/",
	registerSchedule : "/api/benhnhan/register-schedule/",
	userRegisterScheduleList : "/api/benhnhan/user-register-schedule-list/",
	userCancelRegisterSchedule : (registerScheduleId) => `/api/benhnhan/cancel-register-schedule/${registerScheduleId}/`,
	getAllRegisterScheduleList : "/api/yta/all-register-schedule/",
	getAllStatusIsApproved : "/api/users/getAllStatusIsApproved/",
	getAllUsers : "/api/yta/get-all-users/",
	getUsersByRegisterDateAndStatus : "/api/yta/get-users-schedule-status/",
	ytaAutoConfirmRegister : "/api/yta/auto-confirm-registers/",
	takeOrderFromQrCode : "/api/users/take-order-from-qrCode/",
	printOrderPdf : "/api/pdf/generate/",
	directRegister : "/api/yta/direct-register/",
	getAllProcessingUserToday : "/api/bacsi/get-all-processing-user-today/",
	getAllMedicineGroup : "/api/bacsi/get-all-medicine-group/",
	getAllMedicinesByGroup : (medicineGroupId) => `/api/bacsi/get-all-medicine-by-group/${medicineGroupId}/`,
	getMedicineById : (medicineId) => `/api/bacsi/get-medicine-by-id/${medicineId}/`,
	getAllMedicines : '/api/bacsi/get-all-medicines/',
	submitMedicalExamination : '/api/bacsi/submit-medical-examination/',
	getHistoryUserRegister : '/api/bacsi/get-history-user-register/',
	getPrescriptionItemsByMedicalExamId : (medicalExamId) => `/api/bacsi/get-prescriptionItems-by-medicalExam-id/${medicalExamId}/`
};

export const authAPI = () => {
	return axios.create({
		baseURL: `${BASE_URL}`,
		headers: {
			Authorization: localStorage.getItem("token"),
		},
	});
};

export default axios.create({
	baseURL: `${BASE_URL}`,	
});
