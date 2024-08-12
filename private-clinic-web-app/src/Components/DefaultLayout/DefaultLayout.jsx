import Footer from "../Footer/Footer";
import Header from "../Header/Header";
import "./DefaultLayout.css"

function DefaultLayout({ children }) {
    return (
        <div className='wrapper'>
            <Header />
            <div className="container-default-layout">
                <div className="content">{children}</div>
            </div>
            <Footer />
        </div>
    );
}

export default DefaultLayout;