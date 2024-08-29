import { Badge, Button, Dropdown } from "react-bootstrap";
import "./NotificationContainer.css";
import { useContext, useEffect, useReducer, useRef, useState } from "react";
import SockJS from "sockjs-client";
import { over } from "stompjs";
import { BASE_URL } from "../config/Api";
import { UserContext } from "../config/Context";
import dayjs from "dayjs";
import { CustomerSnackbar, isBENHNHAN, isYTA } from "../Common/Common";
import { useNavigate } from "react-router-dom";

export default function NotificationContainer() {
  const [showDropdownYTA, setShowDropdownYTA] = useState(false);
  const [showDropdownBN, setShowDropdownBN] = useState(false);

  const [YTAnotifications, setYTANotifications] = useState([]);
  const [BENHNHANnotifications, setBENHNHANNotifications] = useState([]);

  const stompYTAClientRef = useRef(null);
  const stompBENHNHANClientRef = useRef(null);

  const { currentUser } = useContext(UserContext);

  const [countIsReadFalse, setCountIsReadFalse] = useState(0);
  const [countIsReadFalseBN, setCountIsReadFalseBN] = useState(0);

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
    let stompYTAClient = null;
    let socket = new SockJS(`${BASE_URL}/ws`);
    stompYTAClient = over(socket);
    stompYTAClient.debug = () => {}; // tắt log của stomp in ra console
    stompYTAClientRef.current = stompYTAClient;
    stompYTAClient.connect(
      {},
      () => {
        stompYTAClient.subscribe("/notify/registerContainer/", (payload) => {
          const p = JSON.parse(payload.body);
          p.timeSent = Date.now();
          p.isRead = false;
          setYTANotifications((prevYTANotifications) => [
            p,
            ...prevYTANotifications,
          ]);
          showSnackbar("Bạn có thông báo mới", "success");
          forceUpdate(); // bên client đã re-render , do đã navigate và nạp trang list , nhưng bên này để màn hình đứng yên dẫn đến ko đc re render
        });
      },
      onError
    );
    return () => {
      if (stompYTAClientRef.current) {
        stompYTAClientRef.current.disconnect();
        stompYTAClientRef.current = null;
      }
    };
  };

  const benhnhanConnectNotificationWsInit = () => {
    let stompBENHNHANClient = null;
    let socket = new SockJS(`${BASE_URL}/ws`);
    stompBENHNHANClient = over(socket);
    // stompYTAClient.debug = () => {}; // tắt log của stomp in ra console
    stompBENHNHANClientRef.current = stompBENHNHANClient;
    stompBENHNHANClient.connect(
      {},
      () => {
        stompBENHNHANClient.subscribe(
          "/notify/directRegister/" + currentUser.id,
          (payload) => {
            const p = JSON.parse(payload.body);
            p.timeSent = Date.now();
            p.isRead = false;
            setBENHNHANNotifications((prevBNNotifications) => [
              p,
              ...prevBNNotifications,
            ]);
            showSnackbar("Bạn có thông báo mới", "success");
            forceUpdate(); // bên client đã re-render , do đã navigate và nạp trang list , nhưng bên này để màn hình đứng yên dẫn đến ko đc re render
          }
        );
      },
      onError
    );
    return () => {
      if (stompBENHNHANClientRef.current) {
        stompBENHNHANClientRef.current.disconnect();
        stompBENHNHANClientRef.current = null;
      }
    };
  };

  useEffect(() => {
    if (
      currentUser !== null &&
      !stompYTAClientRef.current &&
      isYTA(currentUser)
    )
      ytaConnectNotificationWsInit();
    else if (
      currentUser !== null &&
      !stompBENHNHANClientRef.current &&
      isBENHNHAN(currentUser)
    )
      benhnhanConnectNotificationWsInit();
    handleCountIsReadFalse(YTAnotifications);
    handleCountIsReadFalseBN(BENHNHANnotifications);
  }, [YTAnotifications, BENHNHANnotifications]);

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

  function handleCountIsReadFalseBN(BENHNHANnotifications) {
    let count = 0;
    if (BENHNHANnotifications.length < 1) {
      setCountIsReadFalseBN(count);
      return;
    }
    BENHNHANnotifications.map((n) => {
      if (n.isRead === false) ++count;
    });
     setCountIsReadFalseBN(count);
  }


  return (
    <>
      <CustomerSnackbar
        open={open}
        message={data.message}
        severity={data.severity}
      />
      {isYTA(currentUser) && (
        <div className="notification-container">
          <Dropdown
            show={showDropdownYTA}
            onToggle={() => setShowDropdownYTA(!showDropdownYTA)}
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
              <Dropdown.Divider />

              {isYTA(currentUser) &&
                YTAnotifications.length > 0 &&
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
                          {dayjs(notification.schedule.date).format(
                            "DD/MM/YYYY"
                          )}
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
      )}

      {isBENHNHAN(currentUser) && (
        <div className="notification-container">
          <Dropdown
            show={showDropdownBN}
            onToggle={() => setShowDropdownBN(!showDropdownBN)}
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
                {countIsReadFalseBN}
              </Badge>
            </Dropdown.Toggle>

            <Dropdown.Menu
              className="shadow-lg"
              style={{
                width: "350px",
                maxHeight: "400px",
                minHeight: "100px",
                overflowY: "scroll",
                right: "0",
                left: "auto",
              }}
            >
              <Dropdown.Divider />

              {isBENHNHAN(currentUser) &&
                BENHNHANnotifications.length > 0 &&
                BENHNHANnotifications.map((notification) => (
                  <Dropdown.Item
                    onClick={() => {
                      notification.isRead = true;
                      navigate("/user-register-schedule-list");
                      handleCountIsReadFalseBN(BENHNHANnotifications);
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
                      <strong>Đặt lịch khám trực tiếp thành công !</strong>
                      <p
                        className="mb-0"
                        style={{ fontSize: "12px", color: "#fff" }}
                      >
                        <small style={{ fontSize: "12px", color: "#000" }}>
                          Đặt lịch khám vào ngày{" "}
                          {dayjs(notification.schedule.date).format(
                            "DD/MM/YYYY"
                          )}
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
              {BENHNHANnotifications.length > 0 ? (
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
      )}
    </>
  );
}
