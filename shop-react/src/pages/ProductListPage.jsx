import { useEffect, useState } from "react";
import axios from "../api/axiosInstance";
import styles from "./ProductListPage.module.css"; // 필요시 스타일 작성

const ProductListPage = () => {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    axios.get("/mypage/products") // JWT 토큰 기반 userId 추출
      .then((res) => setProducts(res.data))
      .catch((err) => {
        console.error("제품 목록 조회 실패", err);
        alert("제품 목록을 불러오지 못했습니다.");
      });
  }, []);

  return (
    <div className={styles.container}>
      <h2>제품 등록 내역</h2>
      {products.length === 0 ? (
        <p>등록한 제품이 없습니다.</p>
      ) : (
        <table className={styles.table}>
          <thead>
            <tr>
              <th>이미지</th>
              <th>제품명</th>
              <th>모델 번호</th>
              <th>출시가</th>
              <th>등록일</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product) => (
              <tr key={product.id}>
                <td>
                  <img
                    src={product.imageUrls?.[0] || "/no-image.png"}
                    alt={product.nameKr}
                    style={{ width: "300px", height: "300px", objectFit: "cover", marginBottom: "1rem" }}
                  />
                </td>
                <td>{product.nameKr}</td>
                <td>{product.modelNumber}</td>
                <td>{product.releasePrice.toLocaleString()}원</td>
                <td>{new Date(product.createdAt).toLocaleDateString()}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default ProductListPage;
