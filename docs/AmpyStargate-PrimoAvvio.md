# Primo avvio AmpyStargate 0.1.2-alpha

1. Installa l'APK e aprilo come una normale app, lasciando inizialmente Pixel Launcher come Home.
2. In Settings autorizza le notifiche di recupero. La notifica AmpyGuard offre Stop e accesso alla schermata di recupero.
3. Apri Permessi e configurazione (AmpyPermissions). Se Accessibilità mostra Controlled by Restricted Setting: premi 1 · Apri informazioni app, tocca ⋮ in alto → Allow restricted settings / Consenti impostazioni con limitazioni e conferma in Android. Poi premi 2 · Apri Accessibilità, scegli AmpyStargate e abilita il servizio. Il passaggio è manuale; l'APK non si concede il permesso.
4. Torna in AmpyStargate e premi Avvia overlay. Lo stato deve mostrare servizio connesso e overlay ATTIVO: la sola autorizzazione non avvia gli effetti. Prova Test tocco nativo: il contatore deve aumentare anche con la grafica sovrapposta. Puoi scegliere Morph, Glitch, Scan lines, skin a riposo e temi SVG.
5. Dopo questa prova, Launcher predefinito apre la scelta Home Android. Scegli AmpyStargate solo quando vuoi usarlo come Home principale.
6. AmpyKeyboard è facoltativa: abilitala e selezionala dalle impostazioni Android solo se vuoi provarla. È una IME propria di base; non è Gboard modificata. Il pulsante 🌐 permette di cambiare tastiera.

Quando cambi tema, chiudi e riapri AmpyKeyboard per applicarlo anche alla tastiera già in esecuzione. Non occorre terminarne il processo. In modalità di recupero sono disponibili soltanto i pulsanti nativi Cambia tastiera e Impostazioni: gli SVG personalizzati non vengono caricati.

Invio rispetta il tipo di campo: va a capo nei campi multilinea che richiedono il tasto di ritorno, mentre invia l'azione dell'editor nei campi configurati con Fine, Cerca o analoghe azioni. La tastiera rimane sperimentale: non include ancora predizione, dizionario o tutti i caratteri accentati.

## Aggiornamento da un APK debug precedente

Questi APK sono build di sviluppo. I runner CI possono generarli con certificati debug diversi: Android potrebbe rifiutare l'aggiornamento della versione già installata. Prima di qualsiasi disinstallazione conserva i tuoi SVG e le palette originali e salva il report AmpyDebug. Non viene fornita ancora una firma di distribuzione stabile; la disinstallazione cancella i dati dell'app.

Le distribuzioni precedenti 0.1.0 e 0.1.1 avevano certificati diversi. Prima di installare un nuovo APK debug considera la possibile differenza di firma: se Android rifiuta l'aggiornamento, conserva i tuoi file e scegli il launcher Android di riserva prima di rimuovere la vecchia app.

## Se qualcosa si blocca

Il guardiano può terminare il processo Ampy non responsivo dopo almeno sei secondi. Usa la notifica di recupero per aprire le impostazioni Home e scegliere Pixel Launcher. Dopo un problema, un nuovo avvio presenta una schermata semplice: Riprova usa grafica di fabbrica e conserva gli SVG personalizzati; gli overlay non si riattivano da soli.

Un blocco globale del sistema o del driver GPU può impedire anche al guardiano di funzionare: il recupero automatico non è garantito in quel caso. Ampy non forza la chiusura di altre app e non cambia il launcher senza conferma.

## File e continuità

Le palette JSON e le forme SVG sono importabili da Settings. I temi copiati nell'area dell'app rimangono separati dal codice. Esporta report AmpyDebug crea un file testo da allegare per la diagnosi, senza cronologia di digitazione.

Per continuare in un'altra chat indica la repository `Frak89/AmpyStargate` e chiedi di leggere `AGENTS.md`, `docs/AmpyStargate-Specifiche.md` e `docs/AmpyStargate-Validation.md`. Questi file conservano le specifiche; non viene trasferita automaticamente l'intera conversazione.

## Se il servizio non risponde

AmpyPermissions distingue tre stati: accessibilità abilitata da Android; servizio che risponde effettivamente; overlay attivo confermato dal servizio. Un messaggio di richiesta non significa avvio riuscito. Se non arriva conferma, ricontrolla il servizio in Android e consulta il report AmpyDebug, che include AmpyAccess.json, AmpyOverlay.json e AmpyGuard.json, senza testo digitato o schermate. Dopo un recupero gli effetti rimangono spenti fino a un nuovo avvio esplicito.

Le selezioni di tema ed effetto e i controlli ON usano un contorno SVG neon dorato. Il colore è personalizzabile aggiungendo selection (ad esempio #FFD166) alla palette JSON, mantenendo i cinque campi obbligatori.

Procedura Android ufficiale: https://support.google.com/android/answer/12623953

La firma dell’APK 0.1.2 qui distribuito è stata confrontata con quella della 0.1.1: è diversa. Per il solo sblocco Restricted Setting non serve reinstallare: puoi eseguire i passaggi anche nell’installazione attuale.
