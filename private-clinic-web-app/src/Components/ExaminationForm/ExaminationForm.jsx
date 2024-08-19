import { useCallback, useContext, useEffect, useState } from "react";
import "./ExaminationForm.css";
import { CustomerSnackbar, isBACSI } from "../Common/Common";
import { authAPI, endpoints } from "../config/Api";
import { UserContext } from "../config/Context";
import { useLocation } from "react-router-dom";
import dayjs from "dayjs";

export default function ExaminationForm() {
  const [medicineGroupList, setMedicineGroupList] = useState([]);
  const { currentUser, setCurrentUser } = useContext(UserContext);
  const [selectedMGId, setSelectedMGId] = useState(0);
  const [medicinesList, setMedicinesList] = useState([]);
  const [selectedMedicineId, setSelectedMedicineId] = useState(0);

  const location = useLocation();
  const { examPatient } = location.state || {};

  const [open, setOpen] = useState(false);
  const [data, setData] = useState({
    message: "Đăng kí thành công",
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
  };

  const getAllMedicineGroup = useCallback(async () => {
    let response;
    if (isBACSI(currentUser) && currentUser !== null) {
      try {
        let url = `${endpoints["getAllMedicineGroup"]}`;
        response = await authAPI().get(url, {
          validateStatus: function (status) {
            return status < 500; // Chỉ ném lỗi nếu status code >= 500
          },
        });

        if (response.status === 200) {
          setMedicineGroupList(response.data);
        } else showSnackbar(response.data, "error");
      } catch {
        showSnackbar("Lỗi", "error");
      }
    }
  }, [currentUser]);

  const getAllMedicinesByGroup = async (selectedMGId) => {
    try {
      const response = await authAPI().get(
        endpoints["getAllMedicinesByGroup"](selectedMGId),
        {
          validateStatus: function (status) {
            return status < 500; // Chỉ ném lỗi nếu status code >= 500
          },
        }
      );
      if (response.status === 200) {
        setMedicinesList(response.data);
      } else showSnackbar(response.data, "error");
    } catch {
      showSnackbar("Lỗi", "error");
    }
  };

  useEffect(() => {
    if (currentUser !== null && medicineGroupList.length < 1) {
      getAllMedicineGroup();
    }
  }, [currentUser]);

  function handleSelectedMedicineGroup(mgId) {
    setSelectedMGId(mgId);
    getAllMedicinesByGroup(mgId);
    setSelectedMedicineId(0);
  }

  return (
    <>
      <CustomerSnackbar
        open={open}
        message={data.message}
        severity={data.severity}
      />
      <div className="examination-form-container">
        <h1 className="text text-danger text-center mb-4">LẬP PHIẾU KHÁM</h1>
        <div className="row">
          {/* Cột bên trái */}
          <div className="col-lg-5">
            {/* Hàng đầu tiên của 2 input */}
            <div className="row mb-5">
              <div className="col">
                <div className="input-card">
                  <div className="input-card-header">
                    <h4>Quá trình điều trị</h4>
                  </div>
                  <div className="input-card-content">
                    <textarea
                      className="w-100"
                      rows="5"
                      placeholder="Nhập nội dung..."
                    ></textarea>
                  </div>
                </div>
              </div>
              <div className="col">
                <div className="input-card">
                  <div className="input-card-header">
                    <h4>Diễn biến bệnh</h4>
                  </div>
                  <div className="input-card-content">
                    <textarea
                      className="w-100"
                      rows="5"
                      placeholder="Nhập nội dung..."
                    ></textarea>
                  </div>
                </div>
              </div>
            </div>

            {/* Hàng thứ hai của 2 bảng */}
            <div className="row">
              <div className="col">
                <div className="medicine-group-container selection-card">
                  <div className="selection-card-header">
                    <h4 className="text-center">Nhóm thuốc</h4>
                  </div>
                  <div className="selection-card-content">
                    <ul className="selection-list">
                      {medicineGroupList.length > 1 &&
                        medicineGroupList.map((mg) => {
                          const mgId = mg.id;
                          return (
                            <>
                              <li
                                className={
                                  selectedMGId === mg.id ? "selected" : ""
                                }
                                key={mg.id}
                                onClick={() =>
                                  handleSelectedMedicineGroup(mgId)
                                }
                              >
                                {mg.groupName}
                              </li>
                            </>
                          );
                        })}
                    </ul>
                  </div>
                </div>
              </div>
              <div className="col">
                <div className="medicine-group-container selection-card">
                  <div className="selection-card-header">
                    <h4 className="text-center">Danh sách thuốc</h4>
                  </div>
                  <div className="selection-card-content">
                    <ul className="selection-list">
                      {medicinesList.length > 1 &&
                        medicinesList.map((m) => {
                          return (
                            <>
                              <li
                                key={m.id}
                                className={
                                  selectedMedicineId === m.id ? "selected" : ""
                                }
                                onClick={() => setSelectedMedicineId(m.id)}
                              >
                                {m.name}
                              </li>
                            </>
                          );
                        })}
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Cột bên phải */}
          <div className="col-lg-7">
            <div className="border p-4 shadow bg-white large-box">
              <div className="examination-container">
                <div className="prescription-header">
                  <h2>ĐƠN THUỐC</h2>
                </div>

                <div className="prescription-details">
                  <p>
                    <span>Họ tên:</span> {examPatient.name}
                  </p>
                  <p>
                    <span>Năm sinh:</span>{" "}
                    {2024 - dayjs(examPatient.user.birthday).year()} (
                    {dayjs(examPatient.user.birthday).format("DD-MM-YYYY")}) (
                    {examPatient.user.gender})
                  </p>
                </div>
                <p>
                  <span>Chẩn đoán:</span> <input type="text" />
                </p>

                <div className="btn-group" role="group">
                  <button type="button" className="btn btn-outline-secondary">
                    1 Ngày
                  </button>
                  <button type="button" className="btn btn-outline-secondary">
                    2 Ngày
                  </button>
                  <button type="button" className="btn btn-outline-secondary">
                    3 Ngày
                  </button>
                  <button type="button" className="btn btn-outline-secondary">
                    4 Ngày
                  </button>
                  <button type="button" className="btn btn-outline-secondary">
                    5 Ngày
                  </button>
                  <button type="button" className="btn btn-success">
                    6 Ngày
                  </button>
                  <button type="button" className="btn btn-outline-secondary">
                    ...... Ngày
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
