import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import axios from "../api/axiosInstance";

const PaymentSuccessPage = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    const confirmPayment = async () => {
      const paymentKey = searchParams.get("paymentKey");
      const orderId = searchParams.get("orderId");
      const amount = searchParams.get("amount");

      try {
        await axios.post("/payments/confirm", {
          paymentKey,
          orderId,
          amount: parseInt(amount),
        });

        alert("결제가 완료되었습니다.");
        navigate("/mypage");
      } catch (err) {
        alert("결제 확인 중 오류가 발생했습니다.");
        navigate("/payments/fail");
      }
    };

    confirmPayment();
  }, [searchParams, navigate]);

  return <div className="p-4 text-center">결제 확인 중입니다...</div>;
};

export default PaymentSuccessPage;
