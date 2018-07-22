Sintassi delle interrogazioni
============
Questa pagina fornisce una panoramica della sintassi disponibile per le interrogazioni a partire da quelle più semplici fino a quelle avanzate. La sintassi delle interrogazioni proviene da Apache Lucene, il motore di ricerca che sta alla base di DocFetcher e che è descritto in maniera più tecnica sul sito di Lucene alla pagina [sintassi delle interrogazioni](http://lucene.apache.org/java/3_4_0/queryparsersyntax.html).

Operatori booleani
-----------------
DocFetcher supporta gli operatori booleani `OR`, `AND` e `NOT`. Se le parole sono concatenate *senza* operatori booleani, DocFetcher le tratta come se fossero concatenate mediante l'operatore `OR`.  Se però ciò non soddisfa l'utilizzatore di DocFetcher, questi può andare nelle [preferenze](Preferences.html) e impostare l'operatore `AND` come predefinito.

Invece di `OR`, `AND` e `NOT`, è anche possibile usare, rispettivamente `||`, `&&` e `"-"` (segno meno). Per raggruppare certe espressioni si possono usare anche le *parentesi*. Ecco alcuni esempi:

Interrogazione | I documenti risultanti contengono…
-------------------------|---------------------------------------------
`cane OR gatto` | `cane` o `gatto` (o entrambi)
`cane AND gatto` | `cane` e `gatto` (entrambi)
`cane gatto` | In modalità predefinita è equivalente all'interrogazione `cane OR gatto`
`cane NOT gatto` | `cane`, ma non `gatto`
`-cane gatto` | `gatto`, ma non `cane`
`(cane OR gatto) AND topo` | `topo` e o `cane` o `gatto` (o entrambi)


Ricerca indipendente dall'uso delle maiuscole o delle minuscole
-----------------------------
DocFetcher non distingue fra caratteri maiuscoli e minuscoli, pertanto non ha importanza se le parole da ricercare sono scritte tutte in minuscolo o tutte in maiuscolo o se ne sono una mescolanza. Le sole eccezioni sono le parole-chiave `OR`, `AND`, `NOT` e `TO` che debbono essere sempre inserite in MAIUSCOLO. Nota: per quanto riguarda la parola-chiave `TO` si veda la sottostante sezione "Ambito di ricerca".`


Ricerca di frasi e di termini specifici
----------------------------------
Per cercare una frase (cioè una sequenza ordinata di parole), bisogna inserire tale sequenza fra doppie virgolette. Per indicare che i documenti da ricercare debbono contenere una particolare parola, bisogna farla precedere dal segno `'+'`. Naturalmente è possibile combinare questi costrutti con operatori booleani e parentesi. Alcuni esempi:

Interrogazione | I documenti risultanti contengono…
----------------------|-------------------------------------
`"cane gatto topo"` | le parole `cane`, `gatto` e `topo`, in questo specifico ordine
`+cane gatto` | sicuramente `cane` e forse anche `gatto`
`"cane gatto" AND topo` | la frase (= precisa sequenza di parole) `cane gatto` e, inoltre, la parola `topo`
`+cane +gatto` | Equivalente all'interrogazione `cane AND gatto`


Caratteri "jolly"
---------
Per indicare che taluni caratteri sono sconosciuti o che possono assumere valori differenti, si possono usare il punto di domanda (`'?'`) e l'asterisco (`'*'`). Il punto di domanda sostituisce *esattamente* un carattere sconosciuto, mentre l'asterisco sostituisce un numero variabile di caratteri sconosciuti (da zero a 'n'). Per esempio:

Interrogazione | I documenti risultanti contengono…
-------------|-------------------------------------
`luc?` | `lucy`, `luca`...
`luc*` | `luc`, `lucy`, `luce`, `lucene`...
`*ene*` | `lucene`, `energia`, `generatore`...

Nota: se un carattere jolly viene usato come primo carattere di una parola, la ricerca tende ad essere più lenta. Ciò è dovuto a come è strutturato l'indice: è come se si cercasse di trovare il numero di telefono di una persona conoscendone solo il nome e non il cognome. Pertanto, nell'esempio sopra riportato, la ricerca di `*ene*` sarà probabilmente più lenta di altre ricerche perché `*ene*` inizia con un carattere jolly.


Ricerche per analogia ("Fuzzy Searches")
--------------
Le ricerche per analogia consentono di cercare parole *analoghe* ad una parola data. Per esempio, se si cerca la parola `vist~`, DocFetcher troverà quei documenti che contengono parole quali `vista`, `lista` e `pista`.

È anche possibile inserire una "soglia di analogia" che ha un valore compreso fra 0 e 1. Per esempio: `vist~0.8`. Più alto è il valore della soglia, più elevata deve essere anche l'analogia con le parole che vengono proposte nei risultati della ricerca. Se non si imposta alcuna soglia, viene assunto il valore soglia predefinito che è pari a 0.5.


Ricerche per prossimità
------------------
Le ricerche per prossimità consentono di trovare delle parole che sono situate ad una specifica distanza fra di loro. Per eseguire una ricerca di prossimità bisogna inserire una tilde ('~') alla fine della frase, seguita da un valore di distanza. &mdash; Si noti che ciò risulta sintatticamente simile ad una ricerca per analogia. Per esempio, per ricercare quei documenti che contengono i termini `wikipedia` e `lucene` ad una distanza di non più di 10 parole l'uno dall'altro, bisogna digitare: `"wikipedia lucene"~10`.


Fattore di rafforzamento
--------------
È possibile influenzare la rilevanza nell'ordinamento dei risultati assegnando alle parole dei "pesi" personalizzati. Per esempio: se si scrive: `cane^4 gatto` invece di `cane gatto`, i documenti che contengono la parola `cane` riceveranno un punteggio più alto e si porteranno tra i documenti che sono in testa alla classifica dei risultati.

Sebbene il fattore di rafforzamento debba essere necessariamente rappresentato da un valore positivo, esso può anche essere inferiore ad 1 (per esempio 0.2). Se non si usa alcun fattore di rafforzamento, esso assume il suo valore predefinito, cioè 1.


Ricerca in campi specifici
--------------
DocFetcher, in modalità predefinita, ricerca tutti i dati testuali che è in grado di estrarre, cioè il contenuto, il nome del file e i metadati di ciascun documento. È anche possibile restringere le ricerche al solo nome del file e/o a certi campi di metadati. Per esempio, per cercare i documenti i cui titoli contengono il termine `wikipedia`, bisogna scrivere: `title:wikipedia`. La ricerca in campi specifici può essere combinata con la ricerca di frasi, per esempio: `title:"cane gatto"` o con l'uso di parentesi, per esempio: `title:(cane gatto)`. Se infatti si omettono le doppie virgolette e le parentesi solo il termine `cane` verrà confrontato con il contenuto del titolo e non il termine `gatto`.

Quali siano i campi specifici disponibili dipende generalmente dal formato del documento. In ogni caso si possono seguire queste linee-guida:

<!-- Do not translate the following field names (filename, title, etc.) -->
* *File*: filename, title, author
* *Email*:  subject, sender, recipients


Ambito di ricerca
--------------
DocFetcher consente la ricerca di parole che siano lessicograficamente comprese *tra* due altre parole. Per esempio, la parola `beta` è compresa fra `alfa` e `gamma`. Pertanto se si vuole l'elenco dei documenti che contengono le parole comprese tra `alfa` e `gamma`, si deve scrivere: `[alfa TO gamma]`.

Quando si usano le parentesi quadre, l'ambito di ricerca è *inclusivo*, cioè i termini `alfa` e `gamma` sono compresi nei risultati della ricerca. Per definire un ambito di ricerca che non comprenda gli estremi (ambito di ricerca *esclusivo*), bisogna invece usare le parentesi graffe: `{alfa TO gamma}`.

Si possono anche combinare ambiti di ricerca e campi di ricerca come segue: `title:{alfa TO gamma}` e ciò restringe l'ambito di ricerca solo al campo "title".