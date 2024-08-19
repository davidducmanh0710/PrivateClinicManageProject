import AppointmentForm from "../AppointmentForm/AppointmentForm";
import CencorRegister from "../CensorRegister/CensorRegister";
import DirectRegister from "../DirectRegister/DirectRegister";
import ExaminationForm from "../ExaminationForm/ExaminationForm";
import Home from "../Home/Home";
import QRScanner from "../QRScan/QRScanner";
import UserProcessingList from "../UserProcessingList/UserProcessingList";
import UserRegisterScheduleList from "../UserRegisterScheduleList/UserRegisterScheduleList";

const publicRoutes = [
  { path: "/", component: Home, role: "ROLE_ALL" },
  {
    path: "/register-schedule",
    component: AppointmentForm,
    role: "ROLE_BENHNHAN",
  },
  {
    path: "/user-register-schedule-list",
    component: UserRegisterScheduleList,
    role: "ROLE_BENHNHAN",
  },
  { path: "/censor-register", component: CencorRegister, role: "ROLE_YTA" },
  { path: "/qr-scan-take-order", component: QRScanner, role: "ROLE_YTA" },
  {
    path: "/directly-register-schedule",
    component: DirectRegister,
    role: "ROLE_YTA",
  },
  {
    path: "/prepare-examination-form",
    component: UserProcessingList,
    role: "ROLE_BACSI",
  },
  { path: "/examination-form", component: ExaminationForm, role: "ROLE_BACSI" },
];

const privateRoutes = [];

export { publicRoutes, privateRoutes };
