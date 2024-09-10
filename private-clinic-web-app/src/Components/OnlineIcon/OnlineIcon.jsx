import { useEffect, useState } from "react";
import { authAPI, endpoints } from "../config/Api";

export default function OnlineIcon({ u }) {
  const [isOnline, setIsOnline] = useState(false);

  useEffect(() => {
    isUserOnline();
  });

  const isUserOnline = async () => {
    let response;
    try {
      response = await authAPI().post(
        endpoints["isUserOnline"],
        {
          userId: u.id,
        },
        {
          validateStatus: function (status) {
            return status < 500;
          },
        }
      );
      if (response.status === 200) {
        setIsOnline(response.data);
      } else console.log(response, "error");
    } catch {
      console.log(response, "error");
    }
  };

  return <>{isOnline === true && <div class="status-indicator"></div>}</>;
}
