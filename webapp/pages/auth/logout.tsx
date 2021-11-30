import React, { useEffect } from "react";
import { useSession } from "@/app/common/context/sessionContext";
import { useRouter } from "next/router";
import authService from "@/app/services/authService";

const Logout = () => {
  const { setUser } = useSession();
  const router = useRouter();
  useEffect(() => {
    authService
      .logout()
      .then(() => {
        setUser(null);
      })
      .catch(() => {})
      .finally(() => {
        router?.push("/");
      });
  });
  return <p>Redirecting...</p>;
};
export default Logout;
