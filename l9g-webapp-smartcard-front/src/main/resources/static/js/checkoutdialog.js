    async function startPayment(totalAmount) {
    const { value: formValues } = await Swal.fire({
      title: 'Zahlung starten',
      html:
        `<div style="display: flex; flex-direction: column; align-items: center;">
          <label for="paymentType" style="margin-top: 10px; margin-bottom: 5px;">Zahlungsart:</label>
          <select id="paymentType"
                  style="width: 200px; padding: 6px; margin-bottom: 15px; border-radius: 5px; border: 1px solid #ccc;">
            <option value="BAR">Bar</option>
            <option value="KARTE">Karte</option>
          </select>

          <div id="givenWrapper" style="width: 100%; display: flex; flex-direction: column; align-items: center;">
            <label for="givenAmount" style="margin-bottom: 5px;">Gegeben (in €):</label>
            <input type="number"
                   id="givenAmount"
                   placeholder="z. B. 50.00"
                   style="width: 200px; padding: 6px; border-radius: 5px; border: 1px solid #ccc;">
          </div>
        </div>`,
      didOpen: () => {
        const paymentTypeEl = document.getElementById('paymentType');
        const givenAmountEl = document.getElementById('givenAmount');
        const givenWrapperEl = document.getElementById('givenWrapper');

        const handleChange = () => {
          if (paymentTypeEl.value === 'KARTE') {
            givenAmountEl.value = totalAmount.toFixed(2);
            givenWrapperEl.style.display = 'none'; // Verstecke das Eingabefeld
          } else {
            givenAmountEl.value = '';
            givenWrapperEl.style.display = 'flex'; // Zeige das Eingabefeld
          }
        };

        paymentTypeEl.addEventListener('change', handleChange);
        handleChange(); // Initialzustand setzen
      },
      preConfirm: () => {
        const paymentType = document.getElementById('paymentType').value;
        const givenAmount = parseFloat(document.getElementById('givenAmount').value);
        if (paymentType === 'BAR' && isNaN(givenAmount)) {
          Swal.showValidationMessage('Bitte gültigen Betrag eingeben');
        }
        return { paymentType, givenAmount };
      },
      showCancelButton: true,
      confirmButtonText: 'Weiter zur Berechnung',
      cancelButtonText: 'Abbrechen'
    });

    if (formValues) {
      const change = formValues.givenAmount - totalAmount;
      if (formValues.paymentType === 'BAR' && change < 0) {
        Swal.fire('Fehler', 'Gegebener Betrag reicht nicht aus!', 'error');
        return;
      }

      const confirmPayment = await Swal.fire({
        title: 'Wechselgeld:',
        html: `<p>Wechselgeld: <strong>${change.toFixed(2)} €</strong></p>`,
        icon: 'info',
        showCancelButton: true,
        confirmButtonText: 'Wechselgeld ausgegeben',
        cancelButtonText: 'Abbrechen'
      });

      if (confirmPayment.isConfirmed) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/posx/sales/checkout';

        form.innerHTML = `
          <input type="hidden" name="paymentType" value="${formValues.paymentType}">
          <input type="hidden" name="givenAmount" value="${formValues.givenAmount}">
        `;

        document.body.appendChild(form);
        form.submit();
      }
    }
  }