import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import SignupPage from './pages/SignupPage';
import LoginPage from './pages/LoginPage';
import MainPage from './pages/MainPage';
import MyPage from './pages/MyPage';
import Header from './components/Header';
import ProductUpdate from "./pages/ProductUpdate";
import ProductListPage from "./pages/ProductListPage";
import ProductSearchResultsPage from './pages/ProductSearchResultsPage';
import ProductDetailPage from "./pages/ProductDetailPage";

function App() {
  return (
    <Router>
      <Header />
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/mypage" element={<MyPage />} />
        <Route path="/product/new" element={<ProductUpdate />} />
        <Route path="/mypage/products" element={<ProductListPage />} />
        <Route path="/search" element={<ProductSearchResultsPage />} />
        <Route path="/products/:productId" element={<ProductDetailPage />} />
      </Routes>
    </Router>
  );
}

export default App;
