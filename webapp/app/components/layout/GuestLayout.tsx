import Head from "next/head";
import Header from "@/app/components/layout/Header";
import React from "react";

interface GuestLayoutProps {
  title?: string;
  children: React.ReactNode;
}

const GuestLayout = ({ title, children }: GuestLayoutProps) => {
  return (
    <div className="bg-guest h-100">
      {title ? (
        <Head>
          <title>{title ? title + " | " : ""} Castled</title>
        </Head>
      ) : null}
      <main className="guest-card-holder">
        <div className="card guest-card">
          <div className="card-body">{children}</div>
        </div>
      </main>
    </div>
  );
};
export default GuestLayout;
