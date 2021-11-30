import React from "react";
import cn from "classnames";
import { FieldAttributes } from "formik";
import { IconPlayerPlay, IconLoader } from "@tabler/icons";

interface ButtonSubmitProps extends FieldAttributes<any> {
  children?: JSX.Element | any | null;
  icon?: JSX.Element | string | null;
}

const ButtonSubmit = ({
  submitting,
  children,
  icon,
  ...props
}: ButtonSubmitProps) => {
  console.log(children);
  return (
    <button
      type="submit"
      {...props}
      disabled={submitting}
      className={cn("mt-2 btn btn-primary", props.className)}
    >
      {children === "Run Query" ? (
        <IconPlayerPlay size={14} style={{ marginRight: "5px" }} />
      ) : (
        ""
      )}
      {children || "Submit"}
      {submitting === true ? <IconLoader className="spinner-icon" /> : ""}
    </button>
  );
};
export default ButtonSubmit;
