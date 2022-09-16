import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { ThemeProvider } from "styled-components";
import GlobalStyle from "./assets/styles/GlobalStyle";

const App = () => {
  return (
    <>
      <ThemeProvider>
        <GlobalStyle />
        <Router>
          <Routes>
            {/* 메인 */}
            <Route path="/"></Route>
            <Route path="/share/list"></Route>
            <Route path="/share/detail:id"></Route>
            <Route path="/share/post"></Route>
            <Route path="/share/edit"></Route>
            <Route path="/login"></Route>
            <Route path="/signup"></Route>
            <Route path="/mypage/favorite"></Route>
            <Route path="/mypage/mypost"></Route>
            <Route path="/mypage/edit"></Route>
            <Route path="/shop/list"></Route>
            <Route path="/shop/detail:id"></Route>
            <Route path="/shop/post"></Route>
            <Route path="/shop/edit"></Route>
            {/* id: 로그인 유저 */}
            <Route path="/chat/list:id"></Route>
            {/* id: 상대방 id query &myid = =dsadsa &otherid*/}
            <Route path="/chat/detail:id"></Route>
          </Routes>
        </Router>
      </ThemeProvider>
    </>
  );
};

export default App;
