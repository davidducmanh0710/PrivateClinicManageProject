import AppointmentForm from "../AppointmentForm/AppointmentForm";
import CencorRegister from "../CensorRegister/CensorRegister";
import DirectRegister from "../DirectRegister/DirectRegister";
import Home from "../Home/Home";
import QRScanner from "../QRScan/QRScanner";
import UserRegisterScheduleList from "../UserRegisterScheduleList/UserRegisterScheduleList";

const publicRoutes = [
  { path: "/", component: Home },
  { path: "/register-schedule", component: AppointmentForm },
  { path: "/user-register-schedule-list", component: UserRegisterScheduleList },
  { path: "/censor-register", component: CencorRegister },
  { path: "/qr-scan-take-order", component: QRScanner },
  { path : "/directly-register-schedule" , component : DirectRegister},
];

const privateRoutes = [];

export { publicRoutes, privateRoutes };
