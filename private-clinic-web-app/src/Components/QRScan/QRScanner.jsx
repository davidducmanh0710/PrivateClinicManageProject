import React, { useEffect, useRef, useState } from "react";
import { Html5QrcodeScanner } from "html5-qrcode";

const QRScanner = () => {
  const [scanCount, setScanCount] = useState(0);
  const [lastResult, setLastResult] = useState("");

  
  function domReady(fn) {
    if (
      document.readyState === "complete" ||
      document.readyState === "interactive"
    ) {
      setTimeout(fn, 1);
    } 
    else {
      document.addEventListener("DOMContentLoaded", fn);
    }
  }

  useEffect(() => {
    const myqrElement = document.getElementById("your-qr-result");

    domReady(() => {
      function onScanSuccess(decodeText, decodeResult) {
        if (decodeText !== lastResult) {
          setScanCount((prevCount) => prevCount + 1);
          setLastResult(decodeText);
          alert(`You QR is : ${decodeText}`, decodeResult);
          myqrElement.innerHTML = `You scanned ${
            scanCount + 1
          }: ${decodeResult}`;
        } else {
          alert("This QR has been decoded");
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

      if (hasPermission === false && lastUsedCameraId === null)
        htmlScanner.render(onScanSuccess);
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
      }
    });
  }, [scanCount, lastResult]);

  return (
    <div>
      <div id="your-qr-result"></div>
      <div style={{ display: "flex", justifyContent: "center" }}>
        <div id="my-qr-reader" style={{ width: "200%" }}></div>
      </div>
    </div>
  );
};

export default QRScanner;
