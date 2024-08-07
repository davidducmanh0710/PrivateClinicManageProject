import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import "./Header.css";
import React, { useRef } from "react";
import LoginForm from "../LoginForm/LoginForm";

export default function Header(props) {
  const formLoginRef = useRef();

  function handleShowLoginForm() {
    formLoginRef.current.open();
  }

  function handleCloseLoginForm() {
    formLoginRef.current.close();
  }

  return (
    <>
      <LoginForm ref={formLoginRef} onClose={handleCloseLoginForm} />

      <div className="container-fluid topbar px-0 px-lg-4 bg-light py-2 d-none d-lg-block">
        <div className="container">
          <div className="row gx-0 align-items-center">
            <div className="col-lg-8 text-center text-lg-start mb-lg-0">
              <div className="d-flex flex-wrap">
                <div className="border-end border-success pe-3">
                  <a href="#" className="text-muted small">
                    <i className="fas fa-map-marker-alt text-success me-2"></i>
                    {props.location}
                  </a>
                </div>
                <div className="ps-3">
                  <a
                    href="mailto:example@gmail.com"
                    className="text-muted small"
                  >
                    <i className="fas fa-envelope text-success me-2"></i>
                    {props.email}
                  </a>
                </div>
              </div>
            </div>
            <div className="col-lg-4 text-center text-lg-end">
              <div className="d-flex justify-content-end">
                <div className="d-flex border-end border-success pe-3">
                  <a className="btn p-0 text-success me-3" href="#">
                    <i className="fab fa-facebook-f"></i>
                  </a>
                  <a className="btn p-0 text-success me-3" href="#">
                    <i className="fab fa-telegram"></i>
                  </a>
                  <a className="btn p-0 text-success me-3" href="#">
                    <i className="fab fa-twitter"></i>
                  </a>
                  <a className="btn p-0 text-success me-3" href="#">
                    <i className="fab fa-instagram"></i>
                  </a>
                </div>
                <div className="dropdown ms-3">
                  <a
                    href="#"
                    className="dropdown-toggle text-dark"
                    data-bs-toggle="dropdown"
                  >
                    <small>
                      <i className="fas fa-user text-success me-2"></i> Tài
                      khoản
                    </small>
                  </a>
                  <div className="dropdown-menu rounded">
                    <a className="border border-white dropdown-item">
                      Đăng kí
                    </a>
                    <a
                      onClick={handleShowLoginForm}
                      className="border border-white dropdown-item"
                    >
                      Đăng nhập
                    </a>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div className="container-fluid nav-bar px-0 px-lg-4 py-lg-0">
        <div className="container">
          <nav className="navbar navbar-expand-lg navbar-light">
            <a href="#" className="navbar-brand p-0">
              <h1 className="text-success mb-0"> HealthCare</h1>
            </a>
            <button
              className="navbar-toggler"
              type="button"
              data-bs-toggle="collapse"
              data-bs-target="#navbarCollapse"
            >
              <span className="fa fa-bars"></span>
            </button>
            <div className="collapse navbar-collapse" id="navbarCollapse">
              <div className="navbar-nav mx-0 mx-lg-auto">
                <a href="index.html" className="nav-item nav-link active">
                  Trang chủ
                </a>
                <a href="about.html" className="nav-item nav-link">
                  Giới thiệu
                </a>
                <a href="service.html" className="nav-item nav-link">
                  Đội ngũ
                </a>
                <a href="blog.html" className="nav-item nav-link">
                  Blog
                </a>
                <div className="nav-item dropdown">
                  <a href="#" className="nav-link" data-bs-toggle="dropdown">
                    <span className="dropdown-toggle">Pages</span>
                  </a>
                  <div className="dropdown-menu">
                    <a href="feature.html" className="dropdown-item">
                      Our Features
                    </a>
                  </div>
                </div>
                <a href="contact.html" className="nav-item nav-link">
                  Contact
                </a>
                <div className="nav-btn px-3">
                  <button
                    className="btn-search btn btn-success btn-md-square rounded-circle flex-shrink-0"
                    data-bs-toggle="modal"
                    data-bs-target="#searchModal"
                  >
                    <i className="fas fa-search"></i>
                  </button>
                </div>
              </div>
            </div>
            <div className="d-none d-xl-flex flex-shrink-0 ps-4 ">
              <a
                href="#"
                className="btn btn-light btn-lg-square position-relative wow tada"
                data-wow-delay=".9s"
              >
                <i className="fa fa-calendar-alt fa-2x"></i>
                <div className="position-absolute"></div>
                <div className="d-flex flex-column ms-3 text-center">
                  <a href="#">
                    <span className="text-blue">Đặt lịch khám</span>
                  </a>
                </div>
              </a>
            </div>
          </nav>
        </div>
      </div>
    </>
  );
}
