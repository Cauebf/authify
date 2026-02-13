import { useNavigate } from "react-router-dom";
import WhiteLogo from "../components/WhiteLogo";
import { useContext, useRef, useState } from "react";
import { AppContext } from "../context/AppContext";
import axios from "axios";
import { toast } from "react-toastify";

const ResetPassword = () => {
    const inputRef = useRef([]);
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [email, setEmail] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [isEmailSent, setIsEmailSent] = useState(false);
    const [otp, setOtp] = useState("");
    const [isOtpSubmitted, setIsOtpSubmitted] = useState(false);
    const { getUserData, isLoggedIn, userData, backendURL } =
        useContext(AppContext);

    axios.defaults.withCredentials = true;

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

    const onSubmitEmail = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const response = await axios.post(
                `${backendURL}/send-reset-otp?email=${email}`,
            );
            if (response.status === 200) {
                toast.success("OTP has been sent successfully");
                setIsEmailSent(true);
            } else {
                toast.error("Something went wrong, please try again");
            }
        } catch (error) {
            toast.error(error.message);
        } finally {
            setLoading(false);
        }
    };

    const handleVerify = () => {
        const otp = inputRef.current.map((input) => input.value).join(""); // get the values of all input fields
        if (otp.length !== 6) {
            toast.error("Please enter a valid OTP");
            return;
        }

        setOtp(otp);
        setIsOtpSubmitted(true);
    };

    const onSubmitNewPassword = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            const response = await axios.post(`${backendURL}/reset-password`, {
                email,
                otp,
                newPassword,
            });
            if (response.status === 200) {
                toast.success("Password has been reset successfully");
                navigate("/login");
            } else {
                toast.error("Something went wrong, please try again");
            }
        } catch (error) {
            toast.error(error.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            className="d-flex align-items-center justify-content-center vh-100 position-relative"
            style={{
                background: "linear-gradient(90deg, #6a5af9, #8268f9)",
                border: "none",
            }}
        >
            <WhiteLogo />

            {/* Reset Password Card */}
            {!isEmailSent && (
                <div
                    className="rounded-4 p-5 text-center bg-white"
                    style={{ width: "100%", maxWidth: "400px" }}
                >
                    <h4 className="mb-2">Reset Password</h4>
                    <p className="mb-4">
                        Enter your email to reset your password
                    </p>
                    <form onSubmit={onSubmitEmail}>
                        <div className="input-group mb-4 bg-secondary bg-opacity-10 rounded-pill">
                            <span className="input-group-text bg-transparent border-0 ps-4">
                                <i className="bi bi-envelope"></i>
                            </span>
                            <input
                                type="text"
                                className="form-control bg-transparent border-0 ps-1 pe-4 rounded-end"
                                placeholder="Email"
                                style={{ height: "50px" }}
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>
                        <button
                            className="btn btn-primary w-100 py-2"
                            type="submit"
                            disabled={loading}
                        >
                            {loading ? "Sending..." : "Submit"}
                        </button>
                    </form>
                </div>
            )}

            {/* OTP Card */}
            {!isOtpSubmitted && isEmailSent && (
                <div
                    className="p-5 rounded-4 shadow bg-white"
                    style={{ width: "400px" }}
                >
                    <h4 className="text-center fw-bold mb-2">
                        Email Verify OTP
                    </h4>
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
            )}

            {/* New Password Card */}
            {isOtpSubmitted && isEmailSent && (
                <div
                    className="rounded-4 p-4 text-center bg-white"
                    style={{ width: "100%", maxWidth: "400px" }}
                >
                    <h4>New Password</h4>
                    <p className="mb-4">Enter your new password</p>
                    <form onSubmit={onSubmitNewPassword}>
                        <div className="input-group mb-4 bg-secondary bg-opacity-10 rounded-pill">
                            <span className="input-group-text bg-transparent border-0 ps-4">
                                <i className="bi bi-person-fill-lock"></i>
                            </span>
                            <input
                                type="password"
                                className="form-control bg-transparent border-0 ps-1 pe-4 rounded-end"
                                placeholder="Password"
                                style={{ height: "50px" }}
                                value={newPassword}
                                onChange={(e) => setNewPassword(e.target.value)}
                                required
                            />
                        </div>
                        <button
                            className="btn btn-primary w-100 py-2"
                            type="submit"
                            disabled={loading}
                        >
                            {loading ? "Loading..." : "Submit"}
                        </button>
                    </form>
                </div>
            )}
        </div>
    );
};

export default ResetPassword;
