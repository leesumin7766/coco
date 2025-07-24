import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "../api/axiosInstance";

const OrderPage = () => {
  const { orderId } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [address, setAddress] = useState("");

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        const res = await axios.get(`/orders/${orderId}`);
        setOrder(res.data);
      } catch (err) {
        alert("주문 정보를 불러오지 못했습니다.");
      }
    };

    fetchOrder();
  }, [orderId]);

  const handlePayment = async () => {
    try {
      // 주소 유효성 체크
      if (!address) return alert("배송지를 입력해주세요.");

      // Toss API 연동을 위한 결제 준비 요청 (백엔드에서 처리 가능)
      const res = await axios.post(`/api/payments/prepare`, {
        orderId,
        address,
      });

      window.location.href = res.data.paymentUrl; // Toss 결제 페이지 이동
    } catch (err) {
      alert("결제 요청 실패");
    }
  };

  const handleCancel = async () => {
    try {
      await axios.patch(`/api/orders/${orderId}/cancel`);
      alert("주문이 취소되었습니다.");
      navigate("/mypage");
    } catch (err) {
      alert("취소 실패");
    }
  };

  const handleAddressSearch = () => {
    // 카카오맵 주소 검색 (window daum 우편번호 서비스 활용)
    new window.daum.Postcode({
      oncomplete: function (data) {
        setAddress(data.address);
      },
    }).open();
  };

  if (!order) return <div>로딩 중...</div>;

  return (
    <div className="p-4 max-w-xl mx-auto">
      <h2 className="text-xl font-bold mb-4">주문 정보</h2>
      <div className="border rounded p-4 shadow">
        <p><strong>상품명:</strong> {order.productName ?? "없음"}</p>
        <p><strong>사이즈:</strong> {order.size ?? "없음"}</p>
        <p><strong>가격:</strong> {order.price != null ? order.price.toLocaleString() : "없음"}원</p>
        <p><strong>판매자:</strong> {order.seller ?? "없음"}</p>
      </div>

      <div className="mt-6">
        <label className="block mb-1 font-semibold">배송지</label>
        <input
          type="text"
          value={address}
          onChange={(e) => setAddress(e.target.value)}
          className="border p-2 w-full rounded"
          placeholder="주소 검색 버튼을 클릭하세요"
          readOnly
        />
        <button
          onClick={handleAddressSearch}
          className="mt-2 px-4 py-2 bg-blue-500 text-white rounded"
        >
          주소 검색
        </button>
      </div>

      <div className="mt-6 flex gap-4">
        <button
          onClick={handlePayment}
          className="bg-green-500 text-white px-4 py-2 rounded"
        >
          결제하기
        </button>
        <button
          onClick={handleCancel}
          className="bg-gray-400 text-white px-4 py-2 rounded"
        >
          결제 취소
        </button>
      </div>
    </div>
  );
};

export default OrderPage;