import Footer from "../Footer/Footer";
import Header from "../Header/Header";

function DefaultLayout({ children }) {
    return (
        <div className='wrapper'>
            <Header />
            <div className="container">
                <div className="content">{children}</div>
            </div>
            <Footer />
        </div>
    );
}

export default DefaultLayout;