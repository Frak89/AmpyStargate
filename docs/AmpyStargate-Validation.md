# Verifica 0.1.1-alpha — 16 ottobre 2026

Sorgenti verificati: `13ed31c90301509d32958cb1d335ce370bad0ad3`.

Workflow completata: https://github.com/Frak89/AmpyStargate/actions/runs/38948421674

APK debug: `AmpyStargate-0.1.1-alpha-debug.apk`, versionCode 2, 6.216.992 byte.

SHA256 APK: `f2962dc05609719a4001be674a76ba2d5796a6e01fe8202d4df31c44b7def395`.

## Esiti della 0.1.1

- Matematica di geometria/easing/watchdog, compilazione target API 37 e Android Lint: passati.
- Emulatore Android 16/API 36: digitazione reale con AmpyKeyboard in un normale EditText. Verificati lettere, maiuscola, spazio, tastierino simboli/numeri, cancellazione e ritorno a capo multilinea. Verificata anche l'azione Fine senza inserire una nuova riga.
- Inserimento numerico: verificato con il valore sintetico 42.
- Cambio Fusion → Classic alla riapertura della tastiera: verificato mantenendo lo stesso PID dell'IME.
- Stato sicuro sintetico: visualizzazione dei soli comandi nativi e apertura delle impostazioni IME confermate. Il renderer non carica il tema personalizzato in questo stato.
- Tocchi nativi attraverso overlay attivo e reazioni ai click accessibili: passati.
- Blocco del processo overlay, sua terminazione e launcher ancora utilizzabile: passati.
- Secondo blocco, questa volta del launcher: recuperato dal guardiano ancora attivo.

Le correzioni includono aggiornamento del tema su ogni apertura, rispetto di IME_FLAG_NO_ENTER_ACTION, margine per la barra dei gesti e controlli di uscita nativi dopo il recupero.

Il fixture nativo appartiene solo alla variante debug, non salva testo, usa un task separato e riceve esclusivamente dati sintetici. Le letture UI automatizzate includono la finestra IME e precedono l'abilitazione dell'accessibilità; durante la prova overlay si riutilizzano coordinate già raccolte, evitando di disconnettere il servizio con la strumentazione di test. L'APK non esegue il provisioning dell'emulatore sul telefono.

## Limiti ancora aperti

Non sono verificati sul Pixel 7 Pro: 120 Hz/fps effettivi, carico e stabilità prolungati, permessi manuali, scelta Home e ritorno al launcher di riserva, Gboard e tastiere OEM, tutte le lingue/emoji, azioni di ogni editor e app esterna. AmpyKeyboard è ancora una tastiera base senza predizione o dizionario. Nessun recupero garantito dai blocchi globali di Android/GPU.

La firma debug di questa distribuzione differisce da quella dell'APK 0.1.0 precedente: leggere la guida prima di rimuovere l'installazione precedente e conservare prima i file personali. Una firma di distribuzione stabile resta da predisporre.

---

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
