# FTP-Java

## Italiano
Un File Transfer Protocol sviluppato in Java.

### Componenti
- Client: permette di effettuare operazioni di mget/mput
- Server: permette di effettuare operazioni di mget/mput

### Istruzioni
- Compilazione dalla cartella `src`:
  $ javac -cp . server/Server.java client/Client.java

- Esecuzione dalla cartella `src`:
  - $ java -cp . server/Server portServer
  - $ java -cp . client/Client IPAddressServer portServer

---

## English
A File Transfer Protocol based on Java.

### Components
- Client: allows mget/mput operations
- Server: allows mget/mput operations

### Instructions
- Compile from the `src` folder:
  $ javac -cp . server/Server.java client/Client.java

- Run from the `src` folder:
  - $ java -cp . server/Server portServer
  - $ java -cp . client/Client IPAddressServer portServer
