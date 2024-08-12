import { forwardRef, useImperativeHandle, useRef } from "react";
import "./DeleteConfirmationForm.css";

const DeleteConfirmationForm = forwardRef(function DeleteConfirmationForm(
  {onDelete , onCancel},
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
          <h2>Are you sure?</h2>
          <p>
            This action cannot be undone. All values associated with this field
            will be lost.
          </p>
          <div className="buttons">
            <button className="delete-button" onClick={() => onDelete()}>
              Delete field
            </button>
            <button className="cancel-button" onClick={onCancel}>
              Cancel
            </button>
          </div>
        </div>
      </dialog>
    </>
  );
});

export default DeleteConfirmationForm;
