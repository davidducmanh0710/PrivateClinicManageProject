import React from "react";
import "./PatientTabs.css";
import { Tab, Tabs } from "react-bootstrap";
import dayjs from "dayjs";
import { Link, useNavigate } from "react-router-dom";

export default function PatientTabs({ examPatient , setExamPatient }) {
  return (
    <div className="patient-info-tabs">
      <Tabs
        defaultActiveKey="info"
        id="patient-info-tabs"
        className="custom-tabs"
      >
        <Tab eventKey="info" title="Thông tin bệnh nhân">
          <div className="tab-content-area">
            <div className="patient-details">
              <table className="table table-bordered">
                <h4 className="w-100 text-center text text-danger">
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
                    <td>{dayjs(examPatient.user.birthday).format("DD/MM/YYYY")}</td>
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
                <h4 className="w-100 text-center text text-danger">
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
                    <td>{dayjs(examPatient.schedule.date).format("DD/MM/YYYY")}</td>
                    <th>Số thứ tự</th>
                    <td>{examPatient.order}</td>
                  </tr>
                </tbody>
              </table>
              <div className="d-flex justify-content-evenly align-item-center">
                <div>
                  <Link className="btn btn-primary mt-3" to="/examination-form" state={{examPatient}}>
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
          <div className="tab-content-area">
            {/* Nội dung cho tab Lịch sử khám bệnh */}
          </div>
        </Tab>
        <Tab eventKey="invoices" title="Danh sách hóa đơn">
          <div className="tab-content-area">
            {/* Nội dung cho tab Danh sách hóa đơn */}
          </div>
        </Tab>
      </Tabs>
    </div>
  );
}
