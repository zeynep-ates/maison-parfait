import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App.jsx";
import { AuthProvider } from "./context/AuthContext.jsx";
import { ComingSoonProvider } from "./context/ComingSoonProvider.jsx";
import { CartProvider } from "./context/CartProvider.jsx";
import "./styles/global.css";

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <ComingSoonProvider>
          <CartProvider>
            <App />
          </CartProvider>
        </ComingSoonProvider>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
);
