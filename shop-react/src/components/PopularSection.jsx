import ProductCard from "./ProductCard";
import { useEffect, useState } from "react";
import axios from "../api/axiosInstance";

const brands = [
  { id: 1, name: "Nike" },
  { id: 2, name: "Adidas" },
  { id: 3, name: "New Balance" },
];

const PopularSection = () => {
  const [brandProducts, setBrandProducts] = useState({});

  useEffect(() => {
    const fetchProducts = async () => {
      const data = {};
      for (const brand of brands) {
        try {
          const res = await axios.get(`/products/popular?brandId=${brand.id}`);
          data[brand.name] = res.data;
        } catch (e) {
          console.error(`${brand.name} 상품 불러오기 실패`, e);
        }
      }
      setBrandProducts(data);
    };

    fetchProducts();
  }, []);

  return (
    <div style={{ padding: "2rem" }}>
      {brands.map((brand) => (
        <section key={brand.id} style={{ marginBottom: "2rem" }}>
          <h3>{brand.name} 인기 상품</h3>
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(200px, 1fr))", gap: "1rem" }}>
            {(brandProducts[brand.name] || []).map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>
        </section>
      ))}
    </div>
  );
};

export default PopularSection;
