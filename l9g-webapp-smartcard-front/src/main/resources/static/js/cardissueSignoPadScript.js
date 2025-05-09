  const STPadServerLibDefault = STPadServerLib.STPadServerLibDefault;
  const STPadServerLibCommons = STPadServerLib.STPadServerLibCommons;
  const STPadServerLibApi = STPadServerLib.STPadServerLibApi;

  let storedSignatureData = null;
  let foundPads = [];
  let activePadIndex = null;
  let isSigningActive = false;
  let selectionStatus = {};

  function onConnectionOpen(event) { console.log("WebSocket geöffnet.", event); }
  function onConnectionClose(event) { console.log("WebSocket geschlossen.", event); }
  function onConnectionError(event) { console.error("WebSocket Fehler:", event); }

  function waitForOpenConnection(socket) {
    return new Promise((resolve, reject) => {
      let maxAttempts = 10, interval = 200, attempt = 0;
      const timer = setInterval(() => {
        if (socket.readyState === WebSocket.OPEN) {
          clearInterval(timer);
          resolve();
        }
        if (++attempt > maxAttempts) {
          clearInterval(timer);
          reject(new Error("WebSocket nicht rechtzeitig geöffnet."));
        }
      }, interval);
    });
  }

  async function startProcess() {
    try {
      STPadServerLibCommons.destroyConnection();
      STPadServerLibCommons.createConnection("wss://localhost:49494", onConnectionOpen, onConnectionClose, onConnectionError);
      const socket = STPadServerLibCommons.getSTPadServer();
      await waitForOpenConnection(socket);

      console.log("Suche nach Pads...");
      let params = new STPadServerLibDefault.Params.searchForPads();
      params.setPadSubset("HID");
      let result = await STPadServerLibDefault.searchForPads(params);
      foundPads = result.foundPads;

      if (foundPads.length === 0) {
        throw new Error("Kein Pad gefunden.");
      }
      activePadIndex = foundPads[0].index;
      console.log("Pad gefunden:", foundPads[0]);

      let openParams = new STPadServerLibDefault.Params.openPad(activePadIndex);
      await STPadServerLibDefault.openPad(openParams);
      console.log("Pad geöffnet.");

      let selectionParams = new STPadServerLibDefault.Params.startSelectionDialog();
      selectionParams.addCheckboxInformation([{ id: "confirm", text: "Hiermit bestätige ich den Erhalt der Ostfalia-Karte und erkläre mich ausdrücklich damit einverstanden, im Falle eines Verlustes oder einer Beschädigung der Karte eine Gebühr in Höhe von 20 € für die Ausstellung einer Ersatzkarte zu zahlen", checked: false, required: true }]);
      STPadServerLibDefault.handleSelectionChange = function(fieldId, checked){
        console.log("Auswahl wurde zu" + fieldId + "-->" + checked);
        selectionStatus[fieldId] = checked;      
      };
      await STPadServerLibDefault.startSelectionDialog(selectionParams);

    } catch (error) {
      console.error("Fehler im Signaturprozess:", error);
      await disconnectPad();
      throw error;
    }
  }

  STPadServerLibDefault.handleConfirmSelection = async function() {
    console.log("AGB bestätigt – Starte Unterschrift");
    try {
      let signatureParams = new STPadServerLibDefault.Params.startSignature();
      signatureParams.setFieldName("Bitte unterschreiben");
      signatureParams.setCustomText("Unterschreiben Sie unten auf dem Pad");
      await STPadServerLibDefault.startSignature(signatureParams);
    } catch (error) {
      console.error("Fehler bei startSignature:", error);
      await disconnectPad();
    }
  };

  STPadServerLibDefault.handleConfirmSignature = async function() {
    console.log("Unterschrift bestätigt.");
    try {
      let params = new STPadServerLibDefault.Params.getSignatureData();
      let value = await STPadServerLibDefault.getSignatureData(params);
      storedSignatureData = value.signData;
      console.log("Signaturdaten (Base64):", storedSignatureData);
      console.log("Checkbox:", selectionStatus)
      // Update des Popups: Jetzt soll der Button "Herausgeben" erscheinen.
      Swal.fire({
        title: "Unterschrift erfasst",
        text: "Möchten Sie die Karte herausgeben?",
        icon: "question",
        showCancelButton: true,
        cancelButtonText: "Abbrechen",
        confirmButtonText: "Herausgeben",
        allowOutsideClick: false,
      }).then((result) => {
        if (result.isConfirmed) {
          Swal.fire("Gespeichert!", "Karte erfolgreich herausgegeben.", "success");
        }
      });
    } catch (error) {
      console.error("Fehler bei getSignatureData:", error);
    } finally {
      await disconnectPad();
    }
  };

  STPadServerLibDefault.handleRetrySignature = async function() {
    console.log("Unterschrift wiederholen.");
    await STPadServerLibDefault.retrySignature();
  };

  STPadServerLibDefault.handleCancelSignature = STPadServerLibDefault.handleCancelSelection = async function() {
    console.log("Prozess abgebrochen.");
    await disconnectPad();
    Swal.fire("Abgebrochen", "Der Prozess wurde abgebrochen.", "error");
    location.reload();
  };

  async function disconnectPad() {
    if (activePadIndex !== null) {
      console.log("Verbindung zum Pad wird getrennt...");
      try {
        let closeParams = new STPadServerLibDefault.Params.closePad(activePadIndex);
        await STPadServerLibDefault.closePad(closeParams);
        console.log("Pad geschlossen.");
      } catch (error) {
        console.error("Fehler beim Schließen des Pads:", error);
      }
    }
    STPadServerLibCommons.destroyConnection();
    console.log("WebSocket-Verbindung getrennt.");
    activePadIndex = null;
  }

  STPadServerLibCommons.handleDisconnect = function(padIndex) {
    console.log("Pad " + padIndex + " wurde getrennt.");
    disconnectPad();
    location.reload();
  };

  // === Integration: Gesamter Prozess im SweetAlert-Popup ===
  async function startPadProcessWithPopup() {
    try {
      await Swal.fire({
        title: 'Suche nach Pad...',
        text: 'Bitte warten Sie, während wir nach einem Pad suchen.',
        allowOutsideClick: false,
        showCancelButton: true,
        cancelButtonText: 'Abbrechen',
        didOpen: async () => {
          await startProcess();
          // Nach AGB-Bestätigung wird das Popup aktualisiert
          Swal.update({
            title: 'Warten auf Unterschrift...',
            text: 'Bitte unterschreiben Sie auf dem Pad.',
            allowOutsideClick: false,
            showCancelButton: true,
            cancelButtonText: 'Abbrechen',
            showConfirmButton: false // Es wird zunächst kein "Unterschrift abgeschlossen"-Button angezeigt
          });
        },
        preConfirm: () => {         
          disconnectPad();
          return false;
        }
         }).then(() => {
        // Beim Schließen des Popups, wenn "Abbrechen" gedrückt wird, auch die Verbindung trennen
        disconnectPad();
      });
      
    } catch (error) {
      console.error("Fehler im Popup-Prozess:", error);
      await disconnectPad();
    }
  }