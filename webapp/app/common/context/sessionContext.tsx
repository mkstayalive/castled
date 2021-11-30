import React, { Context, useState } from "react";
import { LoggedInUserDto } from "@/app/common/dtos/LoggedInUserDto";
import authService from "@/app/services/authService";
import { AxiosResponse } from "axios";
import eventService from "@/app/services/eventService";

type SessionProviderType = {
  user: LoggedInUserDto | null | undefined;
  setUser: (session: LoggedInUserDto | null) => void;
};

let SessionContext: Context<SessionProviderType>;
let { Provider } = (SessionContext = React.createContext<SessionProviderType>({
  user: null,
  setUser: () => {},
}));

export const useSession = () => React.useContext(SessionContext);

export default function SessionProvider({ children }: any) {
  const [user, setUser] = useState<LoggedInUserDto | null>();
  if (user === undefined) {
    authService
      .whoAmI()
      .then((res: AxiosResponse<LoggedInUserDto>) => {
        setUser(res.data);
        eventService.send({
          event: "login",
        });
      })
      .catch(() => {
        setUser(null);
      });
  }
  return <Provider value={{ user, setUser }}>{children}</Provider>;
}
