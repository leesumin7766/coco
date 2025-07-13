import { useState, useEffect } from "react";
import axios from "../api/axiosInstance";

const ProductUpdate = () => {
  const [name, setName] = useState("");
  const [nameKr, setNameKr] = useState("");
  const [modelNumber, setModelNumber] = useState("");
  const [releasePrice, setReleasePrice] = useState("");
  const [brandId, setBrandId] = useState("");
  const [brands, setBrands] = useState([]);
  const [sizeIds, setSizeIds] = useState([]);
  const [imageUrls, setImageUrls] = useState([""]);

  // 브랜드 목록 불러오기
  useEffect(() => {
    axios.get("/brands")
      .then(res => setBrands(res.data))
      .catch(err => console.error("브랜드 불러오기 실패", err));
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      await axios.post("/products", {
        nameKr,
        name,
        modelNumber,
        releasePrice: parseInt(releasePrice),
        brandId: parseInt(brandId), // brandId가 문자열일 수 있으니 숫자로 변환
        sizeIds,
        imageUrls,
      });

      alert("제품이 등록되었습니다!");
    } catch (err) {
      console.error(err);
      alert("등록 실패");
    }
  };

  const handleImageChange = (index, value) => {
    const updated = [...imageUrls];
    updated[index] = value;
    setImageUrls(updated);
  };

  return (
    <div style={{ padding: "2rem" }}>
      <h2>제품 등록</h2>
      <form onSubmit={handleSubmit}>
        <input
            placeholder="제품명 (한글)"
            value={nameKr}
            onChange={(e) => setNameKr(e.target.value)}
            required
        />
        <input
          placeholder="제품명"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <input
          placeholder="모델 번호"
          value={modelNumber}
          onChange={(e) => setModelNumber(e.target.value)}
          required
        />
        <input
          type="number"
          placeholder="출시가"
          value={releasePrice}
          onChange={(e) => setReleasePrice(e.target.value)}
          required
        />

        {/* 브랜드 드롭다운 */}
        <select
          value={brandId}
          onChange={(e) => setBrandId(e.target.value)}
          required
        >
          <option value="">브랜드 선택</option>
          {brands.map((brand) => (
            <option key={brand.id} value={brand.id}>
              {brand.name}
            </option>
          ))}
        </select>

        {/* 사이즈 ID 입력 */}
        <input
          placeholder="사이즈 ID (쉼표로 구분, 예: 1,2,3)"
          onChange={(e) =>
            setSizeIds(e.target.value.split(",").map((v) => parseInt(v.trim())))
          }
        />

        {/* 이미지 URL 입력 */}
        {imageUrls.map((url, i) => (
          <input
            key={i}
            placeholder={`이미지 URL ${i + 1}`}
            value={url}
            onChange={(e) => handleImageChange(i, e.target.value)}
          />
        ))}
        <button
          type="button"
          onClick={() => setImageUrls([...imageUrls, ""])}
        >
          이미지 추가
        </button>

        <br /><br />
        <button type="submit">제품 등록</button>
      </form>
    </div>
  );
};

export default ProductUpdate;
