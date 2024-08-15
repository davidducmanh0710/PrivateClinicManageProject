import React, { useCallback, useEffect, useRef, useState } from "react";
import { Html5QrcodeScanner } from "html5-qrcode";
import { CustomerSnackbar } from "../Common/Common";
import Api, { authAPI, endpoints } from "../config/Api";

const QRScanner = () => {
  const [scanCount, setScanCount] = useState(0);
  const [lastResult, setLastResult] = useState("");
  //   const [decodeTextNow , setDecodeTextNow] = useState("")
  let decodeTextNowRef = useRef(null);

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

  function domReady(fn) {
    if (
      document.readyState === "complete" ||
      document.readyState === "interactive"
    ) {
      setTimeout(fn, 1);
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
      decodeTextNowRef.current = null;
    } else {
      showSnackbar(response.data, "error");
      decodeTextNowRef.current = null
    }
  }, []);

  useEffect(() => {
    // const myqrElement = document.getElementById("your-qr-result");

    domReady(() => {
      function onScanSuccess(decodeText, decodeResult) {
        if (decodeText !== lastResult) {
            setScanCount((prevCount) => prevCount + 1);
            setLastResult(decodeText);
          if (decodeTextNowRef.current === null) {
            decodeTextNowRef.current = decodeText;
            loadTakeOrderFromQrCode();
          }
        } else {
          showSnackbar("Mã QR này đã được quét !", "failed");
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

      if (hasPermission === false && lastUsedCameraId === null){
        htmlScanner.render(onScanSuccess);
        decodeTextNowRef.current = null
      }
      else if (hasPermission === true && lastUsedCameraId !== null) {
        HTML5_QRCODE_DATA.hasPermission = false;
        HTML5_QRCODE_DATA.lastUsedCameraId = null;

        localStorage.setItem(
          "HTML5_QRCODE_DATA",
          JSON.stringify({
            HTML5_QRCODE_DATA,
          })
        );
        htmlScanner.render(onScanSuccess);
        decodeTextNowRef.current = null
      }
    });
  }, [decodeTextNowRef]);

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
