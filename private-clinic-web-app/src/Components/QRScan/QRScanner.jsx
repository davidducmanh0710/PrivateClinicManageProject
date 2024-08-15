import React, { useCallback, useEffect, useRef, useState } from "react";
import { Html5QrcodeScanner } from "html5-qrcode";
import { CustomerSnackbar } from "../Common/Common";
import Api, { authAPI, endpoints } from "../config/Api";

const QRScanner = () => {
  //   const [decodeTextNow , setDecodeTextNow] = useState("")
  let decodeTextNowRef = useRef(null);
  let lastResultRef = useRef("diff");
  let isFinishedRef = useRef(true);
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
    }, 7000);
  };

  function domReady(fn) {
    if (
      document.readyState === "complete" ||
      document.readyState === "interactive"
    ) {
      // nếu đã load đc camera
      setTimeout(fn, 1000);
    } else {
      document.addEventListener("DOMContentLoaded", fn);
    }
  }

  const loadTakeOrderFromQrCode = useCallback(async () => {
    const response = await Api.post(
      endpoints["takeOrderFromQrCode"],
      {
        mrlId: decodeTextNowRef.current,
      },
      {
        validateStatus: function (status) {
          return status < 500; // Chỉ ném lỗi nếu status code >= 500
        },
      }
    );
    if (response.status === 200) {
      showSnackbar("Quét mã QR lấy số thứ tự thành công!", "success");

      setTimeout(() => {
        decodeTextNowRef.current = null;
        isFinishedRef.current = true;
      }, 7000);
    } else {
      showSnackbar(response.data, "error");
      setTimeout(() => {
        decodeTextNowRef.current = null;
        isFinishedRef.current = true;
      }, 7000);
    }
  }, []); // đã fecth lấy dữ liệu đc , ko cần nạp lại decodeTextNowRef

  useEffect(() => {
    domReady(() => {
      function onScanSuccess(decodeText, decodeResult) {
        if (isFinishedRef.current == true) {
          isFinishedRef.current = false;
          if (decodeText !== lastResultRef.current) {
            if (decodeTextNowRef.current === null) {
              lastResultRef.current = decodeText;
              decodeTextNowRef.current = decodeText;
              loadTakeOrderFromQrCode();
            }
          } else {
            showSnackbar("Mã QR này đã được quét !", "error");
          }
        }
      }

      const htmlScanner = new Html5QrcodeScanner("my-qr-reader", {
        fps: 60,
        qrbox: 500,
      });

      const HTML5_QRCODE_DATA = JSON.parse(
        localStorage.getItem("HTML5_QRCODE_DATA")
      );
      const hasPermission = HTML5_QRCODE_DATA.hasPermission;
      const lastUsedCameraId = HTML5_QRCODE_DATA.lastUsedCameraId;

      if (hasPermission === false && lastUsedCameraId === null) {
        htmlScanner.render(onScanSuccess);
        decodeTextNowRef.current = null;
      } else if (hasPermission === true && lastUsedCameraId !== null) {
        HTML5_QRCODE_DATA.hasPermission = false;
        HTML5_QRCODE_DATA.lastUsedCameraId = null;

        localStorage.setItem(
          "HTML5_QRCODE_DATA",
          JSON.stringify({
            HTML5_QRCODE_DATA,
          })
        );
        htmlScanner.render(onScanSuccess);
        decodeTextNowRef.current = null;
        isFinishedRef.current = true;
      }
    });
  }, [decodeTextNowRef]); // phải nạp lại nếu có lần scan mới

  return (
    <>
      <CustomerSnackbar
        open={open}
        message={data.message}
        severity={data.severity}
      />
      <div>
        <div id="your-qr-result"></div>
        <div style={{ display: "flex", justifyContent: "center" }}>
          <div id="my-qr-reader" style={{ width: "200%" }}></div>
        </div>
      </div>
    </>
  );
};

export default QRScanner;
