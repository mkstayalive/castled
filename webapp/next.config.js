const withPWA = require("next-pwa");
module.exports = withPWA({
  pwa: {
    dest: "public"
  },
  env: {
    // Commenting to avoid inlining of this env variable. Will be fetched using getServerSideProps from pages where its required
    // APP_BASE_URL: process.env.APP_BASE_URL,
    API_BASE: process.env.API_BASE,
    DEBUG: process.env.DEBUG
  },
  async rewrites() {
    const backendBaseUrl = process.env.API_BASE_URL;
    const apiBase = process.env.API_BASE;
    return [
      {
        source: "/swagger/:path*",
        destination: `${backendBaseUrl}${apiBase}/swagger/:path*`
      },
      {
        source: "/swagger-static/:path*",
        destination: `${backendBaseUrl}${apiBase}/swagger-static/:path*`
      },
      {
        source: "/swagger.json/:path*",
        destination: `${backendBaseUrl}${apiBase}/swagger.json/:path*`
      },
      {
        source: `${apiBase}/:path*`,
        destination: `${backendBaseUrl}${apiBase}/:path*`
      }
    ];
  }
});
