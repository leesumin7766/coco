import React from 'react';

const AddressSearch = ({ setAddress }) => {
  const loadPostcode = () => {
    new window.daum.Postcode({
      oncomplete: function (data) {
        const fullAddress = data.address;
        setAddress(fullAddress);
      }
    }).open();
  };

  return (
    <div>
      <input
        type="text"
        placeholder="주소를 검색하려면 클릭"
        onClick={loadPostcode}
        readOnly
      />
    </div>
  );
};

export default AddressSearch;
