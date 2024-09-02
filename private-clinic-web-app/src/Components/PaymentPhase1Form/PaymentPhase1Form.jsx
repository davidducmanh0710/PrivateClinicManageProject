import {
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from "react";
import "./PaymentPhase1Form.css";
import dayjs from "dayjs";
import { authAPI, endpoints } from "../config/Api";
import { CustomerSnackbar } from "../Common/Common";
import { CircularProgress } from "@mui/material";
import VoucherForm from "../VoucherForm/VoucherForm";

const PaymentPhase1Form = forwardRef(function PaymentPhase1Form(
  { onCancel, paymentPhase1UrlForm },
  ref
) {
  const dialog2 = useRef();

  const voucherFormRef = useRef();

  const [loading, setLoading] = useState();

  const [voucher, setVoucher] = useState(null);
  const [code, setCode] = useState("");

  const [finalPrice, setFinalPrice] = useState(100000);

  const [open, setOpen] = useState(false);
  const [data, setData] = useState({
    message: "Thanh toán thành công",
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
    }, 3000);
  };

  useImperativeHandle(ref, () => {
    return {
      open() {
        dialog2.current.style.border = "none";
        // dialog2.current.style.overflow = "hidden";
        dialog2.current.style.width = "50%";
        dialog2.current.style.maxWidth = "50%";
        dialog2.current.style.maxHeight = "100%";
        dialog2.current.showModal();
      },

      close() {
        dialog2.current.close();
      },
    };
  });

  const handleMOMOPaymentPhase1 = async (amount, mrlId) => {
    setLoading(true);
    let response;
    try {
      response = await authAPI().post(
        endpoints["benhnhanMOMOPaymentPhase1"],
        {
          amount: amount,
          mrlId: mrlId,
          voucherId : voucher.id
        },
        {
          validateStatus: function (status) {
            return status < 500; // Chỉ ném lỗi nếu status code >= 500
          },
        }
      );

      if (response.status === 200) {
        showSnackbar("Đang chuyển hướng thanh toán MOMO ... ", "warning");
        setTimeout(() => {
          window.location.href = response.data;
        }, 3000);
        setTimeout(() => {
          setLoading(false);
        }, 5000);
      } else {
        showSnackbar(response.data, "error");
      }
    } catch {
      showSnackbar("Lỗi", "error");
      console.log(response);
    }
  };

  const handleVNPAYPaymentPhase1 = async (amount, mrlId) => {
    setLoading(true);
    let response;
    try {
      response = await authAPI().post(
        endpoints["benhnhanVNPAYPaymentPhase1"],
        {
          amount: amount,
          mrlId: mrlId,
        },
        {
          validateStatus: function (status) {
            return status < 500; // Chỉ ném lỗi nếu status code >= 500
          },
        }
      );

      if (response.status === 200) {
        showSnackbar(
          "Đang chuyển hướng thanh toán sang trang VNPAY ... ",
          "warning"
        );
        setTimeout(() => {
          window.location.href = response.data;
        }, 3000);
        setTimeout(() => {
          setLoading(false);
        }, 5000);
      } else {
        showSnackbar(response.data, "error");
      }
    } catch {
      showSnackbar("Lỗi", "error");
      console.log(response);
    }
  };

  function handleVoucherForm() {
    voucherFormRef.current.open();
  }

  function handleCloseVoucherForm() {
    voucherFormRef.current.close();
  }

  const handleApplyVoucher = async () => {
    let response;
    try {
      response = await authAPI().post(
        endpoints["applyVoucherPayment"],
        {
          code: code,
        },
        {
          validateStatus: function (status) {
            return status < 500; // Chỉ ném lỗi nếu status code >= 500
          },
        }
      );
      if (response.status === 200) {
        voucherFormRef.current.close();
        setVoucher(response.data); // mới set sẽ vô hàng đợi , nên nếu cập nhật giá ở đây là không thể
        showSnackbar("Sử dụng mã giảm giá thành công !", "success");
      } else {
        showSnackbar(response.data, "error");
      }
    } catch {
      showSnackbar("Lỗi", "error");
      console.log(response);
      console.log(code);
    }
  };

  useEffect(() => {
    if (voucher !== null) {
      setFinalPrice(
        100000 - (100000 * voucher.voucherCondition.percentSale) / 100
      ); // set giá ở đây nè
    } else {
      setFinalPrice(100000);
    }
  }, [voucher, finalPrice]);

  return (
    <>
      <VoucherForm
        ref={voucherFormRef}
        onCancel={handleCloseVoucherForm}
        handleApplyVoucher={handleApplyVoucher}
        code={code}
        setCode={setCode}
        voucher={voucher}
        setVoucher={setVoucher}
        open={open}
        data={data}
      />
      <dialog ref={dialog2}>
        <CustomerSnackbar
          open={open}
          message={data.message}
          severity={data.severity}
        />
        <div className="payment-phase1-container">
          <button
            className="btn-close position-absolute top-0 end-0 m-3"
            aria-label="Close"
            onClick={() => onCancel()}
          ></button>
          <div className="row">
            <div className="col-12" style={{ fontSize: "medium" }}>
              <div className="row d-flex">
                <div className="col-xs-6 col-sm-6 col-md-6">
                  <address>
                    <strong>Bệnh viện tư nhân HealthCare</strong>
                    <br />
                    Phước Kiển , Nhà Bè
                    <br />
                    Thành phố Hồ Chí Minh
                    <br />
                    <abbr title="Phone">Số điện thoại:</abbr> (+84)293819230
                  </address>
                </div>
                <div className="col-xs-6 col-sm-6 col-md-6 text-end">
                  <p>
                    <em>Mã hóa đơn: </em>
                    <strong></strong>
                  </p>
                  <p>
                    <em>Ngày lập hóa đơn: </em>
                    <strong>{dayjs(new Date()).format("DD/MM/YYYY")}</strong>
                  </p>
                </div>
              </div>
              <div className="row">
                <div className="text-center">
                  <h1 style={{ color: "green", marginTop: "10px" }}>
                    HÓA ĐƠN ĐĂNG KÍ KHÁM BỆNH
                  </h1>
                </div>

                <div className="container mt-2">
                  <div className="card">
                    <div className="card-body">
                      <h4 className="card-title">Thông tin bệnh nhân : </h4>

                      {paymentPhase1UrlForm !== null && (
                        <>
                          <div className="row d-flex">
                            <div className="col-xs-6 col-sm-6 col-md-6">
                              <p>
                                <span>Mã phiếu đăng ký : </span>
                                <strong className="d-inline">
                                  #MSPDKLK{paymentPhase1UrlForm.id}
                                </strong>
                              </p>
                              <p>
                                <span>Tên người đăng ký : </span>
                                <strong>{paymentPhase1UrlForm.name}</strong>
                              </p>
                              <p>
                                <span>Số điện thoại : </span>
                                <strong>
                                  {paymentPhase1UrlForm.user.phone}
                                </strong>
                              </p>
                            </div>
                            <div className="col-xs-6 col-sm-6 col-md-6 text-end">
                              <p>
                                <span>Ngày đăng ký khám : </span>
                                <strong>
                                  {dayjs(
                                    paymentPhase1UrlForm.createdDate
                                  ).format("DD/MM/YYYY")}
                                </strong>
                              </p>
                              <p>
                                <span>Ngày hẹn khám : </span>
                                <strong>
                                  {dayjs(
                                    paymentPhase1UrlForm.schedule.date
                                  ).format("DD/MM/YYYY")}
                                </strong>
                              </p>
                            </div>
                          </div>
                          <div className="row">
                            <div className="col-xs-12">
                              <p>
                                <span>
                                  Nhu cầu khám :{" "}
                                  <strong className="d-inline">
                                    {paymentPhase1UrlForm.favor}
                                  </strong>
                                </span>
                              </p>
                            </div>
                          </div>
                        </>
                      )}
                    </div>
                  </div>
                </div>
                <div className="container">
                  {/* <table className="table table-hover">
                    <thead>
                      <tr>
                        <th>Danh sách thuốc :</th>
                        <th className="text-center">Giá tiền</th>
                        <th className="text-center">Số lượng cấp</th>
                        <th className="text-center">Đơn vị thuốc</th>
                        <th className="text-center">Tổng tiền</th>
                      </tr>
                    </thead>
                    <tbody></tbody>
                  </table> */}
                  <table className="table table-hover">
                    <thead>
                      <tr>
                        <th>Tiền khám </th>
                        <th className="text-center"></th>
                        <th className="text-center"></th>
                        <th className="text-center"></th>
                        <th className="text-center">Tổng tiền</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td className="col-md-9">
                          <h5>
                            <em>Tiền khám cơ bản </em>
                          </h5>
                        </td>
                        <td></td>
                        <td></td>
                        <td> </td>
                        <td className="text-center">100.000</td>
                      </tr>
                      <tr>
                        <td className="col-md-9">
                          {voucher !== null && (
                            <h5>
                              <em>Áp dụng mã giảm giá </em>
                            </h5>
                          )}
                        </td>
                        <td></td>
                        <td></td>
                        <td> </td>
                        {voucher !== null && (
                          <td className="text-center">
                            -
                            {(
                              (100000 * voucher.voucherCondition.percentSale) /
                              100
                            ).toLocaleString("vi-VN")}
                            ({voucher.voucherCondition.percentSale}%)
                          </td>
                        )}
                      </tr>
                    </tbody>
                  </table>
                  <div className="w-100 d-flex justify-content-between border shadow-sm align-item-center p-3">
                    <div className="bg-success p-1 rounded">
                      <i className="fa-solid fa-ticket fs-5 mr-5"></i>{" "}
                      <span className="text text-white bg-success fs-5 p-1">
                        HEALTHCARE VOUCHER
                      </span>
                    </div>
                    <div className="text text-primary">
                      <button
                        className="btn btn-primary"
                        onClick={handleVoucherForm}
                      >
                        Chọn hoặc nhập mã
                      </button>{" "}
                    </div>
                  </div>
                  <table className="table table-hover">
                    <thead>
                      <tr>
                        <th>Số tiền cần thanh toán :</th>
                        <th className="text-center"></th>
                        <th className="text-center"></th>
                        <th className="text-center"></th>
                        <th className="text-center">Tổng tiền</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td className="col-md-9">
                          <h4>
                            <em>Tổng cộng </em>
                          </h4>
                        </td>
                        <td>  </td>
                        <td>  </td>
                        <td>  </td>

                        <td className="text-center text-danger">
                          <h4>
                            <strong>
                              {finalPrice.toLocaleString("vi-VN")}
                            </strong>
                          </h4>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>

                {paymentPhase1UrlForm !== null &&
                  (loading ? (
                    <div className="d-flex justify-content-center align-item-center">
                      <CircularProgress className="mt-3" />
                    </div>
                  ) : (
                    <div className="container">
                      <div className="d-flex justify-content-center">
                        <div className="p-3 w-50">
                          <button
                            className="button-vnpay-payment"
                            onClick={() =>
                              handleVNPAYPaymentPhase1(
                                finalPrice,
                                paymentPhase1UrlForm.id
                              )
                            }
                          >
                            <div className="icon-vnpay-div">
                              <img
                                className="icon-vnpay-image"
                                src="https://res.cloudinary.com/diwxda8bi/image/upload/v1725081067/vnpay_g4m1a7.png"
                              />
                            </div>
                            <div className="text-payment">VNPAY Payment</div>
                          </button>
                        </div>

                        <div className="p-3 w-50">
                          <button
                            className="button-momo-payment"
                            onClick={() =>
                              handleMOMOPaymentPhase1(
                                finalPrice,
                                paymentPhase1UrlForm.id
                              )
                            }
                          >
                            <div className="icon-momo-div">
                              <img
                                className="icon-momo-image"
                                src="https://res.cloudinary.com/diwxda8bi/image/upload/v1725079829/momo_payment_i0xokf.png"
                              />
                            </div>
                            <div className="text-payment">MOMO Payment</div>
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
              </div>
            </div>
          </div>
        </div>
      </dialog>
    </>
  );
});

export default PaymentPhase1Form;
