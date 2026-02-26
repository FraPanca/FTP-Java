# FTP-Java
A File Transfer Protocol based on Java.

==== COMPONENTI ====
 - Client su cui è possibile effettuare operazioni di mget/mput
 - Server su cui è possibile effettuare operazioni di mget/mput


==== ISTRUZIONI ====
 - Compilazione dalla cartella src:
    - $ javac -cp . server/Server.java client/Client.java

 - Esecuzione dalla cartella src:
    - $ java -cp . server/Server portServer
    - $ java -cp . client/Client IPAddressServer portServer
