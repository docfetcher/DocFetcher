Raccolte di documenti portatili
==============================

Modalità d'uso semplice
-----------
 La versione portatile di DocFetcher consente di portare con sé (e di ridistribuire) una raccolta di documenti completamente indicizzata e totatmente ricercabile. Qualora non si disponga della versione portatile la si può scaricare [qui](http://docfetcher.sourceforge.net).

La versione portatile non richiede alcuna procedura di installazione; è sufficiente estrarre il contenuto dell'archivio zip in una cartella a scelta. Successivamente si può eseguire DocFetcher attraverso il programma eseguibile specifico per il proprio sistema operativo: `DocFetcher.exe` (Windows), `DocFetcher.sh` (Linux) o l'app `DocFetcher` (Mac OS&nbsp;X). Il solo requisito è che sul computer sul quale si esegue DocFetcher sia contestualmente installata la versione *runtime* di Java 1.6 (o superiori).

<u>Percorsi (path) relativi</u>: un elemento importante cui prestare attenzione per creare una raccolta di documenti portatili è che gli indici siano creati con l'opzione *Memorizzazione di percorsi relativi se possibile (favorisce la portatilità)* attivata. Senza di ciò, DocFetcher registrerà dei riferimenti *assoluti* ai file, per cui sarà sì possibile spostare in giro DocFetcher e i suoi indici, ma non i file &mdash; senza perderne, quanto meno, il riferimento. Ecco un esempio che illustra la differenza fra percorso relativo e assoluto:

* Percorso relativo: `..\..\i-miei-file\qualche-documento.txt`
* Percorso assoluto: `C:\i-miei-file\qualche-documento.txt`

Il percorso (o *path*) relativo dice sostanzialmente a DocFetcher che questi può trovare il file `qualche-documento.txt` spostandosi di 2 livelli a monte rispetto alla sua attuale posizione ed entrando successivamente nella cartella `i-miei-file`. Il percorso assoluto d'altro canto è invece un riferimento assoluto, indipendente dalla posizione di DocFetcher, per cui non è possibile spostare il file `qualche-documento.txt` (cfr. raccolte di documenti portatili) senza perderne anche i riferimenti. In sostanza DocFetcher non è più in grado di trovare dove è localizzato il file in questione.

Si noti che DocFetcher può solamente *tentare* di memorizzare i percorsi relativi. Ovviamente non è possibile farlo se DocFetcher e i file da indicizzare sono memorizzati in volumi differenti, per esempio DocFetcher in `D:\DocFetcher` e i file in `E:\i-miei-file`.

Trucchi per un uso produttivo di DocFetcher
--------------

* ***Archiviazione su CD-ROM***: mettendo DocFetcher su CD-ROM, ovviamente non sarà possibile salvare eventuali modifiche alle preferenze o agli indici. Pertanto è necessario ricordarsi di configurare esattamente il programma secondo le proprie esigenze prima di creare il CD-ROM. Ricordarsi anche che potrebbe essere necessario includere una versione *runtime* di Java qualora questa non fosse installata nel computer su cui si intende usare il CD-ROM.
* ***Differenti titoli per il programma***: per la ridistribuzione di una raccolta di documenti portatili o per consentire di lavorare con instanze multiple di DocFetcher senza che vi sia troppa confusione, è possibile fare in modo che ciascuna istanza, nella propria finestra, abbia titolo diverso. Per fare ciò bisogna fare clic sulla voce `Impostazioni avanzate` presente nella finestra delle *Preferenze* e modificare l'impostazione `Nome dell'applicazione`.

Avvertenze
--------

* ***Non toccare la cartella `indexes`***: è possibile, ma non è obbligatorio, mettere i propri file nella stessa cartella di DocFetcher. Se lo si fa, bisogna comunque ricordarsi di lasciare la sotto-cartella `indexes` a se stante, perché qualsiasi cosa venisse messa dentro a tale cartella potrebbe essere cancellata!
*  ***Incompatibilità dei nomi di file***: attenzione alla incompatibilità dei caratteri presenti nei nomi di file fra i diversi sistemi operativi. Per esempio in Linux i nomi dei file possono contenere caratteri quali ":" (due punti) o "|" (pipe); in Windows no. Conseguentemente si possono spostare delle raccolta di documenti da Linux a Windows o viceversa solo se i documenti di cui sono composte hanno nomi di file privi di caratteri incompatibili fra sistemi operativi. Per quanto riguarda invece caratteri particolari quali le *umlauts* tedesche o le lettere accentate italiane e francesi si tratta di un discorso completamente differente…