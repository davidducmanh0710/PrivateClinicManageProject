import { forwardRef, useImperativeHandle, useRef } from "react";
import "./CashPaymentForm.css";

const CashPaymentForm = forwardRef(function CashPaymentForm(
  { onConfirm, onCancel },
  ref
) {
  const dialog = useRef();

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
  return (
    <>
      <dialog ref={dialog}>
        <div className="warning-popup">
          <div className="warning-icon">⚠️</div>
          <h2>Xác nhận đã thu tiền mặt phiếu đăng ký khám bệnh này ?</h2>

          <div className="buttons">
            <button className="delete-button" onClick={() => onConfirm()}>
              Xác nhận
            </button>
            <button className="cancel-button" onClick={onCancel}>
              Hủy
            </button>
          </div>
        </div>
      </dialog>
    </>
  );
});

export default CashPaymentForm;
