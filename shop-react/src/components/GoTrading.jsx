import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "../api/axiosInstance";

const GoTrading = () => {
  const [keyword, setKeyword] = useState("");
  const navigate = useNavigate();

  const handleSearch = async () => {
    try {
      const response = await axios.get(`/products/search?query=${keyword}`);
      const products = response.data;

      if (products.length === 1) {
        navigate(`/products/${products[0].id}`);
      } else {
        navigate(`/search?query=${keyword}`);
      }
    } catch (error) {
      console.error("검색 실패:", error);
    }
  };

  return (
    <div style={{ padding: "1rem", textAlign: "center", backgroundColor: "#f9f9f9" }}>
      <h2>가장 인기 있는 스니커즈를 쉽고 빠르게 거래해보세요.</h2>
      <input
        type="text"
        placeholder="상품명을 입력하세요"
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        style={{
          padding: "0.8rem",
          fontSize: "1rem",
          width: "60%",
          marginRight: "0.5rem",
        }}
      />
      <button
        onClick={handleSearch}
        style={{
          padding: "0.9rem 1.5rem",
          backgroundColor: "black",
          color: "white",
          border: "none",
          borderRadius: "4px",
          fontSize: "1rem",
          cursor: "pointer",
        }}
      >
        검색
      </button>
    </div>
  );
};

export default GoTrading;