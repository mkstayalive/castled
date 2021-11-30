import React from "react";

interface LoadingProps {
  className?: string;
}

const Loading = ({ className }: LoadingProps) => {
  return <div className={className}>Loading...</div>;
};

export default Loading;
