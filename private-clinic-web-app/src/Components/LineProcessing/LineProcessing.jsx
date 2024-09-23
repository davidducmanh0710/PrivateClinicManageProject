import {
  forwardRef,
  useEffect,
  useImperativeHandle,
  useRef,
  useState,
} from "react";
import "./LineProcessing.css";

const LineProcessing = forwardRef(function LineProcessing(
  { onClose, urs },
  ref
) {
  const dialog = useRef();

  useImperativeHandle(ref, () => {
    return {
      open() {
        dialog.current.style.border = "none";
        dialog.current.style.background = "white";
        dialog.current.style.width = "80%";
        dialog.current.style.height = "80%";
        dialog.current.showModal();
      },

      close() {
        dialog.current.close();
      },
    };
  });

  const goodOrder = [
    "CHECKING",
    "PAYMENTPHASE1",
    "SUCCESS",
    "PROCESSING",
    "PAYMENTPHASE2",
    "FINISHED_FOLLOWUP",
  ];

  const badOrder = ["FAILED", "CANCELED"];

  useEffect(() => {
    if (urs !== null) {
      clearHTMLDOM();

      addHTMLDOM();
    }
  }, [urs]);

  function clearHTMLDOM() {
    let clearClassName = document.querySelectorAll(".finished");
    clearClassName.forEach((e) => {
      if (e.getAttribute("name") !== "DEFAULT") {
        e.classList.remove("finished");
      }
    });

    clearClassName = document.querySelectorAll(".doing");
    clearClassName.forEach((e) => {
      if (e.getAttribute("name") !== "DEFAULT") {
        e.classList.remove("doing");
      }
    });

    clearClassName = document.querySelectorAll(".fa-beat");
    clearClassName.forEach((e) => {
      if (e.getAttribute("name") !== "DEFAULT") {
        e.classList.remove("fa-beat");
      }
    });

    clearClassName = document.querySelectorAll(".failed");
    clearClassName.forEach((e) => {
      if (e.getAttribute("name") !== "DEFAULT") {
        e.classList.remove("failed");
      }
    });
  }

  function addHTMLDOM() {
    const status = urs?.statusIsApproved?.status;
    let statusIndex = goodOrder.findIndex((o) => status === o);

    if (statusIndex >= 0)
      for (let i = 0; i <= statusIndex; i++) {
        let elementId = document.getElementById(goodOrder[i]);
        let elementName = document.getElementsByName(goodOrder[i]);

        if (elementName.length > 0 && statusIndex !== 0 && i !== statusIndex)
          elementName.forEach((e) => {
            e.classList.add("finished");
          });

        if (i !== statusIndex) {
          elementId.classList.add("finished");
          console.log(elementId);
        } else if (i === statusIndex) {
          elementId.classList.add("doing", "fa-beat");
        }
      }
    else {
      if (status === "CANCELED" || status === "FAILED") {
        let elementId = document.getElementById("CHECKING");
        let elementName = document.getElementsByName("CHECKING");
        elementId.classList.add("failed");
        elementName.forEach((e) => {
          if (elementName.length > 0 && statusIndex !== 0)
            e.classList.add("failed");
        });
      }
    }
  }

  return (
    <>
      <dialog className="container" ref={dialog}>
        <div onClick={onClose} className="close-button">
          X
        </div>
        <div className="line-processing-container fs-3 text">
          <h1 className="text-center text text-primary">QUÁ TRÌNH HIỆN TẠI</h1>
          <div className="process-step container">
            <div
              id="DEFAULT"
              name="DEFAULT"
              role="button"
              className="step-circle finished"
            >
              <i class="fa-solid fa-registered"></i>
            </div>
            <div name="DEFAULT" className="step-line finished"></div>

            <div
              id="CHECKING"
              name="CHECKING"
              role="button"
              className="step-circle"
            >
              <i class="fa-solid fa-hourglass-start"></i>
            </div>
            <div name="CHECKING" className="step-line"></div>

            <div name="CHECKING" role="button" className="step-circle">
              <i class="fa-solid fa-clipboard-check"></i>
            </div>
            <div name="CHECKING" className="step-line"></div>

            <div
              id="PAYMENTPHASE1"
              name="PAYMENTPHASE1"
              role="button"
              className="step-circle"
            >
              <i class="fa-solid fa-credit-card"></i>
            </div>
            <div name="PAYMENTPHASE1" className="step-line"></div>

            <div
              id="SUCCESS"
              name="SUCCESS"
              role="button"
              className="step-circle"
            >
              <i class="fa-solid fa-qrcode"></i>
            </div>
            <div name="SUCCESS" className="step-line"></div>

            <div
              id="PROCESSING"
              name="PROCESSING"
              role="button"
              className="step-circle"
            >
              <i class="fa-solid fa-user-doctor"></i>
            </div>
            <div name="PROCESSING" className="step-line"></div>

            <div
              id="PAYMENTPHASE2"
              name="PAYMENTPHASE2"
              role="button"
              className="step-circle"
            >
              <i class="fa-solid fa-credit-card"></i>
            </div>
            <div name="PAYMENTPHASE2" className="step-line"></div>

            <div
              id="FINISHED_FOLLOWUP"
              name="FINISHED_FOLLOWUP"
              role="button"
              className="step-circle"
            >
              <i class="fa-solid fa-circle-check"></i>
            </div>
          </div>
        </div>
      </dialog>
    </>
  );
});

export default LineProcessing;
