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
  const [notifications, setNotifications] = useState([]);
  const stompClientRef = useRef(null);
  const { currentUser } = useContext(UserContext);

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

  function getRemainingTime(timeSent) {
    const currentTime = Date.now();
    const remainingTime = currentTime - timeSent;
    
    if (remainingTime > 0) {
      const remainingMinutes = Math.floor(remainingTime / (1000 * 60));
      const remainingSeconds = Math.floor((remainingTime % (1000 * 60)) / 1000);
      console.log(`Thời gian còn lại: ${remainingMinutes} phút ${remainingSeconds} giây`);
    } else {
      console.log('Đã hết thời gian!');
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
          setNotifications((prevNotifications) => [
            JSON.parse(payload.body),
            ...prevNotifications,
          ]);
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
  }, []);

  function onError() {
    console.log("Lỗi");
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
              {notifications.length}
            </Badge>
          </Dropdown.Toggle>

          <Dropdown.Menu
            className="shadow-lg"
            style={{ width: "300px", right: "0", left: "auto" }}
          >
            {/* <div className="d-flex justify-content-between align-items-center px-3 py-2 shadow-lg">
              <span>Notifications</span>
              <Button variant="primary" size="sm">
                5 new
              </Button>
            </div> */}
            <Dropdown.Divider />

            {notifications.length > 0 &&
              notifications.map((notification) => (
                <Dropdown.Item
                  onClick={() => {
                    navigate("/censor-register");
                  }}
                  key={notification.id}
                  className="d-flex align-items-start"
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
                    </p>
                  </div>
                </Dropdown.Item>
              ))}

            <Dropdown.Divider />
            <Dropdown.Item
              href="#"
              className="text-center text-primary"
              style={{
                fontSize: "12px",
                color: "#000",
                backgroundColor: "#fff",
              }}
            >
              See All Notifications
            </Dropdown.Item>
          </Dropdown.Menu>
        </Dropdown>
      </div>
    </>
  );
}
