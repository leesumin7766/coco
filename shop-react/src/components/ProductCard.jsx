const ProductCard = ({ product }) => {
  return (
    <div style={{ border: "1px solid #eee", borderRadius: "8px", padding: "1rem", textAlign: "center" }}>
      <img
        src={product.thumbnailUrl || "/images/placeholder.png"}
        alt={product.name_kr || product.name}
        style={{ width: "100%", height: "180px", objectFit: "cover" }}
      />
      <h4>{product.name_kr || product.name}</h4>
      <p>{product.brand?.name}</p>
      <p>{product.releasePrice?.toLocaleString()}원</p>
    </div>
  );
};

export default ProductCard;
