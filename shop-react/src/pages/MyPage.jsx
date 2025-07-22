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
  const [password, setPassword] = useState(""); // ✅ 비밀번호 상태 추가
  const [saleStatus, setsaleStatus] = useState("ALL");
  const [buys, setBuys] = useState([]);
  const [buyStatus, setBuyStatus] = useState("ALL");
  const navigate = useNavigate(); // 추가

  useEffect(() => {
    if (activeTab === "info") {
      axios.get("/mypage/info")
        .then((res) => {
          setUserInfo(res.data);
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

  const fetchBuys = async () => {
    try {
      const res = await axios.get("/mypage/biddings/buys");
      setBuys(res.data);
    } catch (err) {
      console.error("구매 입찰 내역 로딩 실패", err);
    }
  };
  const fetchSales = async () => {
    try {
      const res = await axios.get("/mypage/biddings/sales"); // ✅ 변경된 경로
      setSales(res.data);
    } catch (err) {
      console.error("판매 내역 로딩 실패", err);
    }
  };

  const handleCancelBidding = async (biddingId) => {
    const confirm = window.confirm("정말 취소하시겠습니까?");
    if (!confirm) return;

    try {
      await axios.put(`/mypage/biddings/${biddingId}/cancel`);
      alert("취소가 완료되었습니다.");
      fetchSales(); // 목록 갱신
    } catch (err) {
      console.error("취소 실패:", err);
      alert("취소에 실패했습니다: " + (err.response?.data?.message || err.message));
    }
  };
  useEffect(() => {
    if (activeTab === "orders") fetchOrders();
    if (activeTab === "wishlist") fetchWishlist();
    if (activeTab === "sales") fetchSales();
    if (activeTab === "buys") fetchBuys();
  }, [activeTab]);

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
        <button onClick={() => setActiveTab("buys")}>💰 구매 등록</button>
        <button onClick={() => setActiveTab("sales")}>🛍️ 판매 내역</button>
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
        {activeTab === "buys" && (
  <div>
    <h3>구매 등록 현황</h3>

    <div style={{ marginBottom: "1rem" }}>
      <label>상태 필터: </label>
      <select value={buyStatus} onChange={(e) => setBuyStatus(e.target.value)}>
        <option value="ALL">전체</option>
        <option value="PENDING">등록됨</option>
        <option value="COMPLETED">거래 완료</option>
        <option value="CANCELLED">취소됨</option>
      </select>
    </div>

    {buys.length === 0 ? (
      <p>등록된 구매 입찰이 없습니다.</p>
    ) : (
      <table>
        <thead>
          <tr>
            <th>상품명</th>
            <th>사이즈</th>
            <th>희망가</th>
            <th>상태</th>
            <th>등록일</th>
          </tr>
        </thead>
        <tbody>
          {buys
            .filter((item) => buyStatus === "ALL" || item.status === buyStatus)
            .map((item) => (
              <tr key={item.id}>
                <td>{item.productName}</td>
                <td>{item.size}</td>
                <td>{item.price.toLocaleString()}원</td>
                <td>{item.status}</td>
                <td>{new Date(item.createdAt).toLocaleDateString()}</td>
              </tr>
            ))}
        </tbody>
      </table>
    )}
  </div>
)}
        {activeTab === "sales" && (
          <div>
            <h3>판매 등록 현황</h3>

            <div style={{ marginBottom: "1rem" }}>
              <label>상태 필터: </label>
              <select value={saleStatus} onChange={(e) => setsaleStatus(e.target.value)}>
                <option value="ALL">전체</option>
                <option value="PENDING">등록됨</option>
                <option value="COMPLETED">거래 완료</option>
                <option value="CANCELLED">취소됨</option>
              </select>
            </div>

            {sales.length === 0 ? (
              <p>등록된 판매 입찰이 없습니다.</p>
            ) : (
              <table>
                <thead>
                  <tr>
                    <th>상품명</th>
                    <th>사이즈</th>
                    <th>등록 가격</th>
                    <th>상태</th>
                    <th>등록일</th>
                  </tr>
                </thead>
                <tbody>
                  {sales
                    .filter((item) => saleStatus === "ALL" || item.status === saleStatus)
                    .map((item) => (
                      <tr key={item.id}>
                        <td>{item.productName}</td>
                        <td>{item.size}</td>
                        <td>{item.price.toLocaleString()}원</td>
                        <td>{item.status}</td>
                        <td>{new Date(item.createdAt).toLocaleDateString()}</td>
                        <td>
                        {item.status === "PENDING" && (
                          <button onClick={() => handleCancelBidding(item.id)}>❌ 취소하기</button>
                        )}
                        </td>
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
