import { useContext, useEffect, useState } from "react";
import "./ManageExerciseTime.css";
import { UserContext } from "../config/Context";
import { authAPI, endpoints } from "../config/Api";
import { CustomerSnackbar } from "../Common/Common";
import { format } from "date-fns";

export default function ManageExerciseTime() {
  const { currentUser } = useContext(UserContext);
  const [isClockIn, setIsClockIn] = useState(false);
  const [attendanceToday, setAttendanceToday] = useState(null);

  const [currentTime, setCurrentTime] = useState(new Date());
  const startTime = new Date();
  const endTime = new Date();

  // Giờ bắt đầu và kết thúc
  startTime.setHours(5, 30, 0);
  endTime.setHours(6, 0, 0);

  // Cập nhật thời gian mỗi giây
  useEffect(() => {
    const interval = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    handleGetAttendanceExerciseToday();
  }, [isClockIn]);

  useEffect(() => {}, [isClockIn, attendanceToday]);

  const isButtonEnabled =
    currentTime >= new Date(startTime.getTime() - 30 * 60 * 1000) &&
    currentTime <= new Date(endTime.getTime() + 60 * 60 * 1000);

  const [open, setOpen] = useState(false);
  const [data, setData] = useState({
    message: "Chấm công thành công",
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
    }, 2400);
  };

  const handleGetAttendanceExerciseToday = async () => {
    let response;

    try {
      response = await authAPI().get(endpoints["getAttendanceExerciseToday"], {
        validateStatus: function (status) {
          return status < 500;
        },
      });
      if (response.status === 200) {
        setIsClockIn(true);
        setAttendanceToday(response.data);
      } else if (response.status === 204) {
        setIsClockIn(false);
      } else {
        showSnackbar(response, "error");
      }
    } catch {
      showSnackbar(response, "error");
    }
  };

  const handleClockAttendanceExercise = async (event) => {
    event.preventDefault();

    let response;
    if (isClockIn === false && attendanceToday === null) {
      try {
        response = await authAPI().get(endpoints["clockInAttendanceExercise"], {
          validateStatus: function (status) {
            return status < 500;
          },
        });
        if (response.status === 200) {
          showSnackbar("Chấm công vào giờ thành công !", "success");
          setIsClockIn(true);
        } else {
          showSnackbar(response, "error");
        }
      } catch {
        showSnackbar(response, "error");
      }
    } else {
      try {
        response = await authAPI().get(
          endpoints["clockOutAttendanceExercise"],
          {
            validateStatus: function (status) {
              return status < 500;
            },
          }
        );
        if (response.status === 200) {
          showSnackbar("Chấm công ra về thành công !", "success");
          setIsClockIn(false);
        } else {
          showSnackbar(response, "error");
        }
      } catch {
        showSnackbar(response, "error");
      }
    }
  };

  return (
    <>
      <CustomerSnackbar
        open={open}
        message={data.message}
        severity={data.severity}
      />
      <div className="manage-exercise-container">
        <div className="container py-3">
          <div className="row justify-content-center">
            <div className="col-md-10">
              <div className="profile-card text-center">
                <img
                  src={currentUser?.avatar}
                  alt="Profile Picture"
                  className="rounded-circle mb-3 img-avatar"
                />
                <h5>Hi, {currentUser?.name}</h5>
                <p className="text-success">HEALTH CARE</p>
                <div className="time-box">
                  <p className="d-flex align-items-start">
                    Giờ bắt đầu :
                    <strong id="start-time" className="ml-3">
                      {format(startTime, "HH:mm:ss")}
                    </strong>
                  </p>
                </div>

                {attendanceToday !== null && attendanceToday?.clockIn && (
                  <div className="time-box2">
                    <p className="d-flex align-items-start">
                      Ghi nhận vào lúc :
                      <strong id="start-time" className="ml-3">
                        {format(
                          new Date(attendanceToday?.clockIn),
                          "dd-MM-yyyy HH:mm:ss"
                        )}
                      </strong>
                    </p>
                  </div>
                )}
                {isClockIn === false && attendanceToday === null && (
                  <div>
                    <h1 className="my-4" id="current-time">
                      <strong>{currentTime.toLocaleTimeString("en-GB")}</strong>
                    </h1>
                    <button
                      onClick={(e) => handleClockAttendanceExercise(e)}
                      id="enter-button"
                      className={`btn btn-custom ${
                        isButtonEnabled ? "" : "bg-secondary"
                      }`}
                      disabled={!isButtonEnabled}
                    >
                      {isClockIn === true ? "Về" : "Vào"}
                    </button>
                  </div>
                )}
                <div className="time-box mt-3">
                  <p className="d-flex align-items-start">
                    Giờ kết thúc : <strong id="end-time">{format(endTime, "HH:mm:ss")}</strong>
                  </p>
                </div>

                {isClockIn === true &&
                  attendanceToday?.clockIn &&
                  attendanceToday?.clockOut === null && (
                    <div>
                      <h1 className="my-4" id="current-time">
                        <strong>
                          {currentTime.toLocaleTimeString("en-GB")}
                        </strong>
                      </h1>
                      <button
                        onClick={(e) => handleClockAttendanceExercise(e)}
                        id="enter-button"
                        className={`btn btn-custom ${
                          isButtonEnabled ? "" : "bg-secondary"
                        }`}
                        disabled={!isButtonEnabled}
                      >
                        {isClockIn === true ? "Về" : "Vào"}
                      </button>
                    </div>
                  )}

                {attendanceToday !== null && attendanceToday?.clockOut && (
                  <div className="time-box2">
                    <p className="d-flex align-items-start">
                      Ghi nhận vào lúc :
                      <strong id="start-time" className="ml-3">
                        {format(
                          new Date(attendanceToday?.clockOut),
                          "dd-MM-yyyy HH:mm:ss"
                        )}
                      </strong>
                    </p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
