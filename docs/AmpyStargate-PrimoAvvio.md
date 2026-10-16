# Primo avvio AmpyStargate 0.1.1-alpha

1. Installa l'APK e aprilo come una normale app, lasciando inizialmente Pixel Launcher come Home.
2. In Settings autorizza le notifiche di recupero. La notifica AmpyGuard offre Stop e accesso alla schermata di recupero.
3. Leggi la descrizione del servizio e abilitalo nelle impostazioni Accessibilità Android. Se Android limita le impostazioni di un APK installato manualmente, gestisci quella conferma dalla scheda dell'app: Ampy non la aggira.
4. Torna in AmpyStargate e premi Avvia overlay. Prova Test tocco nativo: il contatore deve aumentare anche con la grafica sovrapposta. Puoi scegliere Morph, Glitch, Scan lines, skin a riposo e temi SVG.
5. Dopo questa prova, Launcher predefinito apre la scelta Home Android. Scegli AmpyStargate solo quando vuoi usarlo come Home principale.
6. AmpyKeyboard è facoltativa: abilitala e selezionala dalle impostazioni Android solo se vuoi provarla. È una IME propria di base; non è Gboard modificata. Il pulsante 🌐 permette di cambiare tastiera.

Quando cambi tema, chiudi e riapri AmpyKeyboard per applicarlo anche alla tastiera già in esecuzione. Non occorre terminarne il processo. In modalità di recupero sono disponibili soltanto i pulsanti nativi Cambia tastiera e Impostazioni: gli SVG personalizzati non vengono caricati.

Invio rispetta il tipo di campo: va a capo nei campi multilinea che richiedono il tasto di ritorno, mentre invia l'azione dell'editor nei campi configurati con Fine, Cerca o analoghe azioni. La tastiera rimane sperimentale: non include ancora predizione, dizionario o tutti i caratteri accentati.

## Aggiornamento da un APK debug precedente

Questi APK sono build di sviluppo. I runner CI possono generarli con certificati debug diversi: Android potrebbe rifiutare l'aggiornamento della versione già installata. Prima di qualsiasi disinstallazione conserva i tuoi SVG e le palette originali e salva il report AmpyDebug. Non viene fornita ancora una firma di distribuzione stabile; la disinstallazione cancella i dati dell'app.

Il certificato dell'APK 0.1.1 distribuito con questa verifica è stato confrontato con quello dell'APK 0.1.0 precedente: sono diversi. Questi due pacchetti non consentono quindi un aggiornamento diretto mantenendo l'installazione esistente.

## Se qualcosa si blocca

Il guardiano può terminare il processo Ampy non responsivo dopo almeno sei secondi. Usa la notifica di recupero per aprire le impostazioni Home e scegliere Pixel Launcher. Dopo un problema, un nuovo avvio presenta una schermata semplice: Riprova usa grafica di fabbrica e conserva gli SVG personalizzati; gli overlay non si riattivano da soli.

Un blocco globale del sistema o del driver GPU può impedire anche al guardiano di funzionare: il recupero automatico non è garantito in quel caso. Ampy non forza la chiusura di altre app e non cambia il launcher senza conferma.

## File e continuità

Le palette JSON e le forme SVG sono importabili da Settings. I temi copiati nell'area dell'app rimangono separati dal codice. Esporta report AmpyDebug crea un file testo da allegare per la diagnosi, senza cronologia di digitazione.

Per continuare in un'altra chat indica la repository `Frak89/AmpyStargate` e chiedi di leggere `AGENTS.md`, `docs/AmpyStargate-Specifiche.md` e `docs/AmpyStargate-Validation.md`. Questi file conservano le specifiche; non viene trasferita automaticamente l'intera conversazione.
