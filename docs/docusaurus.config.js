const lightCodeTheme = require("prism-react-renderer/themes/github");
const darkCodeTheme = require("prism-react-renderer/themes/dracula");

/** @type {import('@docusaurus/types').DocusaurusConfig} */
module.exports = {
  title: "Castled",
  tagline: "Dinosaurs are cool",
  url: "https://your-docusaurus-test-site.com",
  baseUrl: "/",
  onBrokenLinks: "throw",
  onBrokenMarkdownLinks: "warn",
  favicon: "img/castled_original.png",
  organizationName: "castled", // Usually your GitHub org/user name.
  projectName: "castled-community", // Usually your repo name.
  themeConfig: {
    navbar: {
      title: "Castled",
      logo: {
        alt: "Castled Logo",
        src: "img/castled_original.png",
      },
      items: [
        // {
        //   type: "doc",
        //   docId: "intro",
        //   position: "left",
        //   label: "Docs",
        // },
      ],
    },
    prism: {
      theme: lightCodeTheme,
      darkTheme: darkCodeTheme,
    },
  },
  presets: [
    [
      "@docusaurus/preset-classic",
      {
        docs: {
          sidebarPath: require.resolve("./sidebars.js"),
          routeBasePath: "/"
          // Please change this to your repo.
          //Commented by Abhilash : To remove the Edit this Page URL
          //editUrl:"https://github.com/facebook/docusaurus/edit/master/website/",
        },

        theme: {
          customCss: require.resolve("./src/css/custom.css"),
        },
      },
    ],
  ],
};
