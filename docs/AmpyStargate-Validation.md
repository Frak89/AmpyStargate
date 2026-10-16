# Verifica 0.1.0-alpha — 16 ottobre 2026

Sorgenti applicativi verificati: `39eeae4d232808504e91707fb414d2bb0136927f`.

Workflow completata: https://github.com/Frak89/AmpyStargate/actions/runs/38891435935

## Esiti

- Test matematici su geometria, easing e scadenza del watchdog: passati.
- Compilazione target Android 17/API 37 e Android Lint: passati, senza disattivare i controlli.
- Emulatore Android 16/API 36, con APK eseguito come normale app: passaggio dei tocchi attraverso l'overlay confermato da tre click sul pulsante nativo; servizio mantenuto attivo durante i click.
- Reazioni agli eventi di click accessibili: confermate dal contatore del servizio.
- Registrazione e selezione dell'IME AmpyKeyboard: confermate; digitazione completa non verificata da questa prova.
- Processo overlay bloccato dall'esterno soltanto sull'emulatore: terminato dal guardiano dell'app, modalità sicura registrata e launcher ancora utilizzabile.
- Secondo blocco del processo launcher: recuperato, a conferma che il primo recupero conserva il monitoraggio dei processi sani.

Il provisioning di accessibilità, notifiche e il segnale SIGSTOP appartengono soltanto al test su emulatore. L'APK non si concede permessi, non richiede root e termina solo i processi della propria applicazione dopo averne verificato l'identità. Il test non promette un recupero da blocchi globali di Android o del driver GPU.

## Da provare sul Pixel 7 Pro

Concessione manuale dei permessi, scelta del launcher predefinito e ritorno a Pixel Launcher; comportamento reale di riavvio Home dopo un problema; tastiera completa e cambio IME; reazioni delle app esterne, specialmente quelle con controlli virtuali; movimento e ordine di sovrapposizione sopra Gboard; latenza, stabilità prolungata, accessibilità e frequenza effettiva del display. La richiesta 120 Hz non costituisce una misurazione di 120 fps.

La skin a riposo è una ricostruzione dei rettangoli accessibili, non uno skinning pixel-perfect di tutte le app. Nessuna cattura dei pixel o lettura del testo viene utilizzata. I screenshot della prova sono soltanto quelli dell'emulatore senza dati personali.
