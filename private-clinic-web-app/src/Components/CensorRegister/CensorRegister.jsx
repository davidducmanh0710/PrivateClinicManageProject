import { useCallback, useContext, useEffect, useRef, useState } from "react";
import { CustomerSnackbar, isYTA } from "../Common/Common";
import "./CensorRegister.css";
import { authAPI, endpoints } from "../config/Api";
import { UserContext } from "../config/Context";
import dayjs from "dayjs";
import { Alert, Pagination } from "@mui/material";
import { useNavigate, useSearchParams } from "react-router-dom";
import AutoConfirmForm from "../AutoConfirmForm/AutoConfirmForm";


export default function CencorRegister() {
  const [statusList, setStatusList] = useState([]);
  const [allRegisterScheduleList, setallRegisterScheduleList] = useState([]);
  const [status, setStatus] = useState("ALL");
  const [search, setSearch] = useState("");
  const [createdDate, setCreatedDate] = useState("");
  const [registerDate, setRegisterDate] = useState("");

  const [isConfirmRegister , setIsConfirmRegister] = useState(false)


  const { currentUser } = useContext(UserContext);
  const navigate = useNavigate();

  const autoConfirmFormRef = useRef();

  const [params] = useSearchParams();

  const [page, setPage] = useState(1);
  const [totalPage, setTotalPage] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const [open, setOpen] = useState(false);
  const [data, setData] = useState({
    message: "Nạp dữ liệu thành công",
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

  function getStatusClass(status) {
    switch (status) {
      case "CHECKING":
        return "warning";
      case "SUCCESS":
        return "success";
      case "FAILED":
        return "danger";
      case "CANCELED":
        return "secondary";
      case "FINISHED":
        return "primary";
      default:
        return "";
    }
  }

  const getAllStatusIsApproved = useCallback(async () => {
    let response;
    try {
      let url = `${endpoints["getAllStatusIsApproved"]}`;
      response = await authAPI().get(url, {
        validateStatus: function (status) {
          return status < 500; // Chỉ ném lỗi nếu status code >= 500
        },
      });

      if (response.status === 200) {
        setStatusList(response.data);
      } else showSnackbar(response.data, "error");
    } catch {
      showSnackbar("Lỗi", "error");
    }
  }, [currentUser]);

  const getAllRegisterScheduleList = useCallback(async () => {
    let response;
    try {
      let url = `${endpoints["getAllRegisterScheduleList"]}?page=${page}`;
      let key = params.get("key");
      if (key) url = `${url}&key=${key}`;
      let statusParam = params.get("status");
      if (statusParam) url = `${url}&key=${key}&status=${statusParam}`;
      let registerDateParam = params.get("registerDate");
      if (registerDateParam)
        url = `${url}&key=${key}&status=${statusParam}&registerDate=${registerDateParam}`;
      let createdDateParam = params.get("createdDate");
      if (createdDateParam)
        url = `${url}&key=${key}&status=${statusParam}&registerDate=${registerDateParam}&createdDate=${createdDateParam}`;

      response = await authAPI().get(url, {
        validateStatus: function (status) {
          return status < 500; // Chỉ ném lỗi nếu status code >= 500
        },
      });

      if (response.status === 200) {
        setallRegisterScheduleList(response.data);
        setTotalPage(response.data.totalPages);
        setTotalElements(response.data.totalElements);
      } else showSnackbar(response.data, "error");
    } catch {
      showSnackbar("Lỗi", "error");
    }
  }, [currentUser, page, params , isConfirmRegister]);

  


  useEffect(() => {
    if (currentUser !== null) {
      getAllRegisterScheduleList();
      if (statusList.length < 1) getAllStatusIsApproved();
      // if (userList.length < 1) getAllUsers();
    }
  }, [currentUser, page, params , isConfirmRegister]);

  const handleStatusChange = (event) => {
    setStatus(event.target.value);
  };

  function handleSortRegisterList(event) {
    event.preventDefault();
    navigate(
      `?key=${search}&status=${status}&registerDate=${registerDate}&createdDate=${createdDate}`
    );
  }

  function handleOpenAutoConfirmForm() {
    setIsConfirmRegister(false)
    autoConfirmFormRef.current.open();
  }

  function handleCloseAutoConfirmForm() {
    autoConfirmFormRef.current.close();
  }

  return (
    <>
      <CustomerSnackbar
        open={open}
        message={data.message}
        severity={data.severity}
      />

      <AutoConfirmForm
        ref={autoConfirmFormRef}
        onClose={handleCloseAutoConfirmForm}
        statusList={statusList}
        // userList={userList}
        setIsConfirmRegister={setIsConfirmRegister}
      />

      <div className="filter-container d-flex justify-content-center align-item-center">
        <form
          onSubmit={handleSortRegisterList}
          id="filterForm"
          className="d-flex w-100 justify-content-center align-item-center"
        >
          <div className="filter-item search-box d-flex flex-direction-column">
            <i className="fa-solid fa-magnifying-glass"></i>
            <input
              type="text"
              id="key"
              name="key"
              placeholder="Tìm kiếm bất kì"
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <div className="filter-item date-box">
            <i className="fa fa-calendar-alt"></i>
            <input
              type="date"
              id="createdDate"
              name="createdDate"
              onChange={(e) => setCreatedDate(e.target.value)}
            />
            <small className="note text-primary">Lọc ngày đặt lịch</small>
          </div>

          <div className="filter-item date-box">
            <i className="fa fa-calendar-alt"></i>
            <input
              value={registerDate}
              type="date"
              id="registerDate"
              name="registerDate"
              onChange={(e) => setRegisterDate(e.target.value)}
            />
            <small className="note text-primary">Lọc ngày hẹn khám</small>
          </div>

          <div
            className={`filter-item select-box bg-${getStatusClass(status)}`}
          >
            <i className="fa-solid fa-list-check"></i>

            <select
              className=""
              id="status"
              name="status"
              onChange={handleStatusChange}
              value={status}
            >
              <option key="0" value="ALL">
                ALL
              </option>
              {statusList.length > 0 &&
                statusList.map((s) => {
                  return (
                    <>
                      <option
                        className={status === s.status ? "selected" : ""}
                        key={s.id}
                        value={s.status}
                      >
                        <strong className="text-white">{s.status}</strong>
                      </option>
                    </>
                  );
                })}
            </select>
            <small className="note text-white">
              Lọc theo trạng thái đăng kí
            </small>
          </div>

          <div className="filter-item button-box">
            <button type="submit">Filter</button>
          </div>
        </form>
      </div>
      <div className="d-inline-flex w-100 align-items-center justify-content-evenly">
        <Pagination
          count={totalPage}
          color="primary"
          className={`mt-4 ${totalElements < 1 ? "d-none" : ""}`}
          onChange={(event, value) => setPage(value)}
        />
        <div>
          <button
            className="btn btn-danger"
            onClick={handleOpenAutoConfirmForm}
          >
            Tự động chỉnh trạng thái và gửi mail
          </button>
        </div>
        <div>
          <button className="btn btn-danger">
            Xuất file PDF danh sách khám
          </button>
        </div>
      </div>
      <div className="table-responsive mt-4 p-4 wrapper rounded-3">
        <table className="table table-scrollable">
          <thead className="bg-light text-center">
            <tr className="align-middle">
              <th>ID</th>
              <th>SĐT</th>
              <th>Email</th>
              <th>Tên người khám</th>
              <th>Ngày đặt</th>
              <th>Ngày hẹn</th>
              <th>Ghi chú</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody className="table-hover text-center" id="userDetails">
            {totalElements < 1 ? (
              <>
                <Alert
                  variant="filled"
                  severity="info"
                  className="w-50 mx-auto"
                >
                  Không có phiếu đăng kí nào
                </Alert>
              </>
            ) : (
              allRegisterScheduleList.content.map((mrl, index) => {
                return (
                  <tr key={index} className="align-middle">
                    <td>{mrl.id}</td>
                    <td>{mrl.user.phone}</td>
                    <td>{mrl.user.email}</td>
                    <td>{mrl.name}</td>
                    <td>
                      {dayjs(mrl.createdDate).format("DD-MM-YYYY HH:mm:ss")}
                    </td>
                    <td>
                      {dayjs(mrl.schedule.date).format("DD-MM-YYYY HH:mm:ss")}
                    </td>
                    <td>{mrl.statusIsApproved.note}</td>
                    <td>
                      <span
                        className={`badge bg-${getStatusClass(
                          mrl.statusIsApproved.status
                        )}`}
                      >
                        {mrl.statusIsApproved.status}
                      </span>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </>
  );
}
