import { IconChevronDown } from "@tabler/icons";
import React from "react";

const DropdownCaretDown = React.forwardRef(
  ({ children, onClick }: any, ref: any) => (
    <a
      href=""
      ref={ref}
      onClick={(e) => {
        e.preventDefault();
        onClick(e);
      }}
      className="d-inline-block"
    >
      {children}
      <IconChevronDown size={14} className="ms-2" />
    </a>
  )
);
export default DropdownCaretDown;
