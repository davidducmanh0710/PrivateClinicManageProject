import { createContext, useContext, useState} from "react";

export const UserContext = createContext({
    currentUser : null,
    setCurrentUser : () => {},
    token : "",
    setToken : () => {}
});
