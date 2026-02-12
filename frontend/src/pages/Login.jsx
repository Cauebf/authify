import { Link, useNavigate } from "react-router-dom";
import { assets } from "../assets/assets";
import { useContext, useState } from "react";
import axios from "axios";
import { AppContext } from "../context/AppContext";
import { toast } from "react-toastify";
import WhiteLogo from "../components/WhiteLogo";

const Login = () => {
    const [isCreateAccount, setIsCreateAccount] = useState(false);
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const { backendURL, setIsLoggedIn, getUserData } = useContext(AppContext);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        axios.defaults.withCredentials = true; // to send cookies with the request
        setLoading(true);

        try {
            if (isCreateAccount) {
                // register API
                const response = await axios.post(`${backendURL}/register`, {
                    name,
                    email,
                    password,
                });

                if (response.status === 201) {
                    navigate("/");
                    toast.success("Account created successfully");
                } else {
                    toast.error("Email already exists");
                }
            } else {
                // login API
                const response = await axios.post(`${backendURL}/login`, {
                    email,
                    password,
                });

                if (response.status === 200) {
                    setIsLoggedIn(true);
                    getUserData();
                    navigate("/");
                } else {
                    toast.error("Invalid credentials");
                }
            }
        } catch (error) {
            toast.error(error.response.data.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            className="position-relative min-vh-100 d-flex justify-content-center align-items-center"
            style={{
                background: "linear-gradient(90deg, #6a5af9, #8269f9)",
                border: "none",
            }}
        >
            <WhiteLogo />

            <div
                className="card p-4"
                style={{ maxWidth: "400px", width: "100%" }}
            >
                <h2 className="mb-4 text-center">
                    {isCreateAccount ? "Create Account" : "Login"}
                </h2>

                <form onSubmit={handleSubmit}>
                    {isCreateAccount && (
                        <div className="mb-3">
                            <label htmlFor="name" className="form-label">
                                Full Name
                            </label>
                            <input
                                type="text"
                                className="form-control"
                                id="name"
                                placeholder="Enter your full name"
                                onChange={(e) => setName(e.target.value)}
                                value={name}
                                required
                            />
                        </div>
                    )}
                    <div className="mb-3">
                        <label htmlFor="email" className="form-label">
                            Email address
                        </label>
                        <input
                            type="email"
                            className="form-control"
                            id="email"
                            placeholder="Enter your email"
                            onChange={(e) => setEmail(e.target.value)}
                            value={email}
                            required
                        />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="password" className="form-label">
                            Password
                        </label>
                        <input
                            type="password"
                            className="form-control"
                            id="password"
                            placeholder="Enter your password"
                            onChange={(e) => setPassword(e.target.value)}
                            value={password}
                            required
                        />
                    </div>
                    <div className="d-flex justify-content-between mb-3">
                        <Link
                            to="/reset-password"
                            className="text-decoration-none"
                        >
                            Forgot Password?
                        </Link>
                    </div>

                    <button
                        type="submit"
                        className="btn btn-primary w-100"
                        disabled={loading}
                    >
                        {loading
                            ? "Loading..."
                            : isCreateAccount
                              ? "Sign Up"
                              : "Login"}
                    </button>
                </form>

                <div className="mt-3 text-center">
                    <p className="mb-0">
                        {isCreateAccount
                            ? "Already have an account? "
                            : "Don't have an account? "}
                        <button
                            className="btn btn-link p-0 mb-1 text-decoration-none"
                            onClick={() => setIsCreateAccount(!isCreateAccount)}
                        >
                            {isCreateAccount ? "Login" : "Sign Up"}
                        </button>
                    </p>
                </div>
            </div>
        </div>
    );
};

export default Login;
