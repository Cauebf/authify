import { Link } from "react-router-dom";
import { assets } from "../assets/assets";
import { useState } from "react";

const Login = () => {
    const [isCreateAccount, setIsCreateAccount] = useState(false);

    return (
        <div 
            className="position-relative min-vh-100 d-flex justify-content-center align-items-center"
            style={{background: "linear-gradient(90deg, #6a5af9, #8269f9)", border: "none"}}
        >
            <div className="position-absolute top-0 start-0 px-5 py-4">
                <Link to="/" style={{display: "flex", gap: 7, alignItems: "center", fontWeight: "bold", fontSize: "24px", textDecoration: "none"}}>
                    <img src={assets.white_logo} alt="Logo" width={32} height={32} />
                    <span className="fw-bold fs-4 text-light">Authify</span>
                </Link>
            </div>

            <div className="card p-4" style={{maxWidth: "400px", width: "100%"}}>
                <h2 className="mb-4 text-center">
                    {isCreateAccount ? "Create Account" : "Login"}
                </h2>

                <form>
                    {
                        isCreateAccount && (
                            <div className="mb-3">
                                <label htmlFor="name" className="form-label">Full Name</label>
                                <input type="text" className="form-control" id="name" placeholder="Enter your full name" required />
                            </div>
                        )
                    }
                    <div className="mb-3">
                        <label htmlFor="email" className="form-label">Email address</label>
                        <input type="email" className="form-control" id="email" placeholder="Enter your email" required />
                    </div>
                    <div className="mb-3">
                        <label htmlFor="password" className="form-label">Password</label>
                        <input type="password" className="form-control" id="password" placeholder="Enter your password" required />
                    </div>
                    <div className="d-flex justify-content-between mb-3">
                        <Link to="/reset-password" className="text-decoration-none">Forgot Password?</Link>
                    </div>

                    <button type="submit" className="btn btn-primary w-100">
                        {isCreateAccount ? "Sign Up" : "Login"}
                    </button>
                </form>

                <div className="mt-3 text-center">
                    <p className="mb-0">
                        {isCreateAccount ? "Already have an account? " : "Don't have an account? "}
                        <button className="btn btn-link p-0 mb-1 text-decoration-none" onClick={() => setIsCreateAccount(!isCreateAccount)}>
                            {isCreateAccount ? "Login" : "Sign Up"}
                        </button>
                    </p>
                </div>
            </div>
        </div>
    )
}

export default Login;