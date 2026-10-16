# Memoria del progetto AmpyStargate — 16 ottobre 2026

Repository richiesta dall'utente: Frak89/AmpyStargate. Progetto distinto da AmpybiOS; non modificare AmpybiOS per introdurre di nascosto un launcher globale. Riferimento di continuità per un'altra chat: leggere questo file insieme al README e al codice corrente prima di continuare.

Davide vuole AmpyStargate come launcher tematico globale sul Google Pixel 7 Pro, Android 17. UI organica/aliena, SVG modificabili, temi Classic/Cyberpunk/Futuristic/Fusion, palette indipendenti, morph, glitch, scan lines, profondità e transizioni. Nomi dei componenti caratteristici con prefisso Ampy. Preferisce grafica elaborata anche se costa CPU/GPU. Nessun root.

Richiesta del 16/10: se non si può modificare tutta Android, osservare lo schermo anche a riposo e sovrapporre grafica alle app e alla tastiera lasciando i tocchi alla UI nativa. Sincronizzare l'overlay con entrata/uscita della tastiera, dimensioni degli elementi e pressione dei tasti. In caso di freeze spegnere gli effetti e proporre il launcher Android normale.

Confine tecnico accettabile: launcher pienamente personalizzato, overlay di accessibilità non toccabile e facoltativo con reazioni agli eventi/limiti esposti. Non promettere intercettazione di ogni tocco, ricostruzione pixel di tutte le app, sincronizzazione perfetta con Gboard o alterazione di System UI. I permessi vengono attivati solo dall'utente. Non usare isAccessibilityTool=true per eludere filtri sui dati sensibili. Non raccogliere testo, password, screenshot o sequenze di tasti. Nessun permesso Internet nell'APK.

Per animare ogni proprio tasto in modo preciso, predisporre AmpyKeyboard come IME separata opzionale; Gboard deve restare selezionabile. Gli effetti su Gboard esterna sono soltanto quelli ricavabili dagli eventi accessibili, senza leggere i caratteri digitati. MediaProjection non è incluso: se in futuro utile richiederà consenso esplicito, gestione lifecycle e rispetto delle superfici protette; non abilitarlo implicitamente.

Recupero richiesto: overlay, launcher e guardiano isolati in processi separati; heartbeat sul thread UI effettivo, arresto del solo processo Ampy bloccato, notifica con Stop e apertura delle impostazioni Home per scegliere il launcher normale. Mai promettere recupero da freeze globale del telefono, force-stop di Android/altre app, o cambio automatico del launcher senza conferma.

Stato iniziale: prima implementazione sorgente 0.1.0-alpha con workflow di compilazione. Annotare separatamente verifiche realmente eseguite, APK generati e limiti; non trattare la presenza dei sorgenti come prova di funzionalità sul Pixel. Le informazioni persistono in questi file: non promettere un trasferimento automatico della conversazione o una memoria personale globale.
