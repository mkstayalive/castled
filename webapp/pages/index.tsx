import React, { useEffect } from "react";
import { useSession } from "@/app/common/context/sessionContext";
import { useRouter } from "next/router";
import Loading from "@/app/components/common/Loading";

const Index = () => {
  const { user } = useSession();
  const router = useRouter();
  useEffect(() => {
    if (user !== undefined && user === null) {
      router?.push("/auth/login");
    } else if (user !== undefined) {
      router?.push("/pipelines");
    }
  });
  return <Loading />;
};
export default Index;
