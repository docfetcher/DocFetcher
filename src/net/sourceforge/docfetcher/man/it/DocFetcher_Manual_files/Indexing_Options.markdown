L'idea di fondo: una coda di attività di indicizzazione
=====================================
La finestra di configurazione dell'indicizzazione è costituita di una o più schede, ciascuna delle quali rappresenta un indice da creare o da aggiornare. Tutte le schede assieme costituiscono la *coda di attività* che deve essere eseguita, le cui voci vengono processate una ad una. A questa coda è possibile aggiungere ulteriori attività utilizzando il pulsante `'+'` posto in alto a destra.

Ciascuna scheda presenta un pulsante `Esegui` posto in basso a destra. Facendo clic su di esso, si conferma che l'attività è stata correttamente configurata ed è pronta per l'indicizzazione che si avvia non appena c'è una attività pronta in coda.

A destra del pulsante `'+'` c'è un altro pulsante. Facendo clic su quest'ultimo, la finestra di configurazione di DocFetcher viene minimizzata sulla barra delle applicazioni consentendo di effettuare ricerche sugli indici già creati mentre se ne stanno creando di nuovi in background.

È possibile annullare qualsiasi attività facendo clic sul pulsante di chiusura (`'X'`) di ciascuna scheda. Se l'attività viene annullata, viene comunque data la possibilità di mantenere o di eliminare l'indice creato fino a quel momento. In tal modo è anche possibile arrestare l'indicizzazione e riprenderla in un secondo tempo, a partire dal punto in cui era giunta. La ripresa consiste semplicemente nell'aggiornamento dell'indice parziale, tramite il comando `Aggiorna Indice` dal menu contestuale presente nell'area `Ambito di ricerca`.

La finestra di configurazione possiede anch'essa un pulsante di chiusura (in Windows è il pulsante con la `'X'` in alto a destra). Facendo clic su di esso tutte le attività di indicizzazione in corso vengono annullate e rimosse dalla coda.

Opzioni di indicizzazione
================
Questa sezione si focalizza sulle opzioni di indicizzazione disponibili per gli indici di cartella e di archivio.

Estensioni file
---------------
Il controllo delle *Estensioni dei file* consente di specificare quali file debbano essere trattati come file di testo semplice e quali come archivi zip. Uno scenario di comune riscontro è quello in cui DocFetcher deve indicizzare certi tipi di file aventi differente estensione ma che rappresentano tutti del codice sorgente e che pertanto sono dei file di testo semplice. Si notino i due pulsanti "`...`" presenti a destra dei campi di testo. Facendo clic su di essi, DocFetcher si può spostare tra le cartelle da indicizzare raccogliendo tutte le estensioni dei file in un elenco dal quale vanno scelte quelle che debbono essere associate ai file di testo semplice e agli archivi zip.

Esclusione file / Riconoscimento tipo di MIME
--------------------------------
Aggiungendo elementi alla tabella, è possibile: (1) escludere dall'indicizzazione certi file e (2) abilitare per taluni file il riconoscimento del tipo di MIME. Tutto ciò si basa sulle cosiddette espressioni regolari (Regular expressions, RegEx). Se non le si si sapesse usare, è opportuno leggere la sezione di introduzione alle [espressioni regolari](Regular_Expressions.html).

Ecco come funzionano le tabelle: ciascun elemento della tabella è una espressione regolare cui è associata una certa azione. L'espressione regolare viene confrontata con i nomi dei file o con il percorso assoluto per raggiungere il/i file, mentre l'azione può essere l'*esclusione* o il *riconoscimento del tipo di MIME* del/i file. Durante l'indicizzazione, quando un file corrisponde ad una espressione regolare presente nella tabella, gli viene applicata l'azione ad essa associata.

È possibile aggiungere o togliere elementi dalla tabella utilizzando i pulsanti `'+'` e `'-'` posti a destra. I pulsanti "Su" e "Giù" invece consentono di aumentare o ridurre la *priorità* degli elementi selezionati nella tabella. La priorità acquista importanza quando un file corrisponde a più di una espressione regolare nella tabella; in quel caso l'espressione regolare avente maggiore priorità *vince* e tutte le altre vengono ignorate.

Al di sotto della tabella si trova uno strumento di aiuto per l'uso delle espressioni regolari. Facendo clic sul pulsante `'...'` posto a destra è possibile selezionare qualcuno dei file presenti nella cartella che deve essere indicizzata, mentre ll percorso per raggiungerlo viene mostrato nel campo di testo. La riga posizionata sopra al campo di testo segnala all'utilizzatore se l'espressione regolare correntemente selezionata nella tabella corrisponde o no al file selezionato.

Varie
--------------------------------
Opzione | Commento
-------|--------
Indicizzazione delle coppie HTML come documento unico | Permette di scegliere se un file HTML e la relativa cartella associata (per esempio il file `pippo.html` e la relativa cartella `pippo_files`) devono essere indicizzati come un unico documento o no.
Riconoscimento degli archivi zip e 7z eseguibili (rallenta il processo) | Se questa funzione è abilitata, DocFetcher controlla *ciascun* file con estensione `exe` per verificare se si tratta di di un archivio zip o 7z eseguibile.
Indicizzazione del nome dei file anche se è impossibile indicizzarne il contenuto | Se questa opzione è abilitata, DocFetcher includerà *tutti* i file nel suo indice, indipendentemente dal fatto che il loro contenuto possa essere indicizzato. Questa opzione deve essere abilitata per poter effettuare una ricerca completa sul nome dei file. Si noti però che DocFetcher potrebbe necessitare di una notevole quantità di memoria, in funzione del numero di file presenti nella cartella indicizzata. Qualora si andasse *out of memory* sarebbe necessario [aumentare la quantità di memoria disponibile](Memory_Limit.html).
Memorizzazione di percorsi relativi se possibile (favorisce la portatilità) | Questa impostazione è importante se si usa la versione portatile di DocFetcher. Per saperne di più si legga questa sezione riguardante le [raccolte di documenti portatili](Portable_Repositories.html).
Sorveglianza sulle cartelle per eventuali modifiche ai file | Se questa impostazione è attiva, DocFetcher dovrebbe essere in grado di riconoscere eventuali modifiche a carico delle cartelle indicizzate aggiornando di conseguenza i propri indici. Questa impostazione non ha effetti sul servizio (demone) di DocFetcher.