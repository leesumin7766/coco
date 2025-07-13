const GoTrading = () => {
  return (
    <div style={{ padding: "0.5rem", textAlign: "center", backgroundColor: "#f9f9f9" }}>
      <h2>가장 인기 있는 스니커즈를 쉽고 빠르게 거래해보세요.</h2>
      <button
        style={{
          marginTop: "0.1rem",
          padding: "1rem 2rem",
          backgroundColor: "black",
          color: "white",
          textAlign: "center",
          border: "none",
          borderRadius: "4px",
          fontSize: "1rem",
          cursor: "pointer",
        }}
        onClick={() => window.scrollTo({ top: 1000, behavior: "smooth" })}
      >
        지금 거래하기
      </button>
    </div>
  );
};

export default GoTrading;