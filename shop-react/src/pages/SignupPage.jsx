import React, { useState } from 'react';
import Address from '../components/Address';
import { useNavigate } from 'react-router-dom';

const SignupPage = () => {
  const [form, setForm] = useState({
    email: '',
    password: '',
    name: '',
    address: ''
  });

  const navigate = useNavigate();

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const response = await fetch('/api/auth/signup', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(form)
    });

    if (response.ok) {
      alert('회원가입 성공');
      navigate('/login');
    } else {
      alert('회원가입 실패');
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input name="email" placeholder="이메일" onChange={handleChange} />
      <input name="password" type="password" placeholder="비밀번호" onChange={handleChange} />
      <input name="name" placeholder="이름" onChange={handleChange} />

      <Address setAddress={(addr) => setForm({ ...form, address: addr })} />
      <p>선택한 주소: {form.address}</p>

      <button type="submit">회원가입</button>
    </form>
  );
};

export default SignupPage;
