import { useEffect, useState } from "react";
import { authAPI, endpoints } from "../config/Api";

export default function LastChatMessage({ r }) {
  const [content, setContent] = useState("");

  useEffect(() => {
    getLastChatMessage();
  });

  const getLastChatMessage = async () => {
    let response;
    try {
      response = await authAPI().post(
        endpoints["getLastChatMessage"],
        {
          recipientId: r.id,
        },
        {
          validateStatus: function (status) {
            return status < 500;
          },
        }
      );
      if (response.status === 200) {
        setContent(response.data.content);
      } else if (response.status === 204) {
        setContent("");
      } else console.log(response, "error");
    } catch {
      console.log(response, "error");
    }
  };

  return (
    <>
      <small>{content}</small>
    </>
  );
}
