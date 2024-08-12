import { useContext, useState } from "react";
import "./AppointmentForm.css";
import { CustomerSnackbar } from "../Common/Common";
import Api, { authAPI, endpoints } from "../config/Api";
import { useNavigate } from "react-router-dom";

export default function AppointmentForm() {
  const [registerScheduleState, setRegisterScheduleState] = useState({
    name: "",
    date: "",
    favor: "",
  });
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const [data, setData] = useState({
    message: "Đặt lịch thành công",
    severity: "success",
  });

  const showSnackbar = (message, severity) => {
    setData({
      message: message,
      severity: severity,
    });

    setOpen(true);

    setTimeout(() => {
      setOpen(false);
    }, 5000);

    console.log(registerScheduleState);
  };

  function handleDateChange(e) {
    const selectedDate = new Date(e.target.value);
    const today = new Date();
    const maxDate = new Date();
    maxDate.setDate(today.getDate() + 21);

    if (selectedDate < today || selectedDate > maxDate) {
      alert(
        "Đặt lịch khám khám phải nằm trong khoảng từ ngày mai đến 3 tuần sau.\n" + 
        "Nếu bạn muốn khám hôm nay , hãy đến cơ sở gần nhất để đăng kí trực tiếp"
      );
      setRegisterScheduleState((prev) => ({
        ...prev,
        date: "",
      }));
    } else {
      setRegisterScheduleState((prev) => ({
        ...prev,
        date: e.target.value,
      }));
    }
  }

  function hanldeRegisterScheduleState(e) {
    const { name, value } = e.target;
    setRegisterScheduleState((prev) => ({
      ...prev,
      [name]: value,
    }));
  }

  const registerScheduleAct = async (event) => {
    event.preventDefault();

    try {
      const response = await authAPI().post(
        endpoints["registerSchedule"],
        {
          ...registerScheduleState,
        },
        {
          validateStatus: function (status) {
            return status < 500; // Chỉ ném lỗi nếu status code >= 500
          },
        }
      );

      if (response.status === 201) {
        showSnackbar("Đặt lịch thành công !", "success");
        setTimeout(() => {
          navigate("/user-register-schedule-list");
        }, 3000);
      } else {
        showSnackbar(response.data, "error");
      }
    } catch {
      showSnackbar("Lỗi", "error");
    }
  };

  return (
    <>
      <CustomerSnackbar
        open={open}
        message={data.message}
        severity={data.severity}
      />

      <div className="appointment-form-container">
        <div className="appointment-form">
          <h2 className="text text-primary">Đặt Lịch Khám Bệnh</h2>
          <form id="appointmentForm" onSubmit={registerScheduleAct}>
            <div className="form-group">
              <label htmlFor="name">Tên người khám</label>
              <input
                onChange={hanldeRegisterScheduleState}
                type="text"
                id="name"
                name="name"
                value={registerScheduleState.name}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="date">Ngày khám</label>
              <input
                onChange={handleDateChange}
                type="date"
                id="date"
                name="date"
                value={registerScheduleState.date}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="favor">Mô tả triệu chứng</label>
              <textarea
                onChange={hanldeRegisterScheduleState}
                id="favor"
                name="favor"
                rows="4"
                value={registerScheduleState.favor}
                required
              ></textarea>
            </div>
            <button type="submit">Đặt hẹn</button>
          </form>
        </div>
      </div>
    </>
  );
}
