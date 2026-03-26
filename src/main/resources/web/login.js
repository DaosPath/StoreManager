const LOGIN_PROMPT = "__storemanager_login__";

const form = document.getElementById("loginForm");
const usernameInput = document.getElementById("username");
const passwordInput = document.getElementById("password");
const rememberInput = document.getElementById("rememberMe");
const feedback = document.getElementById("feedback");
const submitButton = document.getElementById("submitButton");
const submitLabel = document.querySelector(".login-submit-label");
const demoButtons = Array.from(document.querySelectorAll("[data-user][data-password]"));
const inputShells = Array.from(document.querySelectorAll(".login-input-shell"));
const REMEMBER_KEY = "storemanager.login.remembered";

function initializeLogin() {
  restoreRememberedCredentials();
  setBusy(false);
  clearFeedback();
  syncFilledState();
  usernameInput.focus();
}

function setBusy(isBusy) {
  usernameInput.disabled = isBusy;
  passwordInput.disabled = isBusy;
  submitButton.disabled = isBusy;
  demoButtons.forEach((button) => {
    button.disabled = isBusy;
  });
  submitLabel.textContent = isBusy ? "Validando acceso..." : "Entrar al sistema";
}

function showFeedback(type, message) {
  feedback.hidden = false;
  feedback.className = `login-feedback ${type}`;
  feedback.textContent = message;
}

function clearFeedback() {
  feedback.hidden = true;
  feedback.className = "login-feedback";
  feedback.textContent = "";
}

function fillCredentials(username, password) {
  usernameInput.value = username;
  passwordInput.value = password;
  syncFilledState();
  clearFeedback();
}

function submitLogin(event) {
  event.preventDefault();

  const username = usernameInput.value.trim();
  const password = passwordInput.value;

  if (!username || !password) {
    showFeedback("error", "Completa usuario y contrasena.");
    return;
  }

  clearFeedback();
  persistRememberedCredentials(username, password);
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
  showFeedback("error", message || "No se pudo iniciar sesion.");
}

function persistRememberedCredentials(username, password) {
  try {
    if (!rememberInput || !rememberInput.checked) {
      window.localStorage.removeItem(REMEMBER_KEY);
      return;
    }

    window.localStorage.setItem(
      REMEMBER_KEY,
      JSON.stringify({
        username,
        password,
        remember: true
      })
    );
  } catch (error) {
    // Ignore storage issues inside the embedded WebView.
  }
}

function restoreRememberedCredentials() {
  if (!rememberInput) {
    return;
  }

  try {
    const raw = window.localStorage.getItem(REMEMBER_KEY);
    if (!raw) {
      rememberInput.checked = false;
      return;
    }

    const saved = JSON.parse(raw);
    usernameInput.value = saved.username || "";
    passwordInput.value = saved.password || "";
    rememberInput.checked = Boolean(saved.remember && saved.username && saved.password);
  } catch (error) {
    rememberInput.checked = false;
  }
}

function activateDemoCredentials(button) {
  if (!button || button.disabled) {
    return;
  }
  fillCredentials(button.dataset.user, button.dataset.password);
}

function syncFilledState() {
  inputShells.forEach((shell) => {
    const input = shell.querySelector("input");
    const hasValue = input && input.value.trim().length > 0;
    shell.classList.toggle("is-filled", hasValue);
  });
}

form.addEventListener("submit", submitLogin);
demoButtons.forEach((button) => {
  button.addEventListener("pointerdown", (event) => {
    event.preventDefault();
    activateDemoCredentials(button);
  });
  button.addEventListener("click", (event) => {
    event.preventDefault();
    activateDemoCredentials(button);
  });
});
usernameInput.addEventListener("input", syncFilledState);
passwordInput.addEventListener("input", syncFilledState);
if (rememberInput) {
  rememberInput.addEventListener("change", () => {
    if (!rememberInput.checked) {
      persistRememberedCredentials("", "");
    }
  });
}

window.initializeLogin = initializeLogin;
window.finishLogin = finishLogin;
