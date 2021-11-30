import React from "react";
import Head from "next/head";

interface HeadCommonProps {
  title?: string;
}

const HeadCommon = ({ title }: HeadCommonProps) => {
  const pageTitle = `${title ? title + " | " : ""} Castled`;
  return (
    <Head>
      {title ? <title>{pageTitle}</title> : null}
      <link rel="preconnect" href="https://fonts.googleapis.com" />
      <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="" />
      <link
        href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@300;400;600&display=swap"
        rel="stylesheet"
      />
    </Head>
  );
};
export default HeadCommon;
