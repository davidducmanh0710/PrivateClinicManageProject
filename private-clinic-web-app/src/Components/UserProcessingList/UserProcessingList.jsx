import { useCallback, useContext, useEffect, useState } from "react";
import { UserContext } from "../config/Context";
import { authAPI, endpoints } from "../config/Api";
import { CustomerSnackbar, isBACSI } from "../Common/Common";
import { Alert, Pagination } from "@mui/material";
import dayjs from "dayjs";
import "./UserProcessingList.css";
import PatientTabs from "../PatientTabs/PatientTabs";

export default function UserProcessingList() {
  const [userProcessingList, setUserProcessingList] = useState([]);
  const { currentUser } = useContext(UserContext);

  const [examPatient, setExamPatient] = useState({});

  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);

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

  const getAllProcessingUserToday = useCallback(async () => {
    let response;
    if (isBACSI(currentUser) && currentUser !== null) {
      try {
        let url = `${endpoints["getAllProcessingUserToday"]}?page=${page}`;
        response = await authAPI().get(url, {
          validateStatus: function (status) {
            return status < 500; // Chỉ ném lỗi nếu status code >= 500
          },
        });

        if (response.status === 200) {
          setUserProcessingList(response.data);
          setTotalPage(response.data.totalPages);
        } else showSnackbar(response.data, "error");
      } catch {
        showSnackbar("Lỗi", "error");
      }
    }
  }, [currentUser, page]);

  useEffect(() => {
    if (currentUser !== null) {
      getAllProcessingUserToday();
    }
  }, [currentUser, page]);

  return (
    <>
      <CustomerSnackbar
        open={open}
        message={data.message}
        severity={data.severity}
      />
      <div className="container container-user-processing-list">
        <h2 className="text text-primary">Danh sách bệnh nhân đang đợi</h2>
        <Pagination
          count={totalPage}
          color="primary"
          className="mt-2 mb-4"
          onChange={(event, value) => setPage(value)}
        />
        <ul className="responsive-table">
          <li className="table-header">
            <div className="col col-1">Mã</div>
            <div className="col col-2">Tên người khám</div>
            <div className="col col-4">Ngày sinh</div>
            <div className="col col-5">Số điện thoại</div>
            <div className="col col-6">Địa chỉ</div>
            <div className="col col-6">Triệu chứng</div>
            <div className="col col-7">Hành động</div>
          </li>
          {userProcessingList.length < 1 ? (
            <>
              <Alert variant="filled" severity="info" className="w-50 mx-auto">
                Hiện không có phiếu đăng kí nào
              </Alert>
            </>
          ) : (
            <>
              {userProcessingList.content.map((up) => {
                return (
                  <>
                    <li key={up.id} className="table-row">
                      <div className="col col-1" data-label="ID">
                        {up.id}
                      </div>
                      <div
                        role="button"
                        className="col col-2 text text-info underline"
                        data-label="Name Register"
                        onClick={() => setExamPatient(up)}
                      >
                        {up.name}
                      </div>
                      <div className="col col-4" data-label="Date Register">
                        {dayjs(up.schedule.date).format("DD-MM-YYYY")}
                      </div>
                      <div className="col col-5" data-label="Phone">
                        {up.user.phone}
                      </div>
                      <div className="col col-6" data-label="Address">
                        {up.user.address}
                      </div>
                      <div className="col col-6" data-label="Favor">
                        {up.favor}
                      </div>
                      <div className="col col-7" data-label="Action">
                        <button>Test</button>
                      </div>
                    </li>
                    {examPatient.id === up.id && (
                      <PatientTabs
                        examPatient={examPatient}
                        setExamPatient={setExamPatient}
                      />
                    )}
                  </>
                );
              })}
            </>
          )}
        </ul>
      </div>
    </>
  );
}
