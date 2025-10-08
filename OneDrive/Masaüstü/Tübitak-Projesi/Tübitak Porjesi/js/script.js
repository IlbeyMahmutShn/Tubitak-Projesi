const searchForm = document.querySelector(".search-form");
const cartItems = document.querySelector(".cart-items-container");
const navbar = document.querySelector(".navbar");

/* butonlar başlangıç */

const searchBtn = document.querySelector("#search-btn");
const shoppingBtn = document.querySelector("#shopping-btn");
const menuBtn = document.querySelector("#menu-btn");

searchBtn.addEventListener("click", function () {
  searchForm.classList.toggle("active");
  document.addEventListener("click", function (e) {
    if (
      !e.composedPath().includes(searchBtn) &&
      !e.composedPath().includes(searchForm)
    ) {
      searchForm.classList.remove("active");
    }
  });
});

menuBtn.addEventListener("click", function () {
  navbar.classList.toggle("active");
  document.addEventListener("click", function (e) {
    if (
      !e.composedPath().includes(menuBtn) &&
      !e.composedPath().includes(navbar)
    ) {
      navbar.classList.remove("active");
    }
  });
});

const searchBox = document.getElementById('search-box');
searchBox.addEventListener('keypress', function (e) {
  // Enter tuşuna basıldığında
  if (e.key === 'Enter') {
    const searchTerm = e.target.value.toLowerCase();
    const sections = document.querySelectorAll('section');
    sections.forEach(function (section) {
      const sectionContent = section.textContent.toLowerCase();
      if (sectionContent.includes(searchTerm)) {
        window.scrollTo({ top: section.offsetTop, behavior: 'smooth' });
        e.target.value = '';
      }
    });
  }
});

/* butonlar bitiş */
