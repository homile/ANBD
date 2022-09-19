import React, { useEffect, useState } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import styled, { ThemeProvider } from "styled-components";
import GlobalStyle from "./assets/styles/GlobalStyle";
import { darkTheme, lightTheme } from "./assets/styles/Theme";
import Nav from "./components/nav/Nav";
import Main from "./pages/main/Main";
import ShareList from "./pages/share/shareList/ShareList";

const App = () => {
  // 테마 변경 (lightTheme, darkTheme)
  const [isTheme, setIsTheme] = useState("light");
  const theme = isTheme === "light" ? lightTheme : darkTheme;

  useEffect(() => {
    if (localStorage.getItem("theme") === "light") {
      setIsTheme("light");
    } else {
      setIsTheme("dark");
    }
  }, []);
  return (
    <>
      <ThemeProvider theme={theme}>
        <GlobalStyle />
        <AppContainer>
          <Router>
            <Nav isTheme={isTheme} setIsTheme={setIsTheme} />
            <Routes>
              {/* 메인 */}
              <Route path="/" element={<Main></Main>}></Route>
              <Route
                path="/share/list"
                element={<ShareList></ShareList>}
              ></Route>
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
        </AppContainer>
      </ThemeProvider>
    </>
  );
};

export default App;

const AppContainer = styled.div`
  display: flex;
  flex-direction: column;
`;
