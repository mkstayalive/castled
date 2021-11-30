import { Role } from "@/app/common/enums/Role";

export interface LoggedInUserDto {
  id: number;
  name: string;
  email: string;
  teamId: number;
  role: Role;
  avatar?: string;
  createdTs: string;
}
