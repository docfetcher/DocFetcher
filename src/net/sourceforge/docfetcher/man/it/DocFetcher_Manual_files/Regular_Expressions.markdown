Espressioni regolari (Regular Expressions, RegEx)
===================
Questa sezione serve a fornire una breve introduzione alle espressioni regolari. Non è di certo esaustiva, poiché le espressioni regolari rappresentano una tipologia ben definita e a se stante del linguaggio delle corrispondenze. Qualora si voglia approfondire l'argomento, è comunque possibile trovare tonnellate di informazioni in internet; è sufficiente cercare "tutorial espressioni regolari" o "introduzione alle espressioni regolari" o qualcosa di simile.

Corrispondenza con tutti i file `.*\.xlsx?` di Microsoft Excel
-----------------------------------------------
Nelle espressioni regolari (spesso abbreviate in *RegEx*), certi caratteri hanno un significato speciale. Per esempio il ***punto*** (`'.'`) sta esattamente al posto di *un solo carattere*, qualunque esso sia. Pertanto è possibile, per esempio, usare la regex `p.zzo` per trovare corrispondenza con `pazzo` o con `pezzo`, ma anche con `pizzo` o `p8zzo`.

Un altro carattere speciale è l'***asterisco*** (`'*'`) che sta per "il carattere precedente, ripetuto zero o più volte". Pertanto se si scrive la regex `ciao*`, vi corrispondono le seguenti stringhe: `cia`, `ciao`, `ciaoo`, `ciaooo` e così via.

Come conseguenza di queste regole, se il punto e l'asterisco vengono messi assieme, essi corrispondono ad una sequenza di caratteri arbitraria. Per esempio la regex `gen.*ione` corrisponde a: `genione`, `generalizzazione`, `generazione`, `gentrificazione` e così via.

Un carattere speciale, simile all'asterisco è il ***punto di domanda*** (`'?'`), che significa "il carattere precedente preso esattamente zero o una volta". È anche possibile riformulare tale affermazione in questi termini: "il carattere precedente può esserci o non esserci". Come l'asterisco, il punto di domanda può essere combinato con un punto. Pertanto la regex `pizz.?` può corrispondere a: `pizz`, `pizza`, `pizze`, `pizz4`, ecc...

Poiché caratteri come il punto e l'asterisco hanno un significato speciale, se si vuole una corrispondenza *letterale* con questi caratteri è necessario eseguire quello che tecnicamente viene definito come fornire loro un *escape*. Dal punto di vista pratico bisogna cioè far precedere questi caratteri speciali da un altro carattere speciale, la ***barra rovesciata*** o ***backslash*** (`'\'`). Un caso tipico in cui è necessario usare il backslash è qualora si voglia una corrispondenza esatta con il punto presente all'interno di un nome di file. Per esempio per cercare tutti i file che corrispondono al nome del file `license.txt`, bisogna usare la regex `license\.txt` e non la semplice stringa `license.txt` &mdash; quest'ultima infatti avrebbe come corrispondenza anche nomi di file quali, per esempio, `license-txt`.

Mettendo tutto assieme, è pertanto possibile scrivere una regex cui corrispondono tutti i tipi di file Microsoft Excel e cioé: `.*\.xlsx?`; questa regex sostanzialmente corrisponde a: "una sequenza arbitraria di caratteri (`.*`) seguita da un punto vero (`\.`), seguita dalla stringa *xls* e avente una (`x?`) finale opzionale.

Ricerca di corrispondenza con una sequenza di cifre: `journal\d+\.doc`
------------------------------------------------
Si supponga ora di voler cercare una corrispondenza con tutti i file Microsoft Word che iniziano con la stringa "journal" e che finiscono con una data, per esempio: "journal2007.doc". Inoltre, *non* si vogliano trovare corrispondenze con file aventi nome simile quali, per esempio, "journalism.doc".

Una regex come `journal.*\.doc` in questo caso non funziona perché vi corrisponderebbe anche "journalism.doc". Il primo passaggio per risolvere il problema è quello di rimpiazzare il punto o con '[0-9]' o con `\d`, il cui significato, in entambi i casi, è di creare una corrispondenza esatta con ***una cifra***. L'espressione `[0-9]` è una notazione più generale rispetto a `\d`</code>, perché è possibile scrivere, per esempio, `[4-6]` intendendo farvi corrispondere solo le cifre `4`, `5` e `6`. Nota Bene: Questa tipologia (l'inclusione fra parentesi quadre) funziona anche per le lettere. Per esempio: `[m-p]` corrisponde a tutte le lettere minuscole comprese fra la "m" e la "p" (estremi inclusi).

Combinando `\d` con un asterisco, è possibile scrivere l'espressione regolare `journal\d*\.doc`, cui corrisponde per esempio il nome del file "journal2007.doc", ma non "journalism.doc". Ma ciò non è completamente corretto. Si ricordi infatti che l'asterisco significa *il carattere precedente ripetuto zero o più volte*. In questo caso però non si vuole che vi siano *zero* cifre dopo "journal"; se ne vuole *almeno una* &mdash; altrimenti la regex avrebbe come corrispondenza anche il file "journal.doc".

Ecco allora intervenire un altro carattere speciale: il ***più*** il cui simbolo (`'+'`) sta per *il carattere precedente, ripetuto una o più volte*. Pertanto la versione finale della nostra espressione regolare sarà: `journal\d+\.doc`