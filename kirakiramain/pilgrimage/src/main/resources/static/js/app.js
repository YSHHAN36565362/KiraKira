const THEME_KEY = "kirakira-theme";

/* 지도: OpenStreetMap 타일 + Leaflet.js 사용 (API 키/결제 계정 불필요, 완전 무료).
   트래픽이 아주 커지면 tile.openstreetmap.org의 공정 사용 정책에 따라
   MapTiler/Stadia Maps 등 별도 타일 서버로 교체를 고려하세요. */
const DEFAULT_MAP_CENTER = [35.6812, 139.7671]; // Tokyo [lat, lng]
const DEFAULT_MAP_ZOOM = 6;

const themeToggleBtn = document.getElementById("theme-toggle");
const brandLink = document.getElementById("brand-link");
const mediaTypeSelect = document.getElementById("media-type");
const keywordInput = document.getElementById("keyword");
const searchBtn = document.getElementById("search-btn");
const placeListEl = document.getElementById("place-list");
const resultCountEl = document.getElementById("result-count");
const mapCanvasEl = document.getElementById("map-canvas");

const authStatusEl = document.getElementById("auth-status");
const loginBtn = document.getElementById("login-btn");
const signupBtn = document.getElementById("signup-btn");
const logoutBtn = document.getElementById("logout-btn");
const myFavoritesBtn = document.getElementById("my-favorites-btn");
const adminBtn = document.getElementById("admin-btn");

const authDialog = document.getElementById("auth-dialog");
const loginForm = document.getElementById("login-form");
const signupForm = document.getElementById("signup-form");
const loginError = document.getElementById("login-error");
const signupError = document.getElementById("signup-error");

const placeDialog = document.getElementById("place-dialog");
const detailMediaType = document.getElementById("detail-media-type");
const detailPlaceName = document.getElementById("detail-place-name");
const detailWorkTitle = document.getElementById("detail-work-title");
const detailDescription = document.getElementById("detail-description");
const detailMeta = document.getElementById("detail-meta");
const favoriteToggleBtn = document.getElementById("favorite-toggle");
const favoriteToggleLabel = document.getElementById("favorite-toggle-label");
const detailRealFigure = document.getElementById("detail-real-figure");
const detailRealImage = document.getElementById("detail-real-image");
const detailAnimeFigure = document.getElementById("detail-anime-figure");
const detailAnimeImage = document.getElementById("detail-anime-image");

const reviewCountEl = document.getElementById("review-count");
const reviewAverageEl = document.getElementById("review-average");
const reviewListEl = document.getElementById("review-list");
const reviewForm = document.getElementById("review-form");
const reviewLoginNotice = document.getElementById("review-login-notice");
const reviewFields = document.getElementById("review-fields");
const reviewRatingSelect = document.getElementById("review-rating");
const reviewContentInput = document.getElementById("review-content");
const reviewError = document.getElementById("review-error");
const reviewSubmitBtn = document.getElementById("review-submit-btn");
const reviewDeleteBtn = document.getElementById("review-delete-btn");

const adminDialog = document.getElementById("admin-dialog");
const adminPlaceForm = document.getElementById("admin-place-form");
const adminPlaceIdInput = document.getElementById("admin-place-id");
const adminMediaType = document.getElementById("admin-media-type");
const adminWorkTitle = document.getElementById("admin-work-title");
const adminPlaceName = document.getElementById("admin-place-name");
const adminRegion = document.getElementById("admin-region");
const adminAddress = document.getElementById("admin-address");
const adminLatitude = document.getElementById("admin-latitude");
const adminLongitude = document.getElementById("admin-longitude");
const adminDescription = document.getElementById("admin-description");
const adminRealImage = document.getElementById("admin-real-image");
const adminAnimeImage = document.getElementById("admin-anime-image");
const adminPlaceError = document.getElementById("admin-place-error");
const adminPlaceSubmitBtn = document.getElementById("admin-place-submit-btn");
const adminPlaceResetBtn = document.getElementById("admin-place-reset-btn");
const adminPlaceListEl = document.getElementById("admin-place-list");
const adminPlaceCountEl = document.getElementById("admin-place-count");

const mediaTypeLabel = { ANIME: "애니메이션", MOVIE: "영화", DRAMA: "드라마" };

let currentUser = null;
let favoritePlaceIds = new Set();
let showingFavoritesOnly = false;
let currentDetailPlaceId = null;
let currentUserReviewId = null;
let adminPlaces = [];

/* ---------- Theme ---------- */

function applyTheme(theme) {
  document.documentElement.setAttribute("data-theme", theme);
  themeToggleBtn.textContent = theme === "dark" ? "Light" : "Dark";
  localStorage.setItem(THEME_KEY, theme);
}

function initTheme() {
  const saved = localStorage.getItem(THEME_KEY);
  if (saved) {
    applyTheme(saved);
    return;
  }
  const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
  applyTheme(prefersDark ? "dark" : "light");
}

themeToggleBtn.addEventListener("click", () => {
  const current = document.documentElement.getAttribute("data-theme");
  applyTheme(current === "dark" ? "light" : "dark");
});

brandLink.addEventListener("click", () => {
  mediaTypeSelect.value = "";
  keywordInput.value = "";
  fetchPlaces();
});

/* ---------- Helpers ---------- */

function escapeHtml(value) {
  const div = document.createElement("div");
  div.textContent = value ?? "";
  return div.innerHTML;
}

async function readErrorMessage(response) {
  try {
    const body = await response.json();
    return body.message || "요청을 처리하지 못했습니다.";
  } catch {
    return "요청을 처리하지 못했습니다.";
  }
}

/* ---------- Map (Leaflet + OpenStreetMap) ---------- */

let map = null;
let mapMarkers = [];
let mapLoadFailed = false;

function showMapFallback(message) {
  mapCanvasEl.innerHTML = `
    <div class="map-fallback">
      <div class="fallback-icon" aria-hidden="true">✦</div>
      <p>${escapeHtml(message)}</p>
    </div>
  `;
}

function initMap() {
  if (typeof L === "undefined") {
    mapLoadFailed = true;
    showMapFallback("지도를 불러오지 못했습니다. 왼쪽 카드 목록에서 장소를 확인해 주세요.");
    return;
  }

  map = L.map(mapCanvasEl, {
    center: DEFAULT_MAP_CENTER,
    zoom: DEFAULT_MAP_ZOOM,
  });

  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener">OpenStreetMap</a> contributors',
  }).addTo(map);

  window.addEventListener("resize", () => map?.invalidateSize());
}

function createStampIcon() {
  return L.divIcon({
    className: "map-stamp-pin",
    iconSize: [16, 16],
    iconAnchor: [8, 8],
  });
}

function clearMapMarkers() {
  mapMarkers.forEach((marker) => marker.remove());
  mapMarkers = [];
}

function renderMapMarkers(places) {
  if (mapLoadFailed || !map) {
    return;
  }
  map.invalidateSize();
  clearMapMarkers();

  if (places.length === 0) {
    return;
  }

  const bounds = [];
  places.forEach((place) => {
    const position = [place.latitude, place.longitude];
    const marker = L.marker(position, {
      icon: createStampIcon(),
      title: `${place.workTitle} · ${place.placeName}`,
    }).addTo(map);
    marker.on("click", () => openDetail(place.id));
    mapMarkers.push(marker);
    bounds.push(position);
  });

  map.fitBounds(bounds, { padding: [24, 24], maxZoom: 12 });
}

function focusMapOnPlace(place) {
  if (mapLoadFailed || !map) return;
  map.flyTo([place.latitude, place.longitude], 18, { duration: 0.8 });
}

/* ---------- Auth ---------- */

async function fetchCurrentUser() {
  const response = await fetch("/api/auth/me");
  currentUser = response.ok ? await response.json() : null;
  updateAuthUI();
}

function updateAuthUI() {
  if (currentUser) {
    authStatusEl.textContent = `${currentUser.nickname}님`;
    loginBtn.hidden = true;
    signupBtn.hidden = true;
    logoutBtn.hidden = false;
    myFavoritesBtn.hidden = false;
    adminBtn.hidden = currentUser.role !== "ADMIN";
  } else {
    authStatusEl.textContent = "";
    loginBtn.hidden = false;
    signupBtn.hidden = false;
    logoutBtn.hidden = true;
    myFavoritesBtn.hidden = true;
    adminBtn.hidden = true;
    favoritePlaceIds = new Set();
  }
  myFavoritesBtn.classList.toggle("active", showingFavoritesOnly);
  updateDashAuthUI();
}

function openAuthDialog(mode) {
  loginError.textContent = "";
  signupError.textContent = "";
  loginForm.hidden = mode !== "login";
  signupForm.hidden = mode !== "signup";
  authDialog.showModal();
}

loginBtn.addEventListener("click", () => openAuthDialog("login"));
signupBtn.addEventListener("click", () => openAuthDialog("signup"));

authDialog.querySelectorAll("[data-close-dialog]").forEach((btn) => {
  btn.addEventListener("click", () => authDialog.close());
});

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  loginError.textContent = "";
  const email = document.getElementById("login-email").value;
  const password = document.getElementById("login-password").value;

  const response = await fetch("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ email, password })
  });

  if (!response.ok) {
    loginError.textContent = await readErrorMessage(response);
    return;
  }

  currentUser = await response.json();
  updateAuthUI();
  authDialog.close();
  loginForm.reset();
  await fetchFavorites();
  await fetchPlaces();
});

signupForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  signupError.textContent = "";
  const email = document.getElementById("signup-email").value;
  const password = document.getElementById("signup-password").value;
  const nickname = document.getElementById("signup-nickname").value;

  const signupResponse = await fetch("/api/auth/signup", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ email, password, nickname })
  });

  if (!signupResponse.ok) {
    signupError.textContent = await readErrorMessage(signupResponse);
    return;
  }

  const loginResponse = await fetch("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body: JSON.stringify({ email, password })
  });

  if (!loginResponse.ok) {
    signupError.textContent = "가입은 완료되었지만 자동 로그인에 실패했습니다. 로그인해 주세요.";
    return;
  }

  currentUser = await loginResponse.json();
  updateAuthUI();
  authDialog.close();
  signupForm.reset();
  await fetchFavorites();
  await fetchPlaces();
});

logoutBtn.addEventListener("click", async () => {
  await fetch("/api/auth/logout", { method: "POST", credentials: "include" });
  currentUser = null;
  showingFavoritesOnly = false;
  updateAuthUI();
  await fetchPlaces();
});

/* ---------- Favorites ---------- */

async function fetchFavorites() {
  if (!currentUser) {
    favoritePlaceIds = new Set();
    return;
  }
  const response = await fetch("/api/favorites", { credentials: "include" });
  if (!response.ok) return;
  const favorites = await response.json();
  favoritePlaceIds = new Set(favorites.map((f) => f.place.id));
}

async function toggleFavorite(placeId) {
  if (!currentUser) {
    openAuthDialog("login");
    return;
  }
  const isFavorited = favoritePlaceIds.has(placeId);
  const response = await fetch(`/api/favorites/${placeId}`, {
    method: isFavorited ? "DELETE" : "POST",
    credentials: "include"
  });
  if (!response.ok && response.status !== 409) return;

  if (isFavorited) {
    favoritePlaceIds.delete(placeId);
  } else {
    favoritePlaceIds.add(placeId);
  }

  if (showingFavoritesOnly) {
    await renderFavoritesOnly();
  } else {
    renderFavoriteButtons();
  }
  if (currentDetailPlaceId === placeId) {
    updateDetailFavoriteButton();
  }
  if (page2Loaded) {
    dashRenderTable(dashPlaces);
  }
}

myFavoritesBtn.addEventListener("click", async () => {
  showingFavoritesOnly = true;
  myFavoritesBtn.classList.add("active");
  await renderFavoritesOnly();
});

async function renderFavoritesOnly() {
  await fetchFavorites();
  const response = await fetch("/api/favorites", { credentials: "include" });
  const favorites = response.ok ? await response.json() : [];
  const places = favorites.map((f) => f.place);
  resultCountEl.textContent = `내 즐겨찾기 총 ${places.length}건`;
  renderPlaces(places);
  renderMapMarkers(places);
}

function renderFavoriteButtons() {
  document.querySelectorAll(".card-favorite-btn").forEach((btn) => {
    const placeId = Number(btn.dataset.placeId);
    const active = favoritePlaceIds.has(placeId);
    btn.querySelector(".favorite-label").textContent = active ? "즐겨찾기 해제" : "즐겨찾기";
    btn.classList.toggle("active", active);
  });
}

/* ---------- Place list ---------- */

function ratingText(place) {
  if (!place.reviewCount || place.reviewCount === 0) {
    return "리뷰 없음";
  }
  return `평점 ${place.averageRating.toFixed(1)} (${place.reviewCount}건)`;
}

function renderPlaces(places) {
  if (places.length === 0) {
    placeListEl.innerHTML = `<div class="empty-state">표시할 장소가 없습니다.</div>`;
    return;
  }

  placeListEl.innerHTML = places.map((place, index) => {
    const thumbSrc = place.realImageUrl || place.animeImageUrl;
    const thumbHtml = thumbSrc
      ? `<img class="card-thumb" src="${escapeHtml(thumbSrc)}" alt="" loading="lazy" onerror="this.remove()">`
      : "";
    return `
    <article class="place-card" data-place-id="${place.id}" style="--i:${Math.min(index, 10)}">
      ${thumbHtml}
      <div class="card-header">
        <span class="media-type" data-type="${place.mediaType}">${mediaTypeLabel[place.mediaType] ?? place.mediaType}</span>
        <button type="button" class="card-favorite-btn" data-place-id="${place.id}">
          <span class="stamp-dot" aria-hidden="true"></span><span class="favorite-label">즐겨찾기</span>
        </button>
      </div>
      <h3>${escapeHtml(place.placeName)}</h3>
      <p class="work-title">${escapeHtml(place.workTitle)}</p>
      <p class="meta">
        ${escapeHtml(place.region ?? "")}<br>
        ${place.latitude.toFixed(4)}, ${place.longitude.toFixed(4)}
      </p>
      <p class="rating">${ratingText(place)}</p>
    </article>
  `;
  }).join("");

  renderFavoriteButtons();

  placeListEl.querySelectorAll(".place-card").forEach((card) => {
    card.addEventListener("click", () => openDetail(Number(card.dataset.placeId)));
  });

  placeListEl.querySelectorAll(".card-favorite-btn").forEach((btn) => {
    btn.addEventListener("click", (event) => {
      event.stopPropagation();
      toggleFavorite(Number(btn.dataset.placeId));
    });
  });
}

async function fetchPlaces() {
  showingFavoritesOnly = false;
  myFavoritesBtn.classList.remove("active");
  const params = new URLSearchParams();
  if (mediaTypeSelect.value) params.set("mediaType", mediaTypeSelect.value);
  if (keywordInput.value.trim()) params.set("keyword", keywordInput.value.trim());

  const response = await fetch(`/api/places?${params.toString()}`);
  if (!response.ok) {
    placeListEl.innerHTML = `<div class="empty-state">데이터를 불러오지 못했습니다.</div>`;
    return;
  }
  const places = await response.json();
  resultCountEl.textContent = `총 ${places.length}건`;
  renderPlaces(places);
  renderMapMarkers(places);
}

searchBtn.addEventListener("click", fetchPlaces);
keywordInput.addEventListener("keydown", (event) => {
  if (event.key === "Enter") fetchPlaces();
});
mediaTypeSelect.addEventListener("change", fetchPlaces);

/* ---------- Place detail & reviews ---------- */

function updateDetailFavoriteButton() {
  const active = favoritePlaceIds.has(currentDetailPlaceId);
  favoriteToggleLabel.textContent = active ? "즐겨찾기 해제" : "즐겨찾기 추가";
  favoriteToggleBtn.classList.toggle("active", active);
}

favoriteToggleBtn.addEventListener("click", () => {
  if (currentDetailPlaceId !== null) toggleFavorite(currentDetailPlaceId);
});

function renderReviews(reviews) {
  reviewCountEl.textContent = reviews.length;
  if (reviews.length === 0) {
    reviewAverageEl.textContent = "-";
  } else {
    const avg = reviews.reduce((sum, r) => sum + r.rating, 0) / reviews.length;
    reviewAverageEl.textContent = avg.toFixed(1);
  }

  if (reviews.length === 0) {
    reviewListEl.innerHTML = `<p class="form-hint">아직 리뷰가 없습니다.</p>`;
  } else {
    reviewListEl.innerHTML = reviews.map((review) => `
      <div class="review-item">
        <div class="review-meta">${escapeHtml(review.nickname)} · 별점 ${review.rating}</div>
        <div class="review-content">${escapeHtml(review.content ?? "")}</div>
      </div>
    `).join("");
  }

  currentUserReviewId = null;
  if (currentUser) {
    const myReview = reviews.find((r) => r.userId === currentUser.id);
    if (myReview) {
      currentUserReviewId = myReview.id;
      reviewRatingSelect.value = String(myReview.rating);
      reviewContentInput.value = myReview.content ?? "";
      reviewSubmitBtn.textContent = "내 리뷰 수정";
      reviewDeleteBtn.hidden = false;
    } else {
      reviewRatingSelect.value = "5";
      reviewContentInput.value = "";
      reviewSubmitBtn.textContent = "리뷰 등록";
      reviewDeleteBtn.hidden = true;
    }
  }
}

async function openDetail(placeId) {
  currentDetailPlaceId = placeId;
  reviewError.textContent = "";

  const [placeResponse, reviewsResponse] = await Promise.all([
    fetch(`/api/places/${placeId}`),
    fetch(`/api/places/${placeId}/reviews`)
  ]);

  if (!placeResponse.ok) return;
  const place = await placeResponse.json();
  const reviews = reviewsResponse.ok ? await reviewsResponse.json() : [];

  detailMediaType.textContent = mediaTypeLabel[place.mediaType] ?? place.mediaType;
  detailMediaType.dataset.type = place.mediaType;
  detailPlaceName.textContent = place.placeName;
  detailWorkTitle.textContent = place.workTitle;
  detailDescription.textContent = place.description ?? "";
  detailMeta.textContent = `${place.region ?? ""} · ${place.address ?? ""}`;
  setDetailImage(detailRealFigure, detailRealImage, place.realImageUrl);
  setDetailImage(detailAnimeFigure, detailAnimeImage, place.animeImageUrl);
  focusMapOnPlace(place);

  updateDetailFavoriteButton();

  if (currentUser) {
    reviewLoginNotice.hidden = true;
    reviewFields.hidden = false;
  } else {
    reviewLoginNotice.hidden = false;
    reviewFields.hidden = true;
  }

  renderReviews(reviews);
  placeDialog.showModal();
}

placeDialog.querySelectorAll("[data-close-dialog]").forEach((btn) => {
  btn.addEventListener("click", () => placeDialog.close());
});

reviewForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  reviewError.textContent = "";

  const body = JSON.stringify({
    rating: Number(reviewRatingSelect.value),
    content: reviewContentInput.value
  });

  const url = currentUserReviewId
    ? `/api/reviews/${currentUserReviewId}`
    : `/api/places/${currentDetailPlaceId}/reviews`;
  const method = currentUserReviewId ? "PUT" : "POST";

  const response = await fetch(url, {
    method,
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body
  });

  if (!response.ok) {
    reviewError.textContent = await readErrorMessage(response);
    return;
  }

  await refreshDetailAfterReviewChange();
});

reviewDeleteBtn.addEventListener("click", async () => {
  if (!currentUserReviewId) return;
  const response = await fetch(`/api/reviews/${currentUserReviewId}`, {
    method: "DELETE",
    credentials: "include"
  });
  if (!response.ok) {
    reviewError.textContent = await readErrorMessage(response);
    return;
  }
  await refreshDetailAfterReviewChange();
});

async function refreshDetailAfterReviewChange() {
  const reviewsResponse = await fetch(`/api/places/${currentDetailPlaceId}/reviews`);
  const reviews = reviewsResponse.ok ? await reviewsResponse.json() : [];
  renderReviews(reviews);

  const placeResponse = await fetch(`/api/places/${currentDetailPlaceId}`);
  if (placeResponse.ok) {
    const updatedPlace = await placeResponse.json();
    if (!showingFavoritesOnly) {
      const card = placeListEl.querySelector(`.place-card[data-place-id="${updatedPlace.id}"] .rating`);
      if (card) card.textContent = ratingText(updatedPlace);
    }
  }
}

function setDetailImage(figureEl, imgEl, url) {
  if (!url) {
    figureEl.hidden = true;
    imgEl.removeAttribute("src");
    return;
  }
  imgEl.onerror = () => {
    figureEl.hidden = true;
  };
  imgEl.src = url;
  figureEl.hidden = false;
}

/* ---------- Admin: place management ---------- */

function resetAdminForm() {
  adminPlaceForm.reset();
  adminPlaceIdInput.value = "";
  adminMediaType.value = "ANIME";
  adminPlaceError.textContent = "";
  adminPlaceSubmitBtn.textContent = "장소 등록";
  adminPlaceResetBtn.hidden = true;
}

function fillAdminForm(place) {
  adminPlaceIdInput.value = place.id;
  adminMediaType.value = place.mediaType;
  adminWorkTitle.value = place.workTitle;
  adminPlaceName.value = place.placeName;
  adminRegion.value = place.region ?? "";
  adminAddress.value = place.address ?? "";
  adminLatitude.value = place.latitude;
  adminLongitude.value = place.longitude;
  adminDescription.value = place.description ?? "";
  adminRealImage.value = place.realImageUrl ?? "";
  adminAnimeImage.value = place.animeImageUrl ?? "";
  adminPlaceError.textContent = "";
  adminPlaceSubmitBtn.textContent = "장소 수정";
  adminPlaceResetBtn.hidden = false;
}

function renderAdminPlaceList() {
  adminPlaceCountEl.textContent = adminPlaces.length;

  if (adminPlaces.length === 0) {
    adminPlaceListEl.innerHTML = `<p class="form-hint">등록된 장소가 없습니다.</p>`;
    return;
  }

  adminPlaceListEl.innerHTML = adminPlaces.map((place) => `
    <div class="admin-place-row" data-place-id="${place.id}">
      <div class="admin-place-info">
        <strong>${escapeHtml(place.placeName)}</strong>
        <span>${mediaTypeLabel[place.mediaType] ?? place.mediaType} · ${escapeHtml(place.workTitle)}</span>
      </div>
      <div class="admin-place-actions">
        <button type="button" class="admin-edit-btn" data-place-id="${place.id}">수정</button>
        <button type="button" class="admin-delete-btn" data-place-id="${place.id}">삭제</button>
      </div>
    </div>
  `).join("");

  adminPlaceListEl.querySelectorAll(".admin-edit-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      const place = adminPlaces.find((p) => p.id === Number(btn.dataset.placeId));
      if (place) fillAdminForm(place);
    });
  });

  adminPlaceListEl.querySelectorAll(".admin-delete-btn").forEach((btn) => {
    btn.addEventListener("click", () => handleAdminDeleteClick(btn));
  });
}

async function handleAdminDeleteClick(btn) {
  if (!btn.classList.contains("danger-confirm")) {
    btn.classList.add("danger-confirm");
    btn.textContent = "정말 삭제?";
    return;
  }

  const placeId = Number(btn.dataset.placeId);
  const response = await fetch(`/api/admin/places/${placeId}`, {
    method: "DELETE",
    credentials: "include"
  });

  if (!response.ok) {
    adminPlaceError.textContent = await readErrorMessage(response);
    return;
  }

  if (Number(adminPlaceIdInput.value) === placeId) {
    resetAdminForm();
  }
  await loadAdminPlaces();
  await fetchPlaces();
}

async function loadAdminPlaces() {
  const response = await fetch("/api/places");
  adminPlaces = response.ok ? await response.json() : [];
  renderAdminPlaceList();
}

async function openAdminDialog() {
  adminPlaceError.textContent = "";
  await loadAdminPlaces();
  adminDialog.showModal();
}

adminBtn.addEventListener("click", openAdminDialog);

adminDialog.querySelectorAll("[data-close-dialog]").forEach((btn) => {
  btn.addEventListener("click", () => adminDialog.close());
});

adminPlaceResetBtn.addEventListener("click", resetAdminForm);

adminPlaceForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  adminPlaceError.textContent = "";

  const body = JSON.stringify({
    mediaType: adminMediaType.value,
    workTitle: adminWorkTitle.value,
    placeName: adminPlaceName.value,
    region: adminRegion.value || null,
    address: adminAddress.value || null,
    latitude: Number(adminLatitude.value),
    longitude: Number(adminLongitude.value),
    description: adminDescription.value || null,
    animeImageUrl: adminAnimeImage.value || null,
    realImageUrl: adminRealImage.value || null
  });

  const editingId = adminPlaceIdInput.value;
  const url = editingId ? `/api/admin/places/${editingId}` : "/api/admin/places";
  const method = editingId ? "PUT" : "POST";

  const response = await fetch(url, {
    method,
    headers: { "Content-Type": "application/json" },
    credentials: "include",
    body
  });

  if (!response.ok) {
    adminPlaceError.textContent = await readErrorMessage(response);
    return;
  }

  resetAdminForm();
  await loadAdminPlaces();
  await fetchPlaces();
});

/* ---------- Page 2: Dashboard (table view) ---------- */

const dashAuthStatusEl = document.getElementById("dash-auth-status");
const dashMediaTypeSelect = document.getElementById("dash-media-type");
const dashKeywordInput = document.getElementById("dash-keyword");
const dashSearchBtn = document.getElementById("dash-search-btn");
const dashAdminBtn = document.getElementById("dash-admin-btn");
const dashResultCountEl = document.getElementById("dash-result-count");
const dashPlaceTbody = document.getElementById("dash-place-tbody");

const dashFormTitle = document.getElementById("dashFormTitle");
const dashPlaceIdInput = document.getElementById("dash-place-id");
const dashAdminMediaType = document.getElementById("dash-admin-media-type");
const dashAdminWorkTitle = document.getElementById("dash-admin-work-title");
const dashAdminPlaceName = document.getElementById("dash-admin-place-name");
const dashAdminRegion = document.getElementById("dash-admin-region");
const dashAdminAddress = document.getElementById("dash-admin-address");
const dashAdminLatitude = document.getElementById("dash-admin-latitude");
const dashAdminLongitude = document.getElementById("dash-admin-longitude");
const dashSubmitBtn = document.getElementById("dash-submit-btn");
const dashCancelBtn = document.getElementById("dash-cancel-btn");

let dashPlaces = [];
let page2Loaded = false;

function updateDashAuthUI() {
  if (!dashAuthStatusEl) return;
  dashAuthStatusEl.textContent = currentUser ? `${currentUser.nickname}님` : "";
  if (dashAdminBtn) dashAdminBtn.hidden = !currentUser || currentUser.role !== "ADMIN";
}

function dashResetForm() {
  if (!dashPlaceIdInput) return;
  dashPlaceIdInput.value = "";
  dashAdminMediaType.value = "ANIME";
  dashAdminWorkTitle.value = "";
  dashAdminPlaceName.value = "";
  dashAdminRegion.value = "";
  dashAdminAddress.value = "";
  dashAdminLatitude.value = "";
  dashAdminLongitude.value = "";
  dashFormTitle.textContent = "장소 등록";
  dashSubmitBtn.textContent = "DB에 등록하기";
  dashCancelBtn.style.display = "none";
}

function dashFillForm(place) {
  dashPlaceIdInput.value = place.id;
  dashAdminMediaType.value = place.mediaType;
  dashAdminWorkTitle.value = place.workTitle;
  dashAdminPlaceName.value = place.placeName;
  dashAdminRegion.value = place.region ?? "";
  dashAdminAddress.value = place.address ?? "";
  dashAdminLatitude.value = place.latitude;
  dashAdminLongitude.value = place.longitude;
  dashFormTitle.textContent = "장소 수정";
  dashSubmitBtn.textContent = "DB 수정하기";
  dashCancelBtn.style.display = "block";
}

function dashRenderTable(places) {
  if (!dashPlaceTbody) return;

  if (dashResultCountEl) {
    dashResultCountEl.textContent = `총 ${places.length}건`;
  }

  if (places.length === 0) {
    dashPlaceTbody.innerHTML = `<tr><td colspan="7">표시할 장소가 없습니다.</td></tr>`;
    return;
  }

  const isAdmin = currentUser && currentUser.role === "ADMIN";

  dashPlaceTbody.innerHTML = places.map((place) => {
    const isFav = favoritePlaceIds.has(place.id);
    const actionsHtml = isAdmin
      ? `
        <button type="button" class="btn btn-secondary dash-edit-btn" data-place-id="${place.id}">수정</button>
        <button type="button" class="btn btn-primary dash-delete-btn" data-place-id="${place.id}">삭제</button>
      `
      : `<button type="button" class="btn btn-secondary dash-view-btn" data-place-id="${place.id}">상세</button>`;

    return `
    <tr data-place-id="${place.id}">
      <td><span class="dash-media-pill" data-type="${place.mediaType}">${mediaTypeLabel[place.mediaType] ?? place.mediaType}</span></td>
      <td class="td-work">${escapeHtml(place.workTitle)}</td>
      <td class="td-name">${escapeHtml(place.placeName)}</td>
      <td>${escapeHtml(place.region ?? "-")}</td>
      <td class="td-rating">${place.reviewCount ? place.averageRating.toFixed(1) : "-"}</td>
      <td><span class="dash-fav-star ${isFav ? "active" : ""}">${isFav ? "★" : "☆"}</span></td>
      <td><div class="td-actions">${actionsHtml}</div></td>
    </tr>
  `;
  }).join("");

  dashPlaceTbody.querySelectorAll(".dash-view-btn, .dash-edit-btn").forEach((btn) => {
    btn.addEventListener("click", () => {
      const place = dashPlaces.find((p) => p.id === Number(btn.dataset.placeId));
      if (!place) return;
      if (btn.classList.contains("dash-edit-btn")) {
        dashFillForm(place);
      } else {
        openDetail(place.id);
      }
    });
  });

  dashPlaceTbody.querySelectorAll(".dash-delete-btn").forEach((btn) => {
    btn.addEventListener("click", () => dashHandleDeleteClick(btn));
  });
}

async function dashHandleDeleteClick(btn) {
  if (!btn.classList.contains("danger-confirm")) {
    btn.classList.add("danger-confirm");
    btn.textContent = "정말 삭제?";
    return;
  }

  const placeId = Number(btn.dataset.placeId);
  const response = await fetch(`/api/admin/places/${placeId}`, {
    method: "DELETE",
    credentials: "include"
  });

  if (!response.ok) {
    return;
  }

  if (Number(dashPlaceIdInput.value) === placeId) {
    dashResetForm();
  }
  await dashFetchPlaces();
}

async function dashFetchPlaces() {
  const params = new URLSearchParams();
  if (dashMediaTypeSelect && dashMediaTypeSelect.value) params.set("mediaType", dashMediaTypeSelect.value);
  if (dashKeywordInput && dashKeywordInput.value.trim()) params.set("keyword", dashKeywordInput.value.trim());

  const response = await fetch(`/api/places?${params.toString()}`);
  if (!response.ok) {
    if (dashPlaceTbody) dashPlaceTbody.innerHTML = `<tr><td colspan="7">데이터를 불러오지 못했습니다.</td></tr>`;
    return;
  }
  dashPlaces = await response.json();
  dashRenderTable(dashPlaces);
}

if (dashSearchBtn) {
  dashSearchBtn.addEventListener("click", dashFetchPlaces);
}
if (dashKeywordInput) {
  dashKeywordInput.addEventListener("keydown", (event) => {
    if (event.key === "Enter") dashFetchPlaces();
  });
}
if (dashMediaTypeSelect) {
  dashMediaTypeSelect.addEventListener("change", dashFetchPlaces);
}
if (dashAdminBtn) {
  dashAdminBtn.addEventListener("click", openAdminDialog);
}
if (dashCancelBtn) {
  dashCancelBtn.addEventListener("click", dashResetForm);
}

if (dashSubmitBtn) {
  dashSubmitBtn.addEventListener("click", async () => {
    if (!currentUser || currentUser.role !== "ADMIN") {
      openAuthDialog("login");
      return;
    }

    const body = JSON.stringify({
      mediaType: dashAdminMediaType.value,
      workTitle: dashAdminWorkTitle.value,
      placeName: dashAdminPlaceName.value,
      region: dashAdminRegion.value || null,
      address: dashAdminAddress.value || null,
      latitude: Number(dashAdminLatitude.value),
      longitude: Number(dashAdminLongitude.value),
      description: null,
      animeImageUrl: null,
      realImageUrl: null
    });

    const editingId = dashPlaceIdInput.value;
    const url = editingId ? `/api/admin/places/${editingId}` : "/api/admin/places";
    const method = editingId ? "PUT" : "POST";

    const response = await fetch(url, {
      method,
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body
    });

    if (!response.ok) {
      return;
    }

    dashResetForm();
    await dashFetchPlaces();
  });
}

async function ensurePage2Loaded() {
  updateDashAuthUI();
  await dashFetchPlaces();
  page2Loaded = true;
}

document.addEventListener("pagechange", (event) => {
  if (event.detail && event.detail.page === 2) {
    ensurePage2Loaded();
  }
});

/* ---------- Init ---------- */

async function init() {
  initTheme();
  initMap();
  await fetchCurrentUser();
  updateDashAuthUI();
  await fetchFavorites();
  await fetchPlaces();
}

init();
