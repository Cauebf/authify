import { createContext, useEffect, useState } from "react";
import { AppConstants } from "../util/constants";
import axios from "axios";
import { toast } from "react-toastify";

export const AppContext = createContext({});

export const AppContextProvider = (props) => {

    axios.defaults.withCredentials = true;

    const backendURL = AppConstants.BACKEND_URL || 'http://localhost:8080/api/v1';
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [userData, setUserData] = useState(false);

    const getUserData = async () => {
        try {
            const response = await axios.get(`${backendURL}/profile`);
            
            if (response.status === 200) {
                setUserData(response.data);
            } else {
                toast.error("Failed to fetch user data");
            }
        } catch (error) {
            toast.error(error.message);
        }
    }

    const getAuthState = async () => {
        try {
            const response = await axios.get(`${backendURL}/is-authenticated`);
            if (response.status === 200 && response.data === true) {
                setIsLoggedIn(true);
                await getUserData();
            } else {
                setIsLoggedIn(false);
            }
        } catch (error) {
            if (error.response) {
                const msg = error.response.data?.message || "Authentication check failed";
                toast.error(msg);
            } else {
                toast.error(error.message);
            }
            setIsLoggedIn(false);
        }
    }

    useEffect(() => {
        getAuthState();
    }, []);

    const contextValue = {
        backendURL,
        isLoggedIn, setIsLoggedIn,
        userData, setUserData,
        getUserData
    };

    return (
        <AppContext.Provider value={contextValue}>
            {props.children}
        </AppContext.Provider>
    )
}