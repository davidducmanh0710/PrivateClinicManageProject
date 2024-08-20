import axios from "axios";

const BASE_URL = 'http://localhost:8888';

export let endpoints = {
	login: "/api/users/login/",
    currentUser: "/api/users/current-user/",
    register : "/api/users/register/",
    sendOtp : "/api/users/verify-email/",
	registerSchedule : "/api/users/register-schedule/",
	userRegisterScheduleList : "/api/users/user-register-schedule-list/",
	userCancelRegisterSchedule : (registerScheduleId) => `/api/users/cancel-register-schedule/${registerScheduleId}/`,
	getAllRegisterScheduleList : "/api/users/all-register-schedule",
	getAllStatusIsApproved : "/api/users/getAllStatusIsApproved/",
	getAllUsers : "/api/users/get-all-users/",
	getUsersByRegisterDateAndStatus : "/api/users/get-users-schedule-status/",
	ytaAutoConfirmRegister : "/api/users/auto-confirm-registers/",
	takeOrderFromQrCode : "/api/users/take-order-from-qrCode/",
	printOrderPdf : "/api/pdf/generate/",
	directRegister : "/api/users/direct-register/",
	getAllProcessingUserToday : "/api/users/get-all-processing-user-today/",
	getAllMedicineGroup : "/api/users/get-all-medicine-group/",
	getAllMedicinesByGroup : (medicineGroupId) => `/api/users/get-all-medicine-by-group/${medicineGroupId}/`,
	getMedicineById : (medicineId) => `/api/users/get-medicine-by-id/${medicineId}/`,
	getAllMedicines : '/api/users/get-all-medicines/',
	submitMedicalExamination : '/api/users/submit-medical-examination/'

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
