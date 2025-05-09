      function resetSalesbookFilters() {
        const form = document.querySelector('.htmx-filter-form');
        if (form) {
          form.querySelector('select[name="quarter"]').value = 'in_total';
          form.querySelector('select[name="pointOfSaleId"]').value = 'all';
          form.requestSubmit();
        }
      }