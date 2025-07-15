import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "../api/axiosInstance";

const ProductSearchResultsPage = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const queryParams = new URLSearchParams(location.search);
  const searchQuery = queryParams.get("query");

  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchSearchResults = async () => {
      try {
        const response = await axios.get(`/products/search?query=${searchQuery}`);
        setResults(response.data);
      } catch (err) {
        console.error("검색 결과 로드 실패:", err);
        setError("검색 결과를 불러오는 데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };

    if (searchQuery) {
      fetchSearchResults();
    }
  }, [searchQuery]);

  const handleClick = (productId) => {
    navigate(`/products/${productId}`);
  };

  if (loading) return <div>🔄 검색 중...</div>;
  if (error) return <div>❌ {error}</div>;
  if (results.length === 0) return <div>🙁 검색 결과가 없습니다.</div>;

  return (
    <div style={{ padding: "2rem" }}>
      <h2>🔍 '{searchQuery}' 검색 결과 ({results.length}건)</h2>
      <div style={{ display: "flex", flexDirection: "column", gap: "1rem", marginTop: "1.5rem" }}>
        {results.map((product) => (
          <div
            key={product.id}
            style={{
              display: "flex",
              alignItems: "center",
              padding: "1rem",
              border: "1px solid #ccc",
              borderRadius: "8px",
              cursor: "pointer",
            }}
            onClick={() => handleClick(product.id)}
          >
            <img
              src={product.imageUrl || "/no-image.png"}
              alt={product.name}
              style={{ width: "80px", height: "80px", objectFit: "cover", marginRight: "1rem" }}
            />
            <div>
                <div style={{ fontWeight: "bold", fontSize: "1.1rem" }}>{product.nameKr}</div>
              <div style={{ fontWeight: "bold", fontSize: "1.1rem" }}>{product.name}</div>
              <div style={{ fontSize: "0.9rem", color: "#666" }}>{product.modelNumber}</div>
              <div style={{ marginTop: "0.3rem" }}>출시가: {product.releasePrice.toLocaleString()}원</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default ProductSearchResultsPage;
