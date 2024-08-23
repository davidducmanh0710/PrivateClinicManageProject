import { Badge, Button, Dropdown } from "react-bootstrap";
import "./NotificationContainer.css";
import { useContext, useEffect, useReducer, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { over } from "stompjs";
import { BASE_URL } from "../config/Api";
import { UserContext } from "../config/Context";
import dayjs from "dayjs";
import { CustomerSnackbar, isYTA } from "../Common/Common";
import { useNavigate } from "react-router-dom";

export default function NotificationContainer() {
  const [showDropdown, setShowDropdown] = useState(false);
  const [YTAnotifications, setNotifications] = useState([]);
  const stompClientRef = useRef(null);
  const { currentUser } = useContext(UserContext);

  const [countIsReadFalse, setCountIsReadFalse] = useState(0);

  const [, forceUpdate] = useReducer((x) => x + 1, 0);

  const navigate = useNavigate();

  const [open, setOpen] = useState(false);
  const [data, setData] = useState({
    message: "Thông báo mới",
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
    }, 4000);
  };

  function formatDuration(seconds) {
    seconds = seconds / 1000;
    if (seconds < 60) {
      seconds = Math.floor(seconds);
      return `${seconds} giây`;
    } else if (seconds < 3600) {
      const minutes = Math.floor(seconds / 60);
      return `${minutes} phút`;
    } else if (seconds < 86400) {
      const hours = Math.floor(seconds / 3600);
      return `${hours} giờ`;
    } else if (seconds < 2592000) {
      const days = Math.floor(seconds / 86400);
      return `${days} ngày`;
    } else if (seconds < 31536000) {
      const months = Math.floor(seconds / 2592000);
      return `${months} tháng`;
    } else {
      const years = Math.floor(seconds / 31536000);
      return `${years} năm`;
    }
  }

  const ytaConnectNotificationWsInit = () => {
    let stompClient = null;
    let socket = new SockJS(`${BASE_URL}/ws`);
    stompClient = over(socket);
    stompClient.debug = () => {}; // tắt log của stomp in ra console
    stompClientRef.current = stompClient;
    stompClient.connect(
      {},
      () => {
        stompClient.subscribe("/notify/registerContainer/", (payload) => {
          const p = JSON.parse(payload.body);
          p.timeSent = Date.now();
          p.isRead = false;
          setNotifications((prevNotifications) => [p, ...prevNotifications]);
          showSnackbar("Bạn có thông báo mới", "success");
          forceUpdate(); // bên client đã re-render , do đã navigate và nạp trang list , nhưng bên này để màn hình đứng yên dẫn đến ko đc re render
        });
      },
      onError
    );
    return () => {
      if (stompClientRef.current) {
        stompClientRef.current.disconnect();
        stompClientRef.current = null;
      }
    };
  };

  useEffect(() => {
    if (currentUser !== null && !stompClientRef.current && isYTA(currentUser))
      ytaConnectNotificationWsInit();
    handleCountIsReadFalse(YTAnotifications);
  }, [YTAnotifications]);

  function onError() {
    console.log("Lỗi");
  }

  function handleCountIsReadFalse(YTAnotifications) {
    let count = 0;
    if (YTAnotifications.length < 1) {
      setCountIsReadFalse(count);
      return;
    }
    YTAnotifications.map((n) => {
      if (n.isRead === false) ++count;
    });
    setCountIsReadFalse(count);
  }

  return (
    <>
      <CustomerSnackbar
        open={open}
        message={data.message}
        severity={data.severity}
      />
      <div className="notification-container">
        <Dropdown
          show={showDropdown}
          onToggle={() => setShowDropdown(!showDropdown)}
        >
          <Dropdown.Toggle
            className="d-flex text-center justify-content-between align-items-center bg-success"
            variant="light"
            id="dropdown-basic"
          >
            <i
              className="fa fa-bell text-white mr-3"
              style={{ marginRight: "10px" }}
            ></i>
            <Badge className="bg-danger" variant="danger">
              {countIsReadFalse}
            </Badge>
          </Dropdown.Toggle>

          <Dropdown.Menu
            className="shadow-lg"
            style={{
              width: "300px",
              maxHeight: "400px",
              minHeight: "100px",
              overflowY: "scroll",
              right: "0",
              left: "auto",
            }}
          >
            {/* <div className="d-flex justify-content-between align-items-center px-3 py-2 shadow-lg">
              <span>Notifications</span>
              <Button variant="primary" size="sm">
                5 new
              </Button>
            </div> */}
            <Dropdown.Divider />

            {YTAnotifications.length > 0 &&
              YTAnotifications.map((notification) => (
                <Dropdown.Item
                  onClick={() => {
                    notification.isRead = true;
                    navigate("/censor-register");
                    handleCountIsReadFalse(YTAnotifications);
                  }}
                  key={notification.id}
                  className={`d-flex align-items-start border ${
                    notification.isRead ? "" : "bg-warning"
                  }`}
                  style={{
                    fontSize: "12px",
                    color: "#000",
                    backgroundColor: "#fff",
                  }}
                >
                  <img
                    src={notification.user.avatar}
                    alt="Avatar"
                    className="rounded-circle"
                    style={{
                      width: "40px",
                      height: "40px",
                      marginRight: "10px",
                    }}
                  />
                  <div>
                    <strong>{notification.user.name}</strong>
                    <p
                      className="mb-0"
                      style={{ fontSize: "12px", color: "#fff" }}
                    >
                      <small style={{ fontSize: "12px", color: "#000" }}>
                        Đặt lịch khám vào ngày{" "}
                        {dayjs(notification.schedule.date).format("DD/MM/YYYY")}
                      </small>
                      <small
                        style={{
                          display: "block",
                          fontSize: "12px",
                          color: "red",
                        }}
                      >
                        {formatDuration(Date.now() - notification.timeSent)}{" "}
                        trước
                      </small>
                    </p>
                  </div>
                </Dropdown.Item>
              ))}

            <Dropdown.Divider />
            {YTAnotifications.length > 0 ? (
              <Dropdown.Item
                className="text-center text-primary"
                style={{
                  fontSize: "12px",
                  color: "#000",
                  backgroundColor: "#fff",
                }}
              >
                Đóng
              </Dropdown.Item>
            ) : (
              <>
                <p className="text-center">
                  <strong>Hiện tại không có thông báo nào</strong>
                </p>
              </>
            )}
          </Dropdown.Menu>
        </Dropdown>
      </div>
    </>
  );
}