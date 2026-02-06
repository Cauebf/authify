import { createContext, useState } from "react";
import { AppConstants } from "../util/constants";

export const AppContext = createContext({});

export const AppContextProvider = (props) => {

    const backendURL = AppConstants.BACKEND_URL || 'http://localhost:8080/api/v1';
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [userData, setUserData] = useState(false);

    const contextValue = {
        backendURL,
        isLoggedIn, setIsLoggedIn,
        userData, setUserData
    };

    return (
        <AppContext.Provider value={contextValue}>
            {props.children}
        </AppContext.Provider>
    )
}