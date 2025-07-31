import { useSearchParams } from "react-router-dom";

const PaymentFailPage = () => {
  const [searchParams] = useSearchParams();
  const orderId = searchParams.get("orderId");
  const code = searchParams.get("code");
  const message = searchParams.get("message");

  return (
    <div className="p-4 max-w-xl mx-auto">
      <h2 className="text-xl font-bold mb-4 text-red-600">결제 실패</h2>
      <p><strong>실패 코드:</strong> {code}</p>
      <p><strong>실패 사유:</strong> {decodeURIComponent(message || "")}</p>
      <p><strong>주문번호:</strong> {orderId}</p>
      <button onClick={() => window.history.back()} className="mt-4 bg-blue-500 text-white px-4 py-2 rounded">
        이전 페이지로
      </button>
    </div>
  );
};

export default PaymentFailPage;
