const LOGIN_PROMPT = "__storemanager_login__";

const form = document.getElementById("loginForm");
const usernameInput = document.getElementById("username");
const passwordInput = document.getElementById("password");
const feedback = document.getElementById("feedback");
const submitButton = document.getElementById("submitButton");
const demoButtons = Array.from(document.querySelectorAll("[data-user][data-password]"));

function initializeLogin() {
  setBusy(false);
  clearFeedback();
  usernameInput.focus();
}

function setBusy(isBusy) {
  usernameInput.disabled = isBusy;
  passwordInput.disabled = isBusy;
  submitButton.disabled = isBusy;
  demoButtons.forEach((button) => {
    button.disabled = isBusy;
  });
  submitButton.textContent = isBusy ? "Validando acceso..." : "Entrar al sistema";
}

function showFeedback(type, message) {
  feedback.hidden = false;
  feedback.className = `feedback ${type}`;
  feedback.textContent = message;
}

function clearFeedback() {
  feedback.hidden = true;
  feedback.className = "feedback";
  feedback.textContent = "";
}

function fillCredentials(username, password) {
  usernameInput.value = username;
  passwordInput.value = password;
  clearFeedback();
  passwordInput.focus();
  passwordInput.select();
}

function submitLogin(event) {
  event.preventDefault();

  const username = usernameInput.value.trim();
  const password = passwordInput.value;

  if (!username || !password) {
    showFeedback("error", "Completa usuario y contraseña.");
    return;
  }

  clearFeedback();
  setBusy(true);

  try {
    const payload = `${encodeURIComponent(username)}|${encodeURIComponent(password)}`;
    window.prompt(LOGIN_PROMPT, payload);
  } catch (error) {
    setBusy(false);
    showFeedback("error", "No se pudo conectar con el acceso.");
  }
}

function finishLogin(success, message) {
  setBusy(false);
  if (success) {
    showFeedback("success", message || "Acceso concedido.");
    return;
  }
  showFeedback("error", message || "No se pudo iniciar sesión.");
}

form.addEventListener("submit", submitLogin);
demoButtons.forEach((button) => {
  button.addEventListener("click", () => {
    fillCredentials(button.dataset.user, button.dataset.password);
  });
});

window.initializeLogin = initializeLogin;
window.finishLogin = finishLogin;
