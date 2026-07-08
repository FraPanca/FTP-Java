# FTP-Java

## Italiano

Un semplice File Transfer Protocol client/server in Java, con trasferimento massivo di file e directory su TCP.

**Stack:** Java · Socket TCP (`Socket`, `ServerSocket`) · Programmazione multithread

### Descrizione

Implementazione di un protocollo di trasferimento file client/server su TCP, ispirato ai comandi `mget`/`mput` degli storici client FTP. Da console il client può scaricare (`mget`) o caricare (`mput`) intere directory, con la possibilità di specificare una dimensione minima dei file da trasferire ed evitando di ritrasferire file già presenti a destinazione.

### Come si esegue

Compilazione dalla cartella principale del repository:
```
javac -cp . server/Server.java client/Client.java
```

Esecuzione:
```
java -cp . server.Server serverPort
java -cp . client.Client serverIpAddress serverPort
```

Il client va eseguito dalla cartella radice del repository, dato che legge/scrive nelle sottocartelle `client/` e `server/` in modo relativo. Una volta connesso, da console è possibile digitare:
- `mget` — scarica una o più directory dal server (chiede il nome delle directory e, opzionalmente, la dimensione minima in byte dei file da scaricare)
- `mput` — carica una o più directory locali sul server (stessa sintassi di `mget`)
- `end` — termina la sessione

### Funzionalità principali

- Trasferimento file bidirezionale (`mget` per lo scaricamento, `mput` per il caricamento) su un'unica connessione TCP persistente, con sessione interattiva a comandi
- Trasferimento massivo: è possibile indicare più directory in un solo comando
- Filtro dimensionale opzionale: specificando una soglia in byte, i file più piccoli vengono esclusi dal trasferimento
- I file già presenti a destinazione vengono automaticamente saltati (risposta `salta`) invece di essere ritrasmessi
- Creazione automatica delle directory di destinazione mancanti
- Server multithread: ogni client connesso viene gestito da un thread dedicato (`ServerSocketThreads`), con timeout di socket di 20 secondi
- Verifica dell'integrità del trasferimento confrontando la lunghezza dichiarata del file con i byte effettivamente ricevuti, rimuovendo il file in caso di discrepanza

### Struttura del progetto

```
FTP-Java/
├── client/
│   ├── Client.java     # Interfaccia a riga di comando: invia/riceve file dal server
│   └── dir2/            # Directory di esempio con file da caricare (mput)
├── server/
│   ├── Server.java      # Avvio del server e gestione delle connessioni (ServerSocketThreads)
│   └── dir1/            # Directory di esempio con file da scaricare (mget)
```

### Note

Progetto didattico: il protocollo applicativo è un formato testuale/binario custom sopra TCP, senza autenticazione né cifratura del traffico. Non pensato per un uso in produzione.

### Licenza

MIT

---

## English

A simple client/server File Transfer Protocol in Java, supporting bulk transfer of files and directories over TCP.

**Stack:** Java · TCP sockets (`Socket`, `ServerSocket`) · Multithreaded programming

### Description

Implementation of a client/server file-transfer protocol over TCP, inspired by the classic `mget`/`mput` FTP commands. From the console, the client can download (`mget`) or upload (`mput`) entire directories, optionally filtering by a minimum file size, and skipping files that already exist at the destination.

### How to run

Compile from the repository root:
```
javac -cp . server/Server.java client/Client.java
```

Run:
```
java -cp . server.Server serverPort
java -cp . client.Client serverIpAddress serverPort
```

The client must be run from the repository root, since it reads/writes to the `client/` and `server/` subfolders using relative paths. Once connected, the console accepts:
- `mget` — download one or more directories from the server (prompts for directory names and, optionally, a minimum file size in bytes)
- `mput` — upload one or more local directories to the server (same syntax as `mget`)
- `end` — terminate the session

### Key features

- Bidirectional file transfer (`mget` to download, `mput` to upload) over a single persistent TCP connection, with an interactive command-based session
- Bulk transfer: multiple directories can be specified in a single command
- Optional size filter: by specifying a byte threshold, smaller files are excluded from the transfer
- Files already present at the destination are automatically skipped (`salta` response) instead of being re-sent
- Automatic creation of missing destination directories
- Multithreaded server: each connected client is handled by a dedicated thread (`ServerSocketThreads`), with a 20-second socket timeout
- Transfer integrity check by comparing the declared file length against the bytes actually received, deleting the file on mismatch

### Project structure

```
FTP-Java/
├── client/
│   ├── Client.java     # Command-line interface: sends/receives files to/from the server
│   └── dir2/            # Sample directory with files to upload (mput)
├── server/
│   ├── Server.java      # Server startup and per-connection handling (ServerSocketThreads)
│   └── dir1/            # Sample directory with files to download (mget)
```

### Notes

Educational project: the application-level protocol is a custom text/binary format over TCP, with no authentication or traffic encryption. Not intended for production use.

### License

MIT
