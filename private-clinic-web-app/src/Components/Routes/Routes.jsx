import AppointmentForm from "../AppointmentForm/AppointmentForm";
import Home from "../Home/Home";
import UserRegisterScheduleList from "../UserRegisterScheduleList/UserRegisterScheduleList";

const publicRoutes = [
  { path: "/", component: Home },
  { path: "/register-schedule", component: AppointmentForm },
  { path: "/user-register-schedule-list", component: UserRegisterScheduleList },
];

const privateRoutes = [];

export { publicRoutes, privateRoutes };
