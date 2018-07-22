Come aumentare la quantità di memoria disponibile
=============================
DocFetcher ha un limite prefissato di memoria disponibile di 256&nbsp;MB. Esso viene impostato all'avvio del programma. Può venire aumentato giocando con i diversi programmi di avvio che vengono forniti con DocFetcher e che sono sistema operativo-specifici.

Windows
-------
La versione di DocFetcher per Microsoft Windows è dotata di svariati programmi di avvio tra loro alternativi e già pronti all'uso i quali utilizzano diverse dimensioni di *heap*. Per il loro uso si consiglia di seguire le indicazioni sottostanti.

* Aprire la cartella di DocFetcher. Se si sta usando la versione portatile di DocFetcher, si tratta della cartella in cui è stato spacchettato il contenuto del file zip scaricato. Se invece si sta usando la versione non portatile, la cartella DocFetcher è presente in `C:\Program Files` o in `C:\Program Files (x86)` o in una cartella avente nome simile.
* I programmi di avvio di DocFetcher sono presenti nella sotto-cartella `DocFetcher\misc` e hanno nome `DocFetcher-XXX.exe` (ove `XXX` rappresenta la dimensione dell'*heap* impostata nel rispettivo programma di avvio). Per esempio il programma di avvio `DocFetcher-512.exe` imposta una dimensione dell'*heap* di 512&nbsp;MB.
* Prima di usare uno qualsiasi di questi programmi di avvio **è necessario innanzitutto spostarlo o copiarlo nella cartella principale di DocFetcher**. Non è però necessario cancellare il programma di avvio predefinito o rinominare il programma di avvio alternativo.

Un altro modo per modificare il limite di memoria prefissato è copiare il file `misc\DocFetcher.bat` nella cartella principale di DocFetcher e modificare l'espressione `-Xmx256m` presente nell'ultima riga di tale file, per esempio cambiandola in `-Xmx512m`

Linux
-----
In questo caso bisogna aprire con un editor di testo lo script di avvio del programma (cioè `DocFetcher/DocFetcher.sh`) e modificare, secondo necessità, l'espressione `-Xmx256m` presente nell'ultima riga. Per esempio: `-Xmx512m`

Mac OS&nbsp;X
--------------------------------
Entrambe le versioni di DocFetcher (portatile e non-portatile) vengono lanciate tramite una *app* che viene fornita assieme al programma. Nella versione non portatile l'*app* "collegata" è quella che viene fornita assieme all'immagine-disco (*dmg*). Nella versione portatile invece, l'app collegata si trova nella cartella DocFetcher.

In entrambi i casi, l'*app* fornita a corredo è una cartella con estensione `.app`. In Finder dovrebbe essere disponibile una voce del menu contestuale per aprire questa cartella. Se la lingua di sistema del Mac OS&nbsp;X è l'inglese, questa voce di menu è `Show Package Contents`.

All'interno di tale cartella si trova anche lo script di avvio: `Contents/MacOS/DocFetcher`. Bisogna aprirlo con un editor di testo e modificare, secondo necessità, l'espressione `-Xmx256m` presente nell'ultima riga. Per esempio: `-Xmx512m`