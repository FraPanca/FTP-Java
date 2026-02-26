package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	public static void main(String[] args) {
		
		// args: porta server
		
		int port = -1;
		try {
			port = Integer.parseInt(args[0]);
			
			if(port < 1024 || port > 65536)
				throw new NumberFormatException("il numero di porta deve essere compreso tra 1024 e 65535");
		} catch(NumberFormatException e) {
			System.err.println("[SERVER] : Errore -> Numero di porta non valida: " + e);
			System.exit(1);
		}
		
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setReuseAddress(true);
			System.out.println("[SERVER] : Server avviato.");
			System.out.println("[SERVER] : Server in ascolto sulla porta: " + port + "\n");
			
			Socket clientSocket = null;
			while(true) {
				clientSocket = serverSocket.accept();
				clientSocket.setSoTimeout(20000); // 20s
				
				System.out.println("[SERVER] : Accettata nuova richiesta: " + clientSocket.getInetAddress().toString() + " - " + clientSocket.getPort());
				
				ServerSocketThreads serverSocketThread = new ServerSocketThreads(clientSocket);
				serverSocketThread.start();				
			}
			
		} catch(IOException e) {
			System.err.println("[SERVER] : Errore -> Creazione della socket non riuscita: " + e);
			System.exit(2);
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.err.println("[SERVER] : Errore -> Chiusura della socket non riuscita: " + e);
				System.exit(3);
			}
		}
	}

}



class ServerSocketThreads extends Thread {
	
	private Socket socket;
	
	public ServerSocketThreads(Socket socket) {
		this.socket = socket;
	}
	
	
	public void run() {
		
		DataInputStream in = null;
		DataOutputStream out = null;
		
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			
			
			String operation = null;
			
			boolean isOperating = true;
			while(isOperating) {
				
				System.out.println("");
				
				operation = in.readUTF();
				
				if(operation.equals("mget")) {
					
					System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): ========== Scaricamento ==========");
					
					String command = null;
					String fileName = null;
					String dirName = null;
					
					int transferedFiles = 0;
					int totalFiles = 0;
					
					FileOutputStream outFile = null;
					
					// il ciclo termina quando viene inviato il "comando" end
					boolean notFinished = true;
					while(notFinished) {
						try {
							if((command = in.readUTF()) == null)
								throw new FileNotFoundException("[SERVER] : (Verso " + socket.getInetAddress() + "): Errore -> Comando nullo.");
							
							if(command.equals("endTransfer"))
								throw new IOException();
							
								
							if((fileName = in.readUTF()) == null) 
								throw new FileNotFoundException("[SERVER] : (Verso " + socket.getInetAddress() + "): Errore -> File " + fileName + " nullo.");
						} catch (FileNotFoundException e) {
							System.err.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Errore -> File " + fileName + " nullo.");
						} catch(IOException e) {
							System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Fine dei file ricevuti.");
							System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): File scaricati: " + transferedFiles + "/" + totalFiles);
							notFinished = false;
						} 
						
						if(notFinished) {
							
							if(command.equals("dir") && !(new File("server/" + fileName).exists())) { // caso in cui venga passato il nome del direttorio e non esista
								dirName = "server/" + fileName + "/";
								
								if(!(new File(dirName).mkdir())) {
									System.err.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Errore -> Impossibile creare il direttorio " + fileName);
								}
							} else if(command.equals("dir")) { // caso in cui venga passato il nome del direttorio ed esista già
								dirName = "server/" + fileName + "/";
								System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Il direttorio " + dirName + " è già esistente: verranno aggiunti i file nuovi.");
							} else if(command.equals("file") && new File(dirName + fileName).exists()) { // caso in cui venga passato il nome del file ed esista già in quel direttorio
								out.writeUTF("salta");
								
								totalFiles++;
								System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): File già esistente: " + dirName + fileName);
							} else { // caso in cui venga passato il nome del file e non esista ancora in quel direttorio
								out.writeUTF("attiva");
								
								totalFiles++;
								System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): File nuovo: " + dirName + fileName);
								
								
								int lengthFile = 0;
								try {
									lengthFile = Integer.parseInt(in.readUTF());
									System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Lunghezza file: " + lengthFile);
								} catch(NumberFormatException e) {
									System.err.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Errore -> Inserita una lunghezza del file " + dirName + fileName + " non valida: " + e);
								}
								
								outFile = new FileOutputStream(dirName + fileName);

								int nByte = -1;
								byte[] buf = new byte[lengthFile];
								if((nByte = in.read(buf)) != lengthFile) {
									// caso in cui la lunghezza del file passato sia diversa dal numero inviato precedentemente
									// in questo caso si rimuove il file dal direttorio corrente
									System.err.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Errore -> Lunghezza del file " + dirName + fileName + " non corretta: aspettati " + lengthFile + ", ricevuti " + nByte);
									outFile.close();
									
									File fileError = new File(dirName + fileName);
									fileError.delete();
								} else {
									outFile.write(buf);	
									outFile.close();
								}
								
								transferedFiles++;
								System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): File " + dirName + fileName + " scritto correttamente.");							
							}										
						}
					}
					
				} else if(operation.equals("mput")) {
					
					System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): ========== Caricamento ==========");
					
					// comunica la lista dei direttori presenti sul server
					File[] serverPath = new File("server/").listFiles();
					String dirList = "";
					for(File f: serverPath) {
						if(f.isDirectory())
							dirList += "\t" + f.getName() + "\n";
					}					
					out.writeUTF(dirList);
					
					// riceve i parametri passati dall'utente da console
					String[] param = in.readUTF().split(" ");

					// la variabile noThres è:
					// 0 se non è stato passato il valore di soglia
					// 1 se è stato passato un valore
					// e serve se è stato passato un valore per escludere nel for tale valore nei nomi dei direttori
					int noThres = 0;
					int threshold = -1;
					try {
						threshold = Integer.parseInt(param[param.length-1]);
						noThres = 1;
					} catch(NumberFormatException e) {
						System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Non è stato impostato alcun valore di soglia.");
					}
					
					// contatori per tenere traccia di quanti file sono stati trasferiti
					int transferedFiles = 0;
					int totalFiles = 0;
					
					System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Inizio trasferimento...");
					for(int i=0; i<param.length-noThres; i++) {
						String dirName = param[i];
						System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Direttorio: " + dirName);
						
						if(!new File("server/", dirName).exists()) {
							System.err.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Errore -> Il nome della directory " + dirName + " passata non esiste.");
							System.exit(4);
						}
						
						out.writeUTF("dir");
						out.writeUTF(dirName);
						
						File dir = new File("server/", dirName);
						try {
							for(String fileName: dir.list()) {
								File file = new File(dir, fileName);
								
								totalFiles++;
								System.out.print("[SERVER] : (Verso " + socket.getInetAddress() + "): \tValutazione del file: " + fileName + " -> ");
								
								if(file.length() > threshold) {
									out.writeUTF("file");
									out.writeUTF(fileName);
									
									String response = in.readUTF();
									if(response.equals("attiva")) {
										System.out.println("accettato.");
										System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): \tInizia il trasferimento del file " + fileName + " (lunghezza: " + file.length() + ").");
										
										out.writeUTF("" + file.length());
										
										FileInputStream inFile = new FileInputStream(file);
										byte[] buffer = new byte[4096];
										int bytesRead;
										while ((bytesRead = inFile.read(buffer)) != -1) {
										    out.write(buffer, 0, bytesRead);
										}
										out.flush();
										
										inFile.close();
										
										transferedFiles++;
										System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): \tTrasferimento del file " + fileName + " concluso.");
									} else if(response.equals("salta")) {
										System.out.println("saltato perché è già presente.");
									} else {
										System.err.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Errore -> Ricevuta una risposta dal server inaspettata.");
										System.exit(6);
									}
								} else System.out.println("saltato perché è più piccolo della soglia passata (" + file.length() + "/" + threshold + ").");
							}					
						} catch(IOException e) {
							System.err.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Errore -> La comunicazione dei file è stata interrotta: " + e);
							System.exit(5);
						}
						
						System.out.println("");
					}
					out.writeUTF("endTransfer");
					System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Trasferimento terminato.");
					System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): File caricati: " + transferedFiles + "/" + totalFiles);			
					
				} else if(operation.equals("endOperations")) {
					isOperating = false;
					
					System.out.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Operazioni Terminate. Chiusura sessione.");
				} else {
					System.err.println("[SERVER] : (Verso " + socket.getInetAddress() + "): Errore -> Inserito il comando passato non esiste.");
					
					// chiude la connessione con quel client
					isOperating = false;
				}
			}			
			
			
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			System.err.println("[SERVER] : (Verso " + socket.getInetAddress() + " ): Errore -> Lettura/Scrittura con socket non riuscita: " + e);
		}		
	}
	
}