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
