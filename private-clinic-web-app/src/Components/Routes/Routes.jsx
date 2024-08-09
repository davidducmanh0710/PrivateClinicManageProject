import AppointmentForm from "../AppointmentForm/AppointmentForm";
import Home from "../Home/Home";

const publicRoutes = [
  { path: "/", component: Home },
  { path: "/register-schedule", component: AppointmentForm },
];

const privateRoutes = [];

export { publicRoutes, privateRoutes };
