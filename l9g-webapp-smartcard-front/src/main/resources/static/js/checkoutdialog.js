    async function startPayment(totalAmount) {
    const { value: formValues } = await Swal.fire({
      title: translation.startPaymentTitle,
      html:
        `<div style="display: flex; flex-direction: column; align-items: center;">
          <label for="paymentType" style="margin-top: 10px; margin-bottom: 5px;">${translation.paymentTypeLabel}</label>
          <select id="paymentType"
                  style="width: 200px; padding: 6px; margin-bottom: 15px; border-radius: 5px; border: 1px solid #ccc;">
            <option value="BAR">${translation.paymentCash}</option>
            <option value="KARTE">${translation.paymentCard}</option>
          </select>

          <div id="givenWrapper" style="width: 100%; display: flex; flex-direction: column; align-items: center;">
            <label for="givenAmount" style="margin-bottom: 5px;">${translation.givenLabel}</label>
            <input type="number"
                   id="givenAmount"
                   placeholder="z. B. 50.00"
                   style="width: 200px; padding: 6px; border-radius: 5px; border: 1px solid #ccc;">
          </div>
           <div style="width: 100%; display: flex; flex-direction: column; 
              align-items: center;">
               <label for="purpose" style="margin-bottom: 5px;">${translation.purpose}</label>
                     <input type="text" id="purpose" placeholder=
              "${translation.purpose}" style="width: 200px; padding: 6px; 
              border-radius: 5px; border: 1px solid #ccc;">
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
        const purpose = document.getElementById('purpose').value;
        if (paymentType === 'BAR' && isNaN(givenAmount)) {
          Swal.showValidationMessage(translation.errorInvalidAmount);
        }
        return { paymentType, givenAmount, purpose };
      },
      showCancelButton: true,
      confirmButtonText: translation.continuePay,
      cancelButtonText: translation.cancel
    });

    if (formValues) {
      const totalCents = Math.round(totalAmount * 100);
      const givenCents = Math.round(parseFloat(formValues.givenAmount) * 100);
      const change = (givenCents - totalCents) / 100;
      if (formValues.paymentType === 'BAR' && change < 0) {
        Swal.fire(translation.errorTitle, translation.errorNotEnough, 'error');
        return;
      }

      const confirmPayment = await Swal.fire({
        title: translation.changeTitle,
        html: `<p>${translation.changeTitle}: <strong>${change.toFixed(2)} €</strong></p>`,
        icon: 'info',
        showCancelButton: true,
        confirmButtonText: translation.changeConfirm,
        cancelButtonText: translation.cancel
      });

      if (confirmPayment.isConfirmed) {
        const csrfToken = document.querySelector('input[name="_csrf"]').value;
        const csrfParam = document.querySelector('input[name="_csrf"]').getAttribute("name");
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = '/posx/sales/checkout';

        form.innerHTML = `
          <input type="hidden" name="paymentType" value="${formValues.paymentType}">
          <input type="hidden" name="givenAmount" value="${formValues.givenAmount}">
          <input type="hidden" name="change" value="${change.toFixed(2)}">
          <input type="hidden" name="purpose" value="${formValues.purpose}">
          <input type="hidden" name="${csrfParam}" value="${csrfToken}">
        `;

        document.body.appendChild(form);
        form.submit();
        
      }
    }
  }