import Slider from "react-slick";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";

const MainBanner = () => {
  const banners = [
    "/images/mihara.jpg",
    "/images/992.jpg",
    "/images/running.jpg",
  ];

  const settings = {
    dots: true,
    infinite: true,
    autoplay: true,
    speed: 800,
    slidesToShow: 1,
    slidesToScroll: 1,
  };

  return (
    <div style={{ maxWidth: "100%", overflow: "hidden" }}>
      <Slider {...settings}>
        {banners.map((src, index) => (
          <div key={index}>
            <img
              src={src}
              alt={`banner-${index}`}
              style={{ width: "100%", height: "400px", objectFit: "cover" }}
            />
          </div>
        ))}
      </Slider>
    </div>
  );
};

export default MainBanner;
