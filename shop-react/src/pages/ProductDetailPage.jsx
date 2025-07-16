import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "../api/axiosInstance";

const ProductDetailPage = () => {
  const { productId } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedSize, setSelectedSize] = useState(""); 
  const [showModal, setShowModal] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProductDetail = async () => {
      try {
        const response = await axios.get(`/products/${productId}`);
        setProduct(response.data);
      } catch (err) {
        setError("상품 정보를 불러오지 못했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchProductDetail();
  }, [productId]);

  const handleWishlist = async () => {
    if (!selectedSize) return alert("사이즈를 선택해주세요.");

    try {
      await axios.post("/wishlist", {
        productId,
        size: selectedSize,
      });
      alert("위시리스트에 담겼습니다.");
      setShowModal(false); // 팝업 닫기
    } catch (err) {
      alert("위시리스트 추가 실패");
    }
  };

  if (loading) return <div>🔄 로딩 중...</div>;
  if (error) return <div>❌ {error}</div>;
  if (!product) return <div>❗ 상품이 존재하지 않습니다.</div>;

   return (
    <div style={{ padding: "2rem" }}>
      <h2>{product.nameKr} ({product.name})</h2>
      <img
        src={product.imageUrls?.[0] || "/no-image.png"}
        alt={product.nameKr}
        style={{ width: "300px", height: "300px", objectFit: "cover", marginBottom: "1rem" }}
      />
      <p><strong>모델 번호:</strong> {product.modelNumber}</p>
      <p><strong>출시가:</strong> {product.releasePrice.toLocaleString()}원</p>

      {/* 모든 사이즈 버튼 */}
      <button style={{ marginTop: "1rem" }} onClick={() => setShowModal(true)}>
        위시 리스트 담기
      </button>

      {/* 모달 */}
      {showModal && (
        <div style={{
          position: "fixed",
          top: 0, left: 0, right: 0, bottom: 0,
          backgroundColor: "rgba(0,0,0,0.5)",
          display: "flex", justifyContent: "center", alignItems: "center",
          zIndex: 9999
        }}>
          <div style={{
            backgroundColor: "#fff",
            padding: "2rem",
            borderRadius: "8px",
            minWidth: "300px"
          }}>
            <h3>사이즈 선택</h3>
            <div style={{ display: "flex", flexWrap: "wrap", gap: "0.5rem" }}>
              {product.sizes.map((size, idx) => (
                <button
                  key={idx}
                  style={{
                    padding: "0.5rem 1rem",
                    border: selectedSize === size ? "2px solid black" : "1px solid gray",
                    backgroundColor: selectedSize === size ? "#f0f0f0" : "#fff",
                    cursor: "pointer"
                  }}
                  onClick={() => setSelectedSize(size)}
                >
                  {size}
                </button>
              ))}
            </div>

            <div style={{ marginTop: "1rem", display: "flex", justifyContent: "space-between" }}>
              <button onClick={handleWishlist}>위시리스트 담기</button>
              <button onClick={() => setShowModal(false)}>닫기</button>
            </div>
          </div>
        </div>
      )}

      <div style={{ marginTop: "2rem" }}>
        <button style={{ padding: "1rem", marginRight: "1rem" }}>즉시 구매</button>
        <button style={{ padding: "1rem" }}>즉시 판매</button>
      </div>
    </div>
  );
};

export default ProductDetailPage;
