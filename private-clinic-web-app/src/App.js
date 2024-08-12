import { Fragment, useContext, useEffect, useState } from "react";
import "./App.css";
import Header from "./Components/Header/Header";
import { SnackbarContextProvider, SnackbarProvider, UserContext, UserContextProvider } from "./Components/config/Context";
import { authAPI, endpoints } from "./Components/config/Api";
import Footer from "./Components/Footer/Footer";
import AppointmentForm from "./Components/AppointmentForm/AppointmentForm";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { isBENHNHAN } from "./Components/Common/Common";
import DefaultLayout from "./Components/DefaultLayout/DefaultLayout";
import { publicRoutes } from "./Components/Routes/Routes";

function App() {
  const [currentUser, setCurrentUser] = useState(null);
  const [token, setToken] = useState("");

  useEffect(() => {
    if (localStorage.getItem("token")) {
      const fetchUser = async () => {
        const response = await authAPI().get(endpoints["currentUser"]);
        setCurrentUser(response.data);
        setToken(localStorage.getItem("token"));
      };
      fetchUser();
    }
    
  }, []);

  const userCtx = {
    currentUser: currentUser,
    setCurrentUser: setCurrentUser,
    token: token,
  };

  return (
    <>
      <BrowserRouter>
      <UserContext.Provider value={userCtx}>
        <div className="App">
          <Routes>
            {publicRoutes.map((route, index) => {
              const Page = route.component;

              let Layout = DefaultLayout;

              if (route.layout) {
                Layout = route.layout;
              } else if (route.layout === null) {
                Layout = Fragment;
              }

              return (
                <Route
                  key={index}
                  path={route.path}
                  element={
                    <Layout>
                      <Page />
                    </Layout>
                  }
                />
              );
            })}
          </Routes>
        </div>
        </UserContext.Provider> 
      </BrowserRouter>
    </>
  );
}

export default App;
