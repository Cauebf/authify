import { Link, useNavigate } from "react-router-dom";
import { assets } from "../assets/assets";
import { useContext, useEffect, useRef, useState } from "react";
import { AppContext } from "../context/AppContext";
import { toast } from "react-toastify";
import axios from "axios";
import WhiteLogo from "../components/WhiteLogo";

const EmailVerify = () => {
    const inputRef = useRef([]);
    const [loading, setLoading] = useState(false);
    const { getUserData, isLoggedIn, userData, backendURL } =
        useContext(AppContext);
    const navigate = useNavigate();

    const handleChange = (e, index) => {
        const value = e.target.value.replace(/\D/, ""); // remove non-digit characters
        e.target.value = value;
        // focus next input when a digit is entered
        if (value && index < 5) {
            inputRef.current[index + 1].focus();
        }
    };

    const handleKeyDown = (e, index) => {
        // focus previous input when backspace is pressed
        if (e.key === "Backspace" && !e.target.value && index > 0) {
            inputRef.current[index - 1].focus();
        }
    };

    const handlePaste = (e) => {
        e.preventDefault();
        const paste = e.clipboardData.getData("text").slice(0, 6).split(""); // get first 6 digits from clipboard
        // set the values of the input fields with the pasted digits
        paste.forEach((digit, index) => {
            if (inputRef.current[index]) {
                inputRef.current[index].value = digit;
            }
        });
        // focus next input field based on the length of the pasted digits
        const next = paste.length < 6 ? paste.length : 5;
        inputRef.current[next].focus();
    };

    const handleVerify = async () => {
        const otp = inputRef.current.map((input) => input.value).join(""); // get the values of all input fields
        if (otp.length !== 6) {
            toast.error("Please enter a valid OTP");
            return;
        }

        setLoading(true);
        try {
            const response = await axios.post(`${backendURL}/verify-otp`, {
                otp,
            });
            if (response.status === 200) {
                toast.success("OTP verified successfully");
                getUserData();
                navigate("/");
            } else {
                toast.error("Invalid OTP");
            }
        } catch (error) {
            toast.error("Failed to verify OTP. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (isLoggedIn && userData.isAccountVerified || !userData) {
            navigate("/");
        }
    }, [isLoggedIn, userData]);

    return (
        <div
            className="email-verify-container d-flex align-items-center justify-content-center vh-100 position-relative"
            style={{
                background: "linear-gradient(90deg, #6a5af9, #8268f9)",
                borderRadius: "none",
            }}
        >
            <WhiteLogo />

            <div
                className="p-5 rounded-4 shadow bg-white"
                style={{ width: "400px" }}
            >
                <h4 className="text-center fw-bold mb-2">Email Verify OTP</h4>
                <p className="text-center mb-4">
                    Enter the 6-digit code sent to your email.
                </p>

                <div className="d-flex justify-content-between gap-2 mb-4 text-center text-white-50">
                    {[...Array(6)].map((_, index) => (
                        <input
                            key={index}
                            type="text"
                            maxLength={1}
                            className="form-control text-center fs-4 otp-input"
                            ref={(el) => (inputRef.current[index] = el)}
                            onChange={(e) => handleChange(e, index)}
                            onKeyDown={(e) => handleKeyDown(e, index)}
                            onPaste={handlePaste}
                        />
                    ))}
                </div>

                <button
                    className="btn btn-primary w-100 fw-semibold"
                    disabled={loading}
                    onClick={handleVerify}
                >
                    {loading ? "Verifying..." : "Verify Email"}
                </button>
            </div>
        </div>
    );
};

export default EmailVerify;
