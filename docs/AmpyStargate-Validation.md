# Verifica 0.1.2-alpha — 16 ottobre 2026

Sorgenti verificati: `07e85b2a6b33326dd9e5b35bab26e0a87156f130`.

Workflow: https://github.com/Frak89/AmpyStargate/actions/runs/38958589978

APK: `AmpyStargate-0.1.2-alpha-debug.apk`, versionCode 3, 6.232.852 byte.

SHA256: `636f5e7f74bd2bbac036ac3af877560314408876f16eef57b33ca2e4cd9fa633`.

## Correzione dello screenshot Restricted Setting

La pagina AmpyPermissions guida al consenso manuale: Informazioni app → ⋮ → Allow restricted settings / Consenti impostazioni con limitazioni, quindi Accessibilità → AmpyStargate, infine Avvia overlay. Non tenta di scrivere impostazioni protette o di concedersi il permesso. Non rileva in modo certo ogni criterio di restrizione: lo stato disabilitato viene presentato con una guida condizionale, non con una diagnosi inventata.

Riferimento ufficiale: https://support.google.com/android/answer/12623953

## Verifiche passate

- Test matematici, compilazione API 37 e Android Lint.
- Emulatore API 36: apertura della scheda Informazioni app del proprio package e delle impostazioni Accessibilità usando le attività risolte da Android.
- UI del selettore temi: scelta Fusion con segno di selezione; screenshot verificato con contorno SVG e testo dorati. I controlli ON e il tab corrente usano la stessa evidenza. Il colore selection è opzionale nella palette JSON.
- Stato iniziale disabilitato/inattivo, poi conferma esplicita del servizio nel launcher dopo Avvia overlay. Permesso, connessione e overlay sono stati separati; non viene dichiarato successo al solo invio del comando.
- Tocchi nativi attraverso overlay e reazioni ai click accessibili.
- Digitazione reale, maiuscole, spazio, tastierino, cancellazione, Invio multilinea, azione Fine, inserimento numerico, cambio tema senza cambiare PID e uscita alle impostazioni dalla tastiera in modalità sicura.
- Due blocchi consecutivi recuperati: prima overlay, poi launcher. Guardiano separato e controllo PID conservati.

AmpyDebug aggiunge AmpyAccess.json ai report di overlay e guardiano. Mostra stati e l'eventuale errore di avvio senza contenuto dello schermo o cronologia input. Il protocollo privato interprocesso distingue avvio richiesto da avvio confermato e impedisce richieste duplicate durante l'avvio.

## Limiti della prova

L'emulatore installa via ADB: la conferma nativa Allow restricted settings mostrata sul Pixel non è stata automatizzata né verificata sul telefono. È un'azione dell'utente in Android, non una funzione concessa dall'APK. Restano da verificare sul Pixel Android 17 la procedura manuale completa, i 120 Hz effettivi, Gboard, stabilità prolungata e selezione Home. Nessuna promessa di recupero da freeze globali Android/GPU/guardiano.

Gli APK debug delle build CI possono avere certificati diversi: conservare i propri temi e leggere le istruzioni prima di disinstallare. La firma di distribuzione stabile rimane da configurare.

---

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
