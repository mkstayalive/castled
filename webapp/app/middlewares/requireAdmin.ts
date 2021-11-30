import nc from "next-connect";
import { NextApiRequest, NextApiResponse } from "next";
import { Role } from "../common/enums/Role";
import { ServerResponse } from "http";
import { useSession } from "@/app/common/context/sessionContext";

const requireAdmin = async (
  req: NextApiRequest,
  res: NextApiResponse,
  next: any
) => {
  const { user } = await useSession();
  if (!user || user.role !== Role.ADMIN) {
    res.send({
      error: "You must be sign in to view the protected content on this page.",
    });
  } else {
    return next();
  }
};
const handler = nc<NextApiRequest, ServerResponse>().use(requireAdmin);
export default handler;
