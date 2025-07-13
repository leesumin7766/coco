import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Header.css'; // 스타일 파일 임포트

const Header = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const checkLogin = () => {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        setIsLoggedIn(false);
        return;
      }

      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const now = Math.floor(Date.now() / 1000);

        if (payload.exp > now) {
          setIsLoggedIn(true);
        } else {
          localStorage.removeItem('accessToken');
          setIsLoggedIn(false);
          alert("세션이 만료되었습니다. 다시 로그인해주세요.");
          navigate('/login');
        }
      } catch (error) {
        console.error("토큰 파싱 오류:", error);
        localStorage.removeItem('accessToken');
        setIsLoggedIn(false);
      }
    };

    checkLogin();
    window.addEventListener("storage", checkLogin);

    return () => window.removeEventListener("storage", checkLogin);
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    setIsLoggedIn(false);
    navigate('/');
  };

  return (
    <header className="header">
      <div className="header-left" />
      <div className="header-center" onClick={() => navigate('/')}>
        COCO
      </div>
      <div className="header-right">
        {isLoggedIn ? (
          <>
            <Link to="/mypage">마이페이지</Link>
            <button onClick={handleLogout}>로그아웃</button>
          </>
        ) : (
          <>
            <Link to="/login">로그인</Link>
            <Link to="/signup">회원가입</Link>
          </>
        )}
      </div>
    </header>
  );
};

export default Header;
