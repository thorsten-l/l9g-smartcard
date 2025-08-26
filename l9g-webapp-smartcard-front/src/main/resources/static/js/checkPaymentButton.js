document.addEventListener('DOMContentLoaded', function () {
  const kasseButton = document.getElementById('kasse-button');
  const mailInput = document.getElementById('mail');
  
  const customerInfoSelect = $('#customerInfo'); 

  if (!kasseButton || !mailInput || customerInfoSelect.length === 0) {
    console.error('Ein oder mehrere benötigte Elemente (Kasse-Button, Mail-Input, Customer-Select) wurden nicht gefunden.');
    return;
  }

  function updateButtonState() {

    setTimeout(function() {
      if (mailInput.value.trim() !== '') {
        kasseButton.classList.remove('disabled');
        kasseButton.setAttribute('hx-get', '/posx/sales');
      } else {
        kasseButton.classList.add('disabled');
        kasseButton.removeAttribute('hx-get');
      }
    }, 50);
  }


  customerInfoSelect.on('select2:select', updateButtonState);
  

  customerInfoSelect.on('select2:clear', updateButtonState);
  

  mailInput.addEventListener('input', updateButtonState);


  updateButtonState();
});