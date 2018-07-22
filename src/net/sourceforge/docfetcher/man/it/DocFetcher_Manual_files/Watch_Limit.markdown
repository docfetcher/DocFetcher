Come aumentare i limiti di sorveglianza delle cartelle (Linux)
===========================================

In Linux, i processi possono *sorvegliare* un massimo di 8192 cartelle. Questo limite potrebbe essere raggiunto se si indicizzassero delle cartelle fra loro altamente nidificate. Se questo limite viene superato, DocFetcher fa comparire il messaggio di errore: *No space left on device*. È possibile aggirare questo problema innalzando i limiti di sorveglianza delle cartelle. Per esempio il comando sotto riportato innalza temporaneamente tale limite a 32000 cartelle:

    sudo echo 32000 > /proc/sys/fs/inotify/max_user_watches

Per modificare in maniera definitiva il limite di sorveglianza delle cartelle, bisogna aprire (come utente `root`) il file `/etc/sysctl.conf`, aggiungervi la riga sotto riportata e salvare il file.

    fs.inotify.max_user_watches=32000