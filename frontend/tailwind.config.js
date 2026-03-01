/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {
      colors: {
        blush: "#F6E7EA",
        rose: "#F2D7DD",
        cream: "#FFF9F6",
        ink: "#2B1F22",
        cocoa: "#6B565B",
        gold: "#C9A46A",
        berry: "#7A2E3A",
      },
      boxShadow: {
        ambient: "0 18px 40px rgba(43,31,34,.08)",
      },
      borderRadius: {
        panel: "12px",
      },
      fontFamily: {
        serif: ["ui-serif", "Georgia", "Cambria", "Times New Roman", "Times", "serif"],
        sans: ["ui-sans-serif", "system-ui", "Inter", "Segoe UI", "Roboto", "Arial", "sans-serif"],
      },
    },
  },
  plugins: [],
};