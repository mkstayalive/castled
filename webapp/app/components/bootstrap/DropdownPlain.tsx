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
    </a>
  )
);
export default DropdownCaretDown;
