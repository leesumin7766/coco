import axios from "axios";

const axiosInstance = axios.create({
  baseURL: "http://localhost:3000/api", // 백엔드 API의 base URL
});

// 요청 시 accessToken 자동 주입
axiosInstance.interceptors.request.use(
  config => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// 응답에서 에러 처리도 여기에 추가할 수 있음 (옵션)
axiosInstance.interceptors.response.use(
  res => res,
  err => {
    if (err.response && err.response.status === 401) {
      alert("세션이 만료되었습니다. 다시 로그인해주세요.");
      localStorage.removeItem("accessToken");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

export default axiosInstance;