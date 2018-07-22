Introduzione
============

DocFetcher è una applicazione *open source* che consente di effettuare ricerche all'interno del proprio computer riguardanti i file e il loro contenuto. Si può pensare a questa applicazione come al motore di ricerca *Google* applicato ai propri file locali.

**Ricerca basata sugli indici**: poiché nella maggior parte dei casi effettuare una ricerca direttamente all'interno dei documenti stessi risulterebbe troppo lento, DocFetcher crea degli *indici delle cartelle* all'interno delle quali si vogliono effettuare ricerche. Essi consentono a DocFetcher di recuperare facilmente i file e il loro contenuto attraverso delle *parole-chiave*, in modo del tutto simile a quanto si fa quando si consulta l'indice presente nelle ultime pagine di un libro. La creazione di un indice potrebbe richiedere del tempo in funzione del numero e dalle dimensioni dei file che debbono essere indicizzati all'interno delle singole cartelle. In ogni caso questo processo va eseguito una sola volta per ciascuna cartella; poi si possono effettuare delle ricerche all'interno delle cartelle indicizzate quante volte si vuole.

**Creazione di un indice**: per creare un indice bisogna fare clic col pulsante destro del mouse nell'area `Ambito di ricerca` posta in basso a sinistra e selezionare `Crea Indice da > Cartella`. A questo punto bisogna scegliere la cartella da indicizzare. È opportuno che  i neofiti  inizino col selezionare una cartella contenente un numero non troppo elevato di file, per esempio una cinquantina circa. Dopo aver selezionato la cartella, compare una finestra al cui interno sono presenti i parametri di configurazione dell'indicizzazione. I valori predefiniti dovrebbero andare bene, nel qual caso è sufficiente fare clic sul pusante `Esegui` e aspettare che DocFetcher completi l'indicizzazione dei documenti. Un metodo alternativo per creare un indice è quello di incollare una cartella dagli Appunti nell'area `Ambito di ricerca`. Ciò funziona però solo per le cartelle normali, non per il file PST.

**Ricerca**: per effettuare una ricerca bisogna inserire una o più parole nel campo di testo posizionato sopra il pannello dei risultati (cioè la tabella avente le colonne con intestazioni) e premere il tasto `Invio` o fare clic col mouse sopra il pulsante `Cerca`. I risultati compariranno nell'omonimo pannello, ordinati per punteggio decrescente.

*Se si sta leggendo questa guida all'interno di DocFetcher e se si seguono le istruzioni presenti nel paragrafo successivo la guida scomparirà. Per ripristinarla, bisogna fare clic sul pulsante `'?'` in alto a destra. È anche possibile aprire la guida nel proprio browser predefinito facendo clic sul pulsante `Apri nel browser esterno` posto in alto a sinistra del presente pannello.*

**Pannello dei risultati e pannello di anteprima**: al di sotto del pannello dei risultati (o alla sua destra  &mdash; in funzione di come è organizzato il layout dell'interfaccia-utente), si trova il pannello di anteprima. Selezionando un file nel pannello dei risultati, in quello di anteprima verrà mostrata un'anteprima del contenuto del file in questione in formato testo. Caratteristiche degne di nota sono:

* ***Evidenziazione***: in modalità predefinita, i termini di ricerca utilizzati vengono evidenziati nel pannello di anteprima e si può saltare da un'occorrenza alla precedente o alla successiva usando semplicemente i pulsanti "Freccia Su" (occorrenza precedente) e "Freccia Giù" (occorrenza successiva).
* ***Browser web integrato***: per i file di tipo HTML è possibile passare dalla visualizzazione in modalità "solo testo" a quella all'interno del browser web integrato nel programma. Nota: Quest'ultima possibilità non è disponibile in alcune distribuzioni Linux.

**Scorciatoie utili**: premendo `Ctrl+F` oppure `Alt+F` si va direttamente al campo di ricerca. Per aprire un file in un programma esterno, fare un doppio clic sul nome del file nel pannello dei risultati.

**Ordinamento**: è possibile cambiare l'ordinamento dei risultati facendo clic sull'intestazione di una qualsiasi delle colonne presenti nel pannello dei risultati. Per esempio, per ordinare i risultati in base al nome del file, fare clic sull'intestazione `Nome del file`. Facendo clic per due volte sulla stessa intestazione, l'ordinamento sarà egualmente basato su tale colonna, ma l'ordinamento avrà verso opposto (per esempio A > Z diventerà Z > A). È anche possibile variare l'ordine delle colonne utilizzando la modalità "trascina e rilascia" (*drag 'n' drop*). Se, per esempio, si vuole che la colonna `Nome del file` sia la prima a sinistra, è sufficiente selezionare e trascinare la relativa intestazione fino a portarla a sinistra di tutte le altre.

**Filtri**:  nella parte sinistra dell'interfaccia-utente sono disponibili diversi sistemi per filtrare i risultati: (1) A livello di `Dimensione minima/massima dei file` è possibile specificare una dimensione minima e/o massima dei file ai quali si è interessati. (2) La voce `Tipi di documento` consente di filtrare i risultati per tipo di file. (3) Infine, deselezionando una o più elementi presenti nell'area `Ambito di ricerca`, è possibile filtrare i risultati anche in base a una o più cartelle indicizzate (e alle relative sottocartelle).

**Aggiornamento indici**: se vengono aggiunti, modificati o cancellati dei file all'interno delle cartelle indicizzate, bisogna aggiornare anche i relativi indici, altrimenti i risultati della ricerca potrebbero risultare errati. Fortunatamente l'aggiornamento di un indice è quasi sempre molto più rapido della sua creazione poiché debbono essere processate solo le modifiche che sono intervenute. Inoltre DocFetcher può aggiornare i propri indici automaticamente in due modi:

1. ***DocFetcher di per sé***: se DocFetcher è in esecuzione e la *sorveglianza delle cartelle* per eventuali modifiche a tale livello è abilitata, DocFetcher rileva le variazioni e aggiorna immediatamente i propri indici.
2. ***Servizio (demone) di DocFetcher***: se DocFetcher non è in esecuzione, le modifiche vengono registrate da un piccolo demone (servizio) che gira in background e gli indici interessati verranno aggiornati non appena DocFetcher tornerà in funzione. Nota: sfortunatamente il demone NON è attualmente disponibile per Mac OS&nbsp;X.

*Alcune avvertenze*: se si sta usando la versione portatile di DocFetcher e si vuole eseguire il demone, è necessario attivarlo manualmente aggiungendo il programma eseguibile del demone all'elenco dei programmi che vengono eseguiti all'avvio del sistema operativo. Né DocFetcher, né il demone sono in grado di riconoscere eventuali modifiche a livello di condivisioni di rete.  <!-- this line should end with two spaces -->  
Pertanto in quei casi in cui gli indici non possono essere aggiornati automaticamente, bisogna farlo manualmente. Bisogna selezionare uno o piú indici da aggiornare nell'area `Ambito di ricerca`. Poi si deve fare clic col pulsante destro del mouse e dal menu contestuale che compare va selezionato `Aggiorna Indice`. Alternativamente si può premere il tasto `F5`.

* * *

<a name="Advanced_Usage"></a> 

Utilizzo avanzato
==============

**Sintassi delle interrogazioni**: con DocFetcher, è possibile fare molto di più di una semplice ricerca di parole. Per esempio è possibile usare dei caratteri "jolly" (*wildcards*) per cercare delle parole aventi una radice comune come per esempio: `wiki*`. Per ricercare una frase (cioè una sequenza di parole in un ordine specifico) bisogna invece racchiudere tutti i termini entro doppie vigolette come per esempio nel caso di: `"the quick brown fox"`. Ma tutto ciò non è che l'inizio! Per una panoramica riguardante tutti i costrutti supportati, ci si riferisca alla sezione sulla [sintassi delle interrogazioni](DocFetcher_Manual_files/Query_Syntax.html).

**Preferenze**: nell'angolo in alto a destra dell'interfaccia-utente si trova un'icona che rappresenta due ruote dentate. Facendo clic su di essa si apre la finestra delle Preferenze. Per un utilizzo avanzato sono disponibili ulteriori impostazioni, accessibili facendo clic sul link "Impostazioni avanzate", presente in basso a sinistra nella finestra delle preferenze.

**Raccolte di documenti portatili**: la versione portatile di DocFetcher consente di disporre di un contenitore comprendente sia DocFetcher, sia i propri documenti con gli indici ad essi associati e questo insieme può essere liberamente spostato persino da un sistema operativo ad un altro &mdash; per esempio da Windows a Linux e viceversa. Una cosa importante da tenere a mente usando la versione portatile di DocFetcher è che gli indici debbono essere creati usando dei *percorsi relativi*. Fare clic [qui] (DocFetcher_Manual_files/Portable_Repositories.html) per ulteriori informazioni riguardanti le raccolte di documenti portatili. Fra l'altro, si noti che, a differenza delle versioni 1.0.3 e precedenti di DocFetcher, ora non è più richiesto l'inserimento dei documenti nella cartella di DocFetcher.

**Opzioni di configurazione dell'indicizzazione**: per una dettagliata discussione riguardante tutte le opzioni della finestra di configurazione dell'indicizzazione, fare clic [qui](DocFetcher_Manual_files/Indexing_Options.html). Si può raggiungere questa pagina della Guida anche a partire dalla finestra di configurazione stessa, facendo clic sul pulsante `Guida` posto nella parte inferiore sinistra di tale finestra. Forse le opzioni di configurazione più interessanti sono:

* ***Estensioni file***: le estensioni dei file di testo semplice e degli archivi zip sono completamente *personalizzabili*. Ciò risulta particolarmente utile per indicizzare i file di codice sorgente di programmi.
* ***Esclusione file***: utilizzando delle espressioni regolari è possibile escludere certi file dall'indicizzazione.
* ***Riconoscimento tipo di MIME***: senza il riconoscimento del tipo di MIME (*Multipurpose Internet Mail Extensions*), DocFetcher deve basarsi semplicemente sull'estensione del file (per esempio `'.doc'`) per decidere di che tipo di file si tratta. Con il riconoscimento del tipo di MIME, DocFetcher è invece in grado di sbirciare meglio dentro al contenuto dei file stessi per vedere se può trovare maggiori informazioni. Questa procedura è piú lenta rispetto al semplice controllo dell'estensione, ma è assai utile nel caso in cui i file abbiano un'estensione scorretta.
* ***Indicizzazione delle coppie HTML come documento unico***: DocFetcher tratta in maniera predefinita come un unico documento un file HTML e la relativa cartella associata (per esempio il file `foo.html` e la cartella `foo_files`). Lo scopo principale di ciò è di far scomparire dai risultati della ricerca tutta la confusione dovuta alla presenza di numerosi file presenti all'interno della cartella associata al file HTML.

**Espressioni regolari (Regular expressions, RegEx)**: sia l'esclusione dei file, sia il riconoscimento del tipo di MIME si basa sulle cosiddette *espressioni regolari*. Queste sono sequenze di caratteri (stringhe) che DocFetcher confronta con nomi di file o con percorsi per raggiungere dei file. Per esempio, per escludere tutti i file il cui nome inizia con la parola "journal", è possibile usare l'espressione regolare: `journal.*`. Si noti come ciò sia leggermente diverso dalla sintassi di interrogazione classica di DocFetcher dove sarebbe stato omesso il punto e si sarebbe scritto: `journal*`. Per saperne di più sulle espressioni regolari, si legga questa [breve introduzione](DocFetcher_Manual_files/Regular_Expressions.html).

**Notifica di nuove versioni**: DocFetcher non è stato pensato per effettuare il controllo automatico di sue eventuali nuove versioni. Se si desidera *veramente* essere avvisati della disponibilità di nuove versioni, è possibile farlo  [così](DocFetcher_Manual_files/Release_Notification.html).

* * *

<a name="Caveats"></a> 

Avvertenze e problemi comuni
==========================

**Aumento della quantità di memoria a disposizione**: DocFetcher, come tutti i i programmi Java, ha dei limiti nella quantità memoria che può usare e che è noto come *Java heap size*. Questo limite deve essere fissato in partenza e per DocFetcher è stato scelto un valore predefinito di 256&nbsp;MB. Se si vuole indicizzare un elevatissimo numero di file e/o se taluni file da indicizzare sono di dimensioni veramente enormi (il che non è infrequente nel caso di certi file PDF) è possibile che DocFetcher raggiunga o superi tale limite. Se ciò dovesse accadere, si potrebbe volere/dovere [aumentare la quantità di memoria a disposizione](DocFetcher_Manual_files/Memory_Limit.html).

**Non indicizzare le cartelle di sistema**: diversamente da altre applicazioni di ricerca, DocFetcher non è stato pensato per indicizzare le cartelle di sistema quali: `C:\` e `C:\Windows`. Fare ciò è sconsigliato per i seguenti motivi:

1. ***Rallentamento***: i file delle cartelle di sistema tendono a venir modificati molto frequentemente. Se  la *sorveglianza delle cartelle* è attiva, si ha un continuo aggiornamento degli indici di DocFetcher, il che determina un rallentamento del computer.
2. ***Problemi di memoria***: DocFetcher deve mantenere in memoria una rappresentazione dei file che deve essere la più ridotta possibile. A causa di ciò e poiché le cartelle di sistema generalmente contengono un gran numero di file, se queste vengono indicizzate, DocFetcher può andare con relativa facilità *out of memory*.
3. ***Spreco di risorse e peggiori risultati di ricerca***: oltre ai precedenti motivi tecnici, indicizzare le cartelle di sistema comporta uno spreco di tempo e di spazio disco e i risultati di una ricerca vengono inquinati da dei file di sistema che sono assolutamente inutili. Pertanto, per ottenere i migliori risultati nel più breve tempo possibile, conviene indicizzare solo ciò che effettivamente serve.

**Supporto Unicode**: DocFetcher supporta pienamente Unicode per tutti i formati di documento.. Nel caso di file di testo semplice, DocFetcher deve usare un [approccio euristico](http://www-archive.mozilla.org/projects/intl/UniversalCharsetDetection.html) per tentare di indovinare la codifica corretta, in quanto i file di testo semplice non possiedono alcuna informazione esplicita riguardante la codifica.

**Archivi supportati**:  DocFetcher attualmente supporta i seguenti formati di archivio: `zip` e formati derivati, `7z`, `rar` e l'intera famiglia `tar.*`. Inoltre sono supportati anche gli archivi eseguibili `zip` e `7z`, ma non gli archivi eseguibili di tipo `rar`. DocFetcher tratta questi archivi come fossero delle normali cartelle e può maneggiare anche eventuali nidificazioni di questi archivi aventi profondità arbitraria (per esempio un archivio `zip` che contiene un archivio`7z` che a sua volta contiene un archivio `rar`…).   
Ciò detto, si noti che il supporto per gli archivi `zip` e `7z` è il migliore in termini di robustezza e di velocità. D'altro canto, l'indicizzazione di file `tar.gz`, `tar.bz2` e di formati simili tende ad essere meno efficiente. Ciò dipende dal fatto che questi formati non contengono un "sommario" interno del contenuto dell'archivio, per cui DocFetcher è costretto a spacchettare l'intero archivio invece delle singole voci dell'archivio. In conclusione: se si può scegliere come comprimere i propri file, conviene usare i formati di archivio `zip` o `7z` per avere la massima compatibilità con DocFetcher.

**Il demone (servizio) di DocFetcher è innocente!**: se si sospetta che il demone (servizio) di DocFetcher sia responsabile del rallentamento del computer o che provochi dei *crash* di sistema, ci si sta probabilmente sbagliando! Infatti il demone è un programma semplicissimo che consuma scarsissima memoria, che usa pochissima CPU e che ha solo il compito di "sorvegliare" eventuali modifiche a carico delle cartelle già indicizzate. Se, nonostante tutto, non si fosse convinti di ciò, è pur sempre possibile rinominare i file eseguibili del demone in modo tale che essi non partano automaticamente o, ancora, si può provare ad usare la versione portatile di DocFetcher che in modalità predefinita ha tale demone disattivato.

* * *

<a name="Subpages"></a>

Argomenti specifici
===============
* [Sintassi delle interrogazioni](DocFetcher_Manual_files/Query_Syntax.html)
* [Raccolte di documenti portatili](DocFetcher_Manual_files/Portable_Repositories.html)
* [Opzioni di indicizzazione](DocFetcher_Manual_files/Indexing_Options.html)
* [Espressioni regolari (Regular expressions, RegEx)](DocFetcher_Manual_files/Regular_Expressions.html)
* [Notifica del rilascio di nuove versioni del programma](DocFetcher_Manual_files/Release_Notification.html)
* [Come aumentare la quantità di memoria disponibile](DocFetcher_Manual_files/Memory_Limit.html)
* [Come aumentare i limiti di sorveglianza delle cartelle (Linux)](DocFetcher_Manual_files/Watch_Limit.html)
* [Preferenze](DocFetcher_Manual_files/Preferences.html)

Ulteriori informazioni
===================
Per maggiori informazioni ci si può riferire al nostro [wiki](http://docfetcher.sourceforge.net/wiki/doku.php). Se vi fossero domande o problemi, è anche possibile visitare il nostro [forum](http://sourceforge.net/projects/docfetcher/forums/forum/702424). Eventuali bug possono essere segnalati sul nostro [bug tracker](http://sourceforge.net/tracker/?group_id=197779&atid=962834).