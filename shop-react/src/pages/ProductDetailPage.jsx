import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import axios from "../api/axiosInstance";

const ProductDetailPage = () => {
  const { productId } = useParams();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedSize, setSelectedSize] = useState("");
  const [sellPrice, setSellPrice] = useState("");
  const [showModal, setShowModal] = useState(false); // 위시리스트용
  const [showSellModal, setShowSellModal] = useState(false); // 판매등록용
  const [buyPrice, setBuyPrice] = useState("");
  const [showBuyModal, setShowBuyModal] = useState(false); // 구매전용
  const [lowestAsk, setLowestAsk] = useState(null);
  const [highestBid, setHighestBid] = useState(null);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

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
      setShowModal(false);
    } catch (err) {
      alert("위시리스트 추가 실패");
    }
  };

  useEffect(() => {
    if (selectedSize) {
      axios.get(`/biddings/summary`, {
        params: { productId, size: selectedSize }
      })
      .then((res) => {
        setLowestAsk(res.data.lowestAsk || null);
        setHighestBid(res.data.highestBid || null);
      })
      .catch(() => {
        setLowestAsk(null);
        setHighestBid(null);
      });
    }
  }, [selectedSize, productId]);

  const isBuyNowAvailable = () =>
    !!lowestAsk &&
    !!buyPrice &&
    parseInt(buyPrice, 10) >= parseInt(lowestAsk, 10);
  const isSellNowAvailable = () => highestBid && sellPrice && parseInt(sellPrice, 10) <= highestBid;

  const handleBuy = async () => {
    if (!selectedSize || !buyPrice) return alert("사이즈와 가격을 모두 입력해주세요.");
    try {
      const res = await axios.post("/biddings", {
        productId: Number(productId),
        size: selectedSize,
        price: parseInt(buyPrice, 10),
        position: "BUY"
      });

      if (res.data.matched) {
        const orderId = res.data.orderId; // ✅ 백엔드에서 orderId 리턴되도록 해야 함
        console.log("응답 받은 orderId:", orderId);
        if (!orderId) return alert("orderId가 존재하지 않습니다.");
        navigate(`/order/${orderId}`);    // ✅ 결제 페이지로 이동
      } else {
        alert("구매 입찰이 등록되었습니다.");
      }

      setShowBuyModal(false);
      setSelectedSize("");
      setBuyPrice("");
    } catch {
      alert("구매 요청 실패");
    }
  };

  const handleSell = async () => {
    if (!selectedSize || !sellPrice) return alert("사이즈와 가격을 모두 입력해주세요.");
    try {
      const res = await axios.post("/biddings", {
        productId: Number(productId),
        size: selectedSize,
        price: parseInt(sellPrice, 10),
        position: "SELL"
      });
      alert(res.data.matched ? "즉시 판매가 완료되었습니다!" : "판매 등록이 완료되었습니다.");
      setShowSellModal(false);
      setSelectedSize("");
      setSellPrice("");
    } catch {
      alert("판매 요청 실패");
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

      <button style={{ marginTop: "1rem", marginRight: "1rem" }} onClick={() => setShowModal(true)}>
        위시 리스트 담기
      </button>

      <button style={{ marginTop: "1rem", marginLeft: "1rem" }} onClick={() => setShowBuyModal(true)}>
        구매
      </button>

      <button style={{ marginTop: "1rem" }} onClick={() => setShowSellModal(true)}>
        판매
      </button>
      
      {/* 위시리스트 모달 */}
      {showModal && (
        <div style={modalBackdropStyle}>
          <div style={modalBoxStyle}>
            <h3>사이즈 선택</h3>
            <div style={sizeButtonWrapper}>
              {product.sizes.map((size, idx) => (
                <button
                  key={idx}
                  style={getSizeButtonStyle(size === selectedSize)}
                  onClick={() => setSelectedSize(size)}
                >
                  {size}
                </button>
              ))}
            </div>

            <div style={modalButtonRow}>
              <button onClick={handleWishlist}>위시리스트 담기</button>
              <button onClick={() => setShowModal(false)}>닫기</button>
            </div>
          </div>
        </div>
      )}

      {/* 구매 모달 */}
      {showBuyModal && (
        <div style={modalBackdropStyle}>
          <div style={modalBoxStyle}>
            <h3>구매 등록</h3>
            <div style={sizeButtonWrapper}>
              {product.sizes.map((size) => (
                <button key={size} style={getSizeButtonStyle(size === selectedSize)} onClick={() => setSelectedSize(size)}>{size}</button>
              ))}
            </div>
            <input type="number" value={buyPrice} onChange={(e) => setBuyPrice(e.target.value)} placeholder="희망 구매가" />
            <div style={modalButtonRow}>
              <button onClick={handleBuy} disabled={!buyPrice} style={{ backgroundColor: isBuyNowAvailable() ? "black" : "gray", color: "white" }}>
                {isBuyNowAvailable() ? "즉시 구매" : "구매 등록"}
              </button>
              <button onClick={() => setShowBuyModal(false)}>닫기</button>
            </div>
          </div>
        </div>
      )}

      {/* 판매 모달 */}
      {showSellModal && (
        <div style={modalBackdropStyle}>
          <div style={modalBoxStyle}>
            <h3>판매 등록</h3>
            <div style={sizeButtonWrapper}>
              {product.sizes.map((size) => (
                <button key={size} style={getSizeButtonStyle(size === selectedSize)} onClick={() => setSelectedSize(size)}>{size}</button>
              ))}
            </div>
            <input type="number" value={sellPrice} onChange={(e) => setSellPrice(e.target.value)} placeholder="희망 판매가" />
            <div style={modalButtonRow}>
              <button onClick={handleSell} disabled={!sellPrice} style={{ backgroundColor: isSellNowAvailable() ? "black" : "gray", color: "white" }}>
                {isSellNowAvailable() ? "즉시 판매" : "판매 등록"}
              </button>
              <button onClick={() => setShowSellModal(false)}>닫기</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ProductDetailPage;

// ✅ 스타일 객체들
const modalBackdropStyle = {
  position: "fixed",
  top: 0, left: 0, right: 0, bottom: 0,
  backgroundColor: "rgba(0,0,0,0.5)",
  display: "flex", justifyContent: "center", alignItems: "center",
  zIndex: 9999
};

const modalBoxStyle = {
  backgroundColor: "#fff",
  padding: "2rem",
  borderRadius: "8px",
  minWidth: "300px"
};

const sizeButtonWrapper = {
  display: "flex",
  flexWrap: "wrap",
  gap: "0.5rem",
  marginBottom: "1rem"
};

const getSizeButtonStyle = (selected) => ({
  padding: "0.5rem 1rem",
  border: selected ? "2px solid black" : "1px solid gray",
  backgroundColor: selected ? "#f0f0f0" : "#fff",
  cursor: "pointer"
});

const modalButtonRow = {
  marginTop: "1rem",
  display: "flex",
  justifyContent: "space-between"
};
