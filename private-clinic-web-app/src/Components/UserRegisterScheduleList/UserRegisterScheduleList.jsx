import { useCallback, useContext, useEffect, useRef, useState } from "react";
import "./UserRegisterScheduleList.css";
import { authAPI, endpoints } from "../config/Api";
import { CustomerSnackbar, isBENHNHAN } from "../Common/Common";
import { Alert, Pagination } from "@mui/material";
import dayjs from "dayjs";
import { useNavigate, useSearchParams } from "react-router-dom";
import DeleteConfirmationForm from "../DeleteConfirmationForm/DeleteConfirmationForm";
import { UserContext } from "../config/Context";


export default function UserRegisterScheduleList() {
  const [userRegisterScheduleList, setUserRegisterScheduleList] = useState([]);
  const [registerScheduleId, setRegisterScheduleId] = useState(null);

  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);
  const { currentUser } = useContext(UserContext);

  const[isCanceled , setIsCanceled] = useState(false);

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

  const deleteFormRef = useRef();

  const loadUserRegisterScheduleList = useCallback(async () => {
    
    let response;
    if (isBENHNHAN(currentUser) && currentUser != null) {
      try {
        let url = `${endpoints["userRegisterScheduleList"]}?page=${page}`;
        response = await authAPI().get(url, {
          validateStatus: function (status) {
            return status < 500; // Chỉ ném lỗi nếu status code >= 500
          },
        });

        if (response.status === 200) {
          setUserRegisterScheduleList(response.data);
          setTotalPage(response.data.totalPages);
        } else showSnackbar(response.data, "error");
      } catch {
        showSnackbar("Lỗi", "error");
      }
    }
  }, [page, currentUser,isCanceled]);

  useEffect(() => {
    if (currentUser !== null) {
      loadUserRegisterScheduleList();
      setIsCanceled(false)
    }
  }, [page, currentUser,isCanceled]);



  const handleCancelRegisterSchedule = async (registerScheduleId) => {
    try {
      const response = await authAPI().patch(
        endpoints["userCancelRegisterSchedule"](registerScheduleId),
        {
          validateStatus: function (status) {
            return status < 500; // Chỉ ném lỗi nếu status code >= 500
          },
        }
      );
      if (response.status === 200) {
        showSnackbar("Hủy lịch thành công !", "success");
        handleCloseDeleteConfirmForm();
        setIsCanceled(true)
      } else showSnackbar(response.data, "error");
    } catch {
      showSnackbar("Lỗi", "error");
    }
  };

  function handleOpenDeleteConfirmForm(registerScheduleId) {
    deleteFormRef.current.open();
    setRegisterScheduleId(registerScheduleId);
  }

  function handleCloseDeleteConfirmForm() {
    setIsCanceled(false);
    deleteFormRef.current.close();
  }

  return (
    <>
      <DeleteConfirmationForm
        ref={deleteFormRef}
        onDelete={() => handleCancelRegisterSchedule(registerScheduleId)}
        onCancel={handleCloseDeleteConfirmForm}
      />
      <CustomerSnackbar
        open={open}
        message={data.message}
        severity={data.severity}
      />
      {userRegisterScheduleList.empty !== true && <Pagination
        count={totalPage}
        color="primary"
        className="mt-4"
        onChange={(event, value) => setPage(value)}
      />}
      <div className="container container-user-register-schedule-list">
        <h2 className="text text-primary">Danh sách đặt lịch khám</h2>
        <ul className="responsive-table">
          <li className="table-header">
            <div className="col col-1">Ngày đặt</div>
            <div className="col col-2">Tên người khám</div>
            <div className="col col-3">Ngày hẹn khám</div>
            <div className="col col-4">Trạng thái</div>
            <div className="col col-5">Ghi chú</div>
            <div className="col col-6">Hủy lịch khám</div>
          </li>
          {userRegisterScheduleList.empty === true ? (
            <>
              <Alert variant="filled" severity="info" className="w-50 mx-auto">
                Hiện không có phiếu đăng kí nào
              </Alert>
            </>
          ) : (
            <>
              {userRegisterScheduleList.empty === false && userRegisterScheduleList.content.map((urs) => {
                const ursId = urs.id;
                return (
                  <li key={urs.id} className="table-row">
                    <div className="col col-1" data-label="Date Created">
                      {dayjs(urs.createdDate).format("DD-MM-YYYY HH:mm:ss")}
                    </div>
                    <div className="col col-2" data-label="Name Register">
                      {urs.name}
                    </div>
                    <div className="col col-3" data-label="Date Register">
                      {dayjs(urs.schedule.date).format("DD-MM-YYYY HH:mm:ss")}
                    </div>
                    <div className="col col-4" data-label="Status Register">
                      {urs.statusIsApproved.status}
                    </div>
                    <div className="col col-5" data-label="Note">
                      {urs.statusIsApproved.note}
                    </div>
                    <button
                      onClick={() => handleOpenDeleteConfirmForm(ursId)}
                      className={`col col-6 btn  ${
                        urs.statusIsApproved.status !== "CHECKING"
                          ? "btn-secondary disabled"
                          : "btn-danger"
                      }`}
                      data-label="Canceled Register"
                    >
                      Hủy
                    </button>
                  </li>
                );
              })}
            </>
          )}
        </ul>
      </div>
    </>
  );
}
