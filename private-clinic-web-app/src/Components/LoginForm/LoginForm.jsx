import { Button, FloatingLabel, Form } from "react-bootstrap";
import "./LoginForm.css";
import { forwardRef, useImperativeHandle, useRef } from "react";

const LoginForm = forwardRef(function LoginForm({onClose}, ref) {
  const dialog = useRef();
  useImperativeHandle(ref, () => {
    return {
      open() {
        dialog.current.style.border="none"
        dialog.current.style.background="none"
        dialog.current.showModal();
      },

      close() {
        dialog.current.close();
      },
    };
  });

  return (
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
        <form>
          <div className="form-group mb-3">
            <label>Email hoặc SĐT</label>
            <input
              type="text"
              className="form-control"
              placeholder="Email hoặc SĐT"
            />
          </div>
          <div className="form-group mb-3">
            <label>Mật khẩu</label>
            <input
              type="password"
              className="form-control"
              placeholder="Mật khẩu"
            />
          </div>
          <div className="form-group mb-3">
            <a href="#" className="text-decoration-none">
              Quên mật khẩu?
            </a>
          </div>
          <button type="submit" className="btn btn-primary w-100">
            Đăng nhập
          </button>
        </form>
        <div className="text-center mt-3">
          <a href="#" className="text-decoration-none">
            Tạo tài khoản? Đăng ký
          </a>
        </div>
      </div>
    </dialog>
  );
});

export default LoginForm;
