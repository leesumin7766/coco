import { useState, useEffect } from "react";
import axios from "../api/axiosInstance"; // Axios 인스턴스 import
import styles from "./MyPage.module.css";
import { useNavigate } from "react-router-dom";
import ProductCard from "../components/ProductCard";

const MyPage = () => {
  const [activeTab, setActiveTab] = useState("orders");
  const [orders, setOrders] = useState([]);
  const [wishlist, setWishlist] = useState([]);
  const [sales, setSales] = useState([]);
  const [userInfo, setUserInfo] = useState({});
  const [isSeller, setIsSeller] = useState(false);
  const [password, setPassword] = useState(""); // ✅ 비밀번호 상태 추가
  const navigate = useNavigate(); // 추가

  useEffect(() => {
    if (activeTab === "info") {
      axios.get("/mypage/info")
        .then((res) => {
          setUserInfo(res.data);
          setIsSeller(res.data.role === "SELLER");
        })
        .catch((err) => {
          console.error("사용자 정보 로딩 실패", err);
        });
    }
  }, [activeTab]);

  const fetchOrders = async () => {
    const res = await axios.get("/mypage/orders");
    setOrders(res.data);
  };

  const fetchWishlist = async () => {
    const res = await axios.get("/mypage/wishlist");
    setWishlist(res.data);
  };

  const fetchSales = async () => {
    const res = await axios.get("/mypage/sales");
    setSales(res.data);
  };

  useEffect(() => {
    if (activeTab === "orders") fetchOrders();
    if (activeTab === "wishlist") fetchWishlist();
    if (activeTab === "sales" && isSeller) fetchSales();
  }, [activeTab, isSeller]);

  const handleChangePassword = async () => {
    if (!password) {
      alert("비밀번호를 입력하세요.");
      return;
    }

    try {
      await axios.post("/mypage/password", {
        newPassword: password,
      });
      alert("비밀번호가 성공적으로 변경되었습니다.");
      setPassword(""); // 입력창 초기화
    } catch (err) {
      console.error("비밀번호 변경 실패", err);
      alert("비밀번호 변경에 실패했습니다.");
    }
  };

  return (
    <div className={styles.container}>
      <h2>마이페이지</h2>

      <div className={styles.tabMenu}>
        <button onClick={() => setActiveTab("orders")}>🛒 구매 내역</button>
        <button onClick={() => setActiveTab("wishlist")}>❤️ 위시리스트</button>
        {isSeller && <button onClick={() => setActiveTab("sales")}>🛍️ 판매 내역</button>}
        <button onClick={() => setActiveTab("info")}>🧑 내 정보 수정</button>
      </div>

      <div style={{ margin: "1rem 0" }}>
        <button onClick={() => navigate("/product/new")}>📦 제품 등록</button>
      </div>

      <button onClick={() => navigate("/mypage/products")}>
        📦 제품 등록 내역
      </button>

      <div className={styles.content}>
        {activeTab === "orders" && (
          <div>
            <h3>구매 내역</h3>
            {orders.length === 0 ? <p>주문이 없습니다.</p> : (
              <table>
                <thead>
                  <tr>
                    <th>상품명</th>
                    <th>수량</th>
                    <th>총 가격</th>
                    <th>주문일</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map(order => (
                    <tr key={order.id}>
                      <td>{order.productName}</td>
                      <td>{order.quantity}</td>
                      <td>{order.totalPrice}원</td>
                      <td>{new Date(order.orderDate).toLocaleString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {activeTab === "wishlist" && (
        <div>
          <h3>위시리스트</h3>
          {wishlist.length === 0 ? (
          <p>위시리스트가 비어있습니다.</p>
          ) : (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))", gap: "1rem" }}>
        {wishlist.map(item => (
          <div key={item.id} onClick={() => navigate(`/product/${item.productId}`)} style={{ cursor: "pointer" }}>
            <ProductCard
              product={{
                id: item.productId,
                name: item.productName,
                name_kr: item.productNameKr,
                brand: { name: "" }, // brand 정보 없음
                releasePrice: item.releasePrice,
                thumbnailUrl: item.imageUrl,
              }}
            />
          </div>
        ))}
      </div>
    )}
  </div>
)}

        {activeTab === "sales" && isSeller && (
          <div>
            <h3>판매 내역</h3>
            {sales.length === 0 ? <p>판매 내역이 없습니다.</p> : (
              <table>
                <thead>
                  <tr>
                    <th>상품명</th>
                    <th>판매 수량</th>
                    <th>판매일</th>
                  </tr>
                </thead>
                <tbody>
                  {sales.map(item => (
                    <tr key={item.id}>
                      <td>{item.productName}</td>
                      <td>{item.quantity}</td>
                      <td>{new Date(item.saleDate).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}

        {activeTab === "info" && (
          <div>
            <h3>내 정보</h3>
            <p>이메일: {userInfo.email}</p>
            <p>이름: {userInfo.name}</p>

            <div>
              <h4>비밀번호 변경</h4>
              <input
                type="password"
                placeholder="새 비밀번호 입력"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <button onClick={handleChangePassword}>비밀번호 변경</button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default MyPage;
