(function () {
  const HX_URL = '/posx/sales';

  function setButtonEnabled(btn, enabled) {
    if (!btn) return;

    if (enabled) {
      btn.classList.remove('disabled'); // optisch
      btn.removeAttribute('aria-disabled');
      btn.setAttribute('hx-get', HX_URL);
      // HTMX neu binden
      if (window.htmx) {
        htmx.process(btn);
      }
    } else {
      btn.classList.add('disabled');
      btn.setAttribute('aria-disabled', 'true');
      btn.removeAttribute('hx-get');
    }
  }

  function computeEnabled(mailEl) {
    return !!(mailEl && mailEl.value && mailEl.value.trim() !== '');
  }

  function updateButtonState() {
    const btn = document.getElementById('kasse-button');
    const mailEl = document.getElementById('mail');
    setButtonEnabled(btn, computeEnabled(mailEl));
  }

  document.addEventListener('click', function (e) {
    const btn = e.target.closest('#kasse-button');
    if (!btn) return;

    if (!btn.hasAttribute('hx-get')) {
      e.preventDefault();
      e.stopPropagation();
    }
  });

  if (window.jQuery) {
    const $doc = jQuery(document);
    $doc.on('select2:select select2:clear', '#customerInfo', function () {
      setTimeout(updateButtonState, 0);
    });
  }

  document.addEventListener('htmx:afterSwap', updateButtonState);
  document.addEventListener('htmx:afterSettle', updateButtonState);

  let lastVal = null;
  setInterval(function () {
    const mailEl = document.getElementById('mail');
    const current = mailEl ? mailEl.value : '';
    if (current !== lastVal) {
      lastVal = current;
      updateButtonState();
    }
  }, 200);

  document.addEventListener('DOMContentLoaded', updateButtonState);
})();
