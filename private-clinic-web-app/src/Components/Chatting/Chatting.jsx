import { useContext, useRef } from "react";
import "./Chatting.css";
import { useEffect } from "react";
import { UserContext } from "../config/Context";

export default function Chatting() {
  const { currentUser } = useContext(UserContext);

  useEffect(() => {
    const element = document.getElementById("chatting-container");
    element.scrollIntoView();
  }, []);

  return (
    <>
      <div id="chatting-container" className="chatting-container">
        <div className="chatting-list shadow p-3">
          <div className="container mt-4 h-100">
            <div className="d-flex justify-content-between align-items-center">
              <div className="chat-header">Đoạn chat</div>
              <button className="btn btn-primary">
                Kết nối với tư vấn viên
              </button>
            </div>
            <div className="mt-3">
              <div className="search-bar">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  fill="currentColor"
                  className="bi bi-search"
                  viewBox="0 0 16 16"
                >
                  <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 0 0 1.415-1.415l-3.85-3.85a1.007 1.007 0 0 0-.115-.098zm-5.344.856a5.5 5.5 0 1 1 0-11 5.5 5.5 0 0 1 0 11z" />
                </svg>
                <input type="text" placeholder="Tìm kiếm người để nhắn tin" />
              </div>
            </div>
          </div>
        </div>
        <div className="chatting-content shadow container mt-3 p-3">
          <div class="profile p-3 shadow-sm">
            <img src="https://via.placeholder.com/40" alt="Avatar" />
            <div class="profile-info">
              <h6>Nguyễn Quỳnh</h6>
              <small>Hoạt động 14 phút trước</small>
            </div>
          </div>
          <div className="chatting-content-main h-100"></div>
          <div className="box-sending shadow-sm">
            <div className="create-advice-container container mt-2">
              <div className="comment-box">
                <img className="avatar" src={currentUser?.avatar} />

                <div
                  class="textbox"
                  role="textbox"
                  contenteditable="true"
                  aria-placeholder="Nhập văn bản tại đây"
                  data-placeholder="Nhập văn bản tại đây"
                ></div>
                <div className="comment-icons mt-2">
                  <button type="submit" className="btn btn-link p-0 text-light">
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="24"
                      height="24"
                      fill="currentColor"
                      className="bi bi-send text text-primary"
                      viewBox="0 0 16 16"
                    >
                      <path d="M15.854.146a.5.5 0 0 1 .11.53l-5 14a.5.5 0 0 1-.927.06L8.155 10.18l-6.792 2.264a.5.5 0 0 1-.65-.65l2.263-6.793L.262 1.036A.5.5 0 0 1 .32.109l14-5a.5.5 0 0 1 .534.037zm-13.5 7.982 5.149 1.721L14.561 1.44 2.354 8.128zm4.265 5.69 1.721 5.149L14.56 1.439 2.353 8.127z" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
