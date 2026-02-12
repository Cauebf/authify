import { Link } from "react-router-dom";
import { assets } from "../assets/assets";

const WhiteLogo = () => {
    return (
        <Link
            to="/"
            className="position-absolute top-0 start-0 py-4 px-5 d-flex align-items-center gap-2 text-decoration-none"
        >
            <img src={assets.white_logo} alt="logo" height={32} width={32} />
            <span className={`fs-4 fw-semibold text-white`}>Authify</span>
        </Link>
    );
};

export default WhiteLogo;
