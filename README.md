# AmpyStargate 0.1.0-alpha

Launcher Android con grafica organica, overlay reattivo decorativo e tastiera Ampy opzionale. Target Android 17, Pixel 7 Pro; minimo Android 11. Nessun root. Non è una ROM e non sostituisce System UI o il codice delle altre app.

## Primo avvio

Apri AmpyStargate come una normale app. **Launcher predefinito** apre la scelta Android: non cambia il launcher senza conferma. Il drawer mostra le app avviabili dell'utente corrente; non implementa ancora spazio privato, profili lavoro o widget Android.

L'overlay è inizialmente spento. Leggi la descrizione, abilita il servizio nelle impostazioni Accessibilità e torna in AmpyStargate per premere **Avvia overlay**. Il servizio decorativo è dichiarato non assistivo. Non richiede esplorazione al tocco, cattura schermo o controllo dei gesti. Effetti e tema si possono cambiare; un comando Stop rimane nella notifica del guardiano.

## Cosa fa davvero

- Decora il rettangolo di click/focus quando l'app lo espone all'accessibilità. Non legge il testo della vista, dei campi o dei tasti.
- Non cattura schermi a riposo: si basa sui metadati delle finestre. Non ricostruisce arbitrariamente i pixel dell'app.
- Disegna un overlay di accessibilità non toccabile e non focalizzabile: il tocco originale va alla finestra nativa. Nessuna iniezione o replica dei tocchi.
- Segue i rettangoli IME esposti dal sistema con una transizione interpolata. Non garantisce la stessa curva temporale di Gboard, né tutti i singoli tasti di una tastiera esterna.
- La tastiera **AmpyKeyboard**, se selezionata esplicitamente, può animare esattamente i propri tasti e si muove con la finestra IME Android. È una tastiera base sperimentale, non Gboard rivestita.
- Le schermate bloccate, i dati sensibili, le finestre di sistema e i componenti senza nodi accessibili vengono esclusi o non decorati. Non si aggirano restrizioni Android.

## Recupero

AmpyGuard gira in un processo separato e riceve heartbeat dai thread UI monitorati. Se un processo Ampy registrato smette di rispondere per almeno 6 secondi, disabilita l'overlay, registra il motivo, termina solo quel processo Ampy se ancora appartenente all'app e pubblica una notifica di recupero. Il recupero offre le impostazioni Home e Accessibilità. I PID vengono verificati prima di ogni terminazione, per non colpire un PID riutilizzato.

Non può garantire il recupero da un blocco dell'intero Android, di System UI, del driver GPU o dello stesso guardiano; non può forzare la chiusura di altre app, né scegliere Pixel Launcher senza la conferma dell'utente. L'overlay non si riattiva da solo dopo una perdita di connessione o un crash. Non viene installata un'app amministratore dispositivo.

## Grafica e moduli

`AmpyThemes/` contiene quattro temi SVG con palette JSON: Classic, Cyberpunk, Futuristic e Fusion. I file sono copiati nella cartella esterna dell'app `AmpyStargate/AmpyThemes` e possono essere importati tramite la scelta file nelle impostazioni del launcher. I percorsi organici supportano un sottoinsieme documentato di SVG: M/C/Z assoluti, un contorno chiuso. Le icone native delle app Android restano quelle delle app; logo e forme di sistema Ampy sono vettoriali.

`AmpyOverlayService`: eventi e lifecycle. `AmpyOverlayView`: rendering. `AmpyGuardService` e `AmpyGuardClient`: recupero e IPC. `AmpyKeyboardService`: IME. `AmpyTheme`: palette/SVG. `AmpyMath`: matematica indipendente da Android. `Core/FunctionDefinitions.json` documenta API e versioni. Vedi [specifiche permanenti](docs/AmpyStargate-Specifiche.md) per continuare in un'altra chat.

## Compilazione

Apri la cartella come progetto Gradle in Android Studio con JDK 17 e SDK Android 17/API 37. È richiesta una distribuzione Gradle 8.13 (non è incluso il wrapper binario). Da terminale: `gradle :app:assembleDebug :app:lintDebug`. La workflow GitHub compila e pubblica l'APK debug; non viene dichiarata riuscita finché non è eseguita. Le firme debug locali e CI possono differire: non disinstallare versioni precedenti senza esportare i dati.

L'accessibilità reale, l'ordine sopra le tastiere OEM, la latenza degli eventi, i 120 Hz e il recupero sotto carico devono essere verificati sul Pixel. Il target 120 Hz è un suggerimento al sistema, non una misura degli fps.
