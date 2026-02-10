const book = document.getElementById("book");
const button = document.getElementById("welcomeBtn");
const cover = document.querySelector(".cover.front");

window.addEventListener("pageshow", (event) => {
    if (event.persisted) {
        book.classList.remove("open");
    }
});

button.addEventListener("click", () => {
    book.classList.add("open");

    cover.addEventListener("transitionend", () => {
        window.location.href = "/search.html";
    }, { once: true });
});
