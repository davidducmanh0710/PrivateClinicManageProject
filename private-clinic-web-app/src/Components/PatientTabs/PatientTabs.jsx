import React, { useCallback, useContext, useEffect, useState } from "react";
import "./PatientTabs.css";
import { Tab, Tabs } from "react-bootstrap";
import dayjs from "dayjs";
import { Link } from "react-router-dom";
import PrescriptionItems from "../PrecriptionItems/PrecriptionItems";
import { authAPI, endpoints } from "../config/Api";

export default function PatientTabs({
  examPatient,
  setExamPatient,
  historyExamsPatient,
  setHistoryExamPatient,
  getHistoryUserRegister,
}) {
  // không thể để state của historyExamsPatient ở đây đc , vì nó thay đổi mỗi component con , mà component cha đang chứa giao diện thằng này , dẫn đến ko đổi
  const [selectMedicalExamId, setSelectMedicalExamId] = useState(0);
  const [precriptionItems, setPrecriptionItems] = useState([]);

  const getPrescriptionItemsByMedicalExamId = async (selectMedicalExamId) => {
    try {
      const response = await authAPI().get(
        endpoints["getPrescriptionItemsByMedicalExamId"](selectMedicalExamId),
        {
          validateStatus: function (status) {
            return status < 500; // Chỉ ném lỗi nếu status code >= 500
          },
        }
      );
      if (response.status === 200) {
        setPrecriptionItems(response.data);
        console.log("Thành công", response.data);
      } else console.log("Lỗi", response.data);
    } catch {
      console.log("Lỗi");
    }
  };

  return (
    <>
      <div className="patient-info-tabs">
        <Tabs
          defaultActiveKey="info"
          id="patient-info-tabs"
          className="custom-tabs"
          onSelect={(key) => {
            if (key === "history") {
              if (examPatient.id !== undefined) getHistoryUserRegister();
            } else console.log("else");
          }}
        >
          <Tab eventKey="info" title="Thông tin bệnh nhân">
            <div className="tab-content-area">
              <div className="patient-details">
                <table className="table table-bordered">
                  <h4 className="w-100 text-center text text-primary">
                    Thông tin tài khoản
                  </h4>
                  <tbody>
                    <tr>
                      <th>Mã bệnh nhân</th>
                      <td>{examPatient.user.id}</td>
                      <th>Tên tài khoản</th>
                      <td>{examPatient.user.name}</td>
                    </tr>
                    <tr>
                      <th>Giới tính</th>
                      <td>{examPatient.user.gender}</td>
                      <th>Ngày sinh</th>
                      <td>
                        {dayjs(examPatient.user.birthday).format("DD/MM/YYYY")}
                      </td>
                    </tr>
                    <tr>
                      <th>Điện thoại</th>
                      <td>{examPatient.user.phone}</td>
                      <th>Địa chỉ</th>
                      <td>{examPatient.user.address}</td>
                    </tr>
                    <tr>
                      <th>Email</th>
                      <td>{examPatient.user.email}</td>
                      <th>Số lần đăng kí khám</th>
                      <td>...</td>
                    </tr>
                  </tbody>
                </table>

                <table className="table table-bordered mt-5">
                  <h4 className="w-100 text-center text text-primary">
                    Thông tin người khám
                  </h4>
                  <tbody>
                    <tr>
                      <th>Tên người khám</th>
                      <td>{examPatient.name}</td>
                      <th>Triệu chứng</th>
                      <td>{examPatient.favor}</td>
                    </tr>
                    <tr>
                      <th>Ngày khám</th>
                      <td>
                        {dayjs(examPatient.schedule.date).format("DD/MM/YYYY")}
                      </td>
                      <th>Số thứ tự</th>
                      <td>{examPatient.order}</td>
                    </tr>
                  </tbody>
                </table>
                <div className="d-flex justify-content-evenly align-item-center">
                  <div>
                    <Link
                      className="btn btn-primary mt-3"
                      to="/examination-form"
                      state={{ examPatient }}
                    >
                      Kê toa
                    </Link>
                  </div>
                  <div>
                    <button
                      className="btn btn-danger mt-3"
                      onClick={() => setExamPatient({})}
                    >
                      Đóng
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </Tab>
          <Tab eventKey="history" title="Lịch sử khám bệnh">
            <div className="history-container tab-content-area">
              <div className="table-responsive wrapper shadow-lg">
                <table className="table table-scrollable">
                  <thead className="bg-light text-center">
                    <tr className="align-middle">
                      <th>ID</th>
                      <th>Tên người khám</th>
                      <th>Ngày lập phiếu</th>
                      <th>Người khám</th>
                      <th>Triệu chứng</th>
                      <th>Số ngày cấp thuốc</th>
                    </tr>
                  </thead>
                  <tbody className="table-hover text-center">
                    {historyExamsPatient.length > 0 &&
                      historyExamsPatient.map((h) => {
                        return (
                          <>
                            <tr key={h.id} className="align-middle">
                              <td
                                onClick={() => {
                                  setSelectMedicalExamId(h.id);
                                  getPrescriptionItemsByMedicalExamId(h.id);
                                }}
                                role="button"
                                className="text text-danger underline pointer"
                              >
                                #MSPK{h.id}
                              </td>
                              <td>{h.mrl.name}</td>
                              <td>
                                {dayjs(h.createdDate).format("DD/MM/YYYY")}
                              </td>
                              <td>{h.userCreated.name}</td>
                              <td>{h.symptomProcess}</td>
                              <td>{h.durationDay}</td>
                            </tr>
                            {selectMedicalExamId === h.id && (
                              <>
                                <PrescriptionItems
                                  precriptionItems={precriptionItems}
                                  setSelectMedicalExamId={setSelectMedicalExamId}
                                  predict={h.predict}
                                  examPatient={examPatient}
                                  h={h}
                                />
                              </>
                            )}
                          </>
                        );
                      })}
                  </tbody>
                </table>
              </div>
            </div>
          </Tab>
          <Tab eventKey="invoices" title="Danh sách hóa đơn">
            <div className="tab-content-area">
              {/* Nội dung cho tab Danh sách hóa đơn */}
            </div>
          </Tab>
        </Tabs>
      </div>
    </>
  );
}
