import Head from "next/head";

const Meta = () => (
	<Head>
		<title>Castled</title>
		<meta charSet="utf-8" />
		<meta name="mobile-web-app-capable" content="yes" />
		<meta name="apple-mobile-web-app-capable" content="yes" />
		<meta
			name="apple-mobile-web-app-status-bar-style"
			content="black-translucent"
		/>
		<meta name="apple-mobile-web-app-title" content="Castled" />
		<meta name="application-name" content="Castled" />
		<meta name="description" content="Bring your own ingredients" />
		<meta name="theme-color" content="#1d2020" />
		<meta
			name="viewport"
			content="width=device-width, initial-scale=1, user-scalable=0, viewport-fit=cover"
		/>
		<link
			rel="apple-touch-icon"
			sizes="180x180"
			href="/images/favicon/apple-touch-icon.png"
		/>
		<link
			rel="icon"
			type="image/png"
			sizes="32x32"
			href="/images/favicon/favicon-32x32.png"
		/>
		<link
			rel="icon"
			type="image/png"
			sizes="16x16"
			href="/images/favicon/favicon-16x16.png"
		/>
		<link
			rel="mask-icon"
			href="/images/favicon/safari-pinned-tab.svg"
			color="#5bbad5"
		/>
		<meta name="msapplication-TileColor" content="#da532c" />
		<meta name="theme-color" content="#ffffff" />
		<link rel="manifest" href="/manifest.json" />
	</Head>
);

export default Meta;
