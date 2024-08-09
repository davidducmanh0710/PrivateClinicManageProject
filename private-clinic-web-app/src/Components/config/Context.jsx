import { createContext, useContext, useState} from "react";

export const UserContext = createContext({
    currentUser : null,
    setCurrentUser : () => {},
    token : "",
    setToken : () => {}
});


const SnackbarContext = createContext();

export const SnackbarContextProvider = ({ children }) => {
  const [open, setOpen] = useState(false);
  const [data, setData] = useState({
    message: "Đặt lịch thành công",
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

  return (
    <SnackbarContext.Provider value={{ open, data, showSnackbar }}>
      {children}
    </SnackbarContext.Provider>
  );
};

export const useSnackbar = () => useContext(SnackbarContext);