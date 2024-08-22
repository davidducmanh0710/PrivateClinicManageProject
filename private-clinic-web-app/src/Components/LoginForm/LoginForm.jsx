import Api, { authAPI, BASE_URL, endpoints } from "../config/Api";
import "./LoginForm.css";
import {
  forwardRef,
  useContext,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from "react";
import { CircularProgress } from "@mui/material";
import { CustomerSnackbar } from "../Common/Common";
import { UserContext } from "../config/Context";
import SockJS from "sockjs-client";
import { over } from "stompjs";

const LoginForm = forwardRef(function LoginForm({ onClose }, ref) {
  const [email, setEmail] = useState();
  const [password, setPassword] = useState();
  const [loading, setLoading] = useState();
  const dialog = useRef();
  const { currentUser, setCurrentUser } = useContext(UserContext);

  const [open, setOpen] = useState(false);
  const [data, setData] = useState({
    message: "Đăng nhập thành công",
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
    }, 2500);
  };

  useImperativeHandle(ref, () => {
    return {
      open() {
        dialog.current.style.border = "none";
        dialog.current.style.background = "none";
        dialog.current.showModal();
      },

      close() {
        dialog.current.close();
      },
    };
  });

  const login = async (event) => {
    event.preventDefault();

    setLoading(true);

    try {
      const response = await Api.post(
        endpoints["login"],
        {
          email: email,
          password: password,
        },
        {
          validateStatus: function (status) {
            return status < 500; // Chỉ ném lỗi nếu status code >= 500
          },
        }
      );

      if (response.status === 200) {
        localStorage.setItem("token", response.data);
        setTimeout(async () => {
          const userResponse = await authAPI().get(endpoints["currentUser"], {
            validateStatus: function (status) {
              return status < 500; // Chỉ ném lỗi nếu status code >= 500
            },
          });
          if (userResponse.status === 200) {
            showSnackbar("Đăng nhập thành công", "success");
            setCurrentUser(userResponse.data);
            dialog.current.close();
            // connectWsInit();
          } else {
            showSnackbar(userResponse.data, "error");
          }
        }, 200);
      } else {
        showSnackbar(response.data, "error");
        console.log(response.data);
      }
    } catch (error) {
      showSnackbar("Lỗi", "error");
    }
    setTimeout(() => {
      setLoading(false);
    }, 2400);
  };

  // const connectWsInit = () => {
  //   let stompClient = null;
  //   let socket = new SockJS(`${BASE_URL}/ws`);
  //   stompClient = over(socket);
  //   stompClient.connect(
  //     {},
  //     () => {
  //       stompClient.subscribe("/notify/registerContainer/", (payload) => {
  //         console.log(payload)
  //       });
  //     },
  //     onError
  //   );
  // };

  // function onConnected() {
  //     stompClient.subscribe('/notify/registerContainer/', onNotifyReceived); // subscribe là hàm callback
  // }
  function onError() {
    showSnackbar("Lỗi", "error");
  }

  // function onNotifyReceived(payload) {
  //   console.log(payload)
  // }

  return (
    <>
      <CustomerSnackbar
        open={open}
        message={data.message}
        severity={data.severity}
      />
      <dialog ref={dialog}>
        <div className="form-login card p-4" style={{ width: "400px" }}>
          <button
            className="btn-close position-absolute top-0 end-0 m-3"
            aria-label="Close"
            onClick={() => onClose()}
          ></button>
          <div className="text-center mb-4">
            <h2>Đăng nhập</h2>
          </div>
          <form onSubmit={login}>
            <div className="form-group mb-3">
              <label>Email</label>
              <input
                type="text"
                className="form-control"
                placeholder="Email"
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>
            <div className="form-group mb-3">
              <label>Mật khẩu</label>
              <input
                type="password"
                className="form-control"
                placeholder="Mật khẩu"
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            {loading ? (
              <div className="d-flex justify-content-center align-item-center">
                <CircularProgress className="mt-3" />
              </div>
            ) : (
              <button type="submit" className="btn btn-primary w-100">
                Đăng nhập
              </button>
            )}
          </form>
          <div className="text-center mt-3">
            <a href="#" className="text-decoration-none">
              Tạo tài khoản? Đăng ký
            </a>
          </div>
          <div className="text-center mb-3">
            <a href="#" className="text-decoration-none">
              Quên mật khẩu?
            </a>
          </div>
        </div>
      </dialog>
    </>
  );
});

export default LoginForm;
