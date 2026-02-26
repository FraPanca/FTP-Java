package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

	public static void main(String[] args) {
		
		// args: ip server, porta server
		
		InetAddress addr = null;
		int port = -1;
		try {
			addr = InetAddress.getByName(args[0]);
			
			port = Integer.parseInt(args[1]);
			if(port < 1024 || port > 65535)
				throw new NumberFormatException("il umero di porta deve essere compreso tra 1024 e 65535.");
		} catch(UnknownHostException e) {
			System.err.println("[CLIENT] : Errore -> Indirizzo ip inserito non valido: " + e);
			System.exit(1);
		} catch(NumberFormatException e) {
			System.err.println("[CLIENT] : Errore -> Porta inserita non valida: " + e);
			System.exit(2);
		}
		
		BufferedReader inConsole = null;
		
		DataInputStream in = null;
		DataOutputStream out = null;
		
		Socket socket = null;
		try {
			socket = new Socket(addr, port);
			
			System.out.println("[CLIENT] : Client avviato.");
			
			inConsole = new BufferedReader(new InputStreamReader(System.in));
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			
			String inputUser = null;
			
			boolean isOperating = true;
			while(isOperating) {
				
				System.out.println("\n\n========== Operazioni ==========");
				System.out.println("\tmget : per scaricare file e direttori sul server");
				System.out.println("\tmput : per caricare file e direttori sul server");
				System.out.println("\tend : per terminare le operazioni sul server");
				System.out.println("Inserisci: ");
				inputUser = inConsole.readLine().toLowerCase();
				
				System.out.println("");
				
				if(inputUser.equals("mget")) {
					
					System.out.println("========== Scaricamento ==========");
					out.writeUTF("mput"); // ordina al servitore di operare in modalità mput
					
					// stampa della lista dei direttori disponibili sul server
					System.out.println("[CLIENT] : Direttori presenti sul server:\n" + in.readUTF());
					System.out.println("Inserisci i nomi dei direttori da scaricare e l'eventuale dimensione minima: ");
					
					out.writeUTF(inConsole.readLine());
					System.out.println("");
					
					
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
								throw new FileNotFoundException("Comando nullo.");
							
							if(command.equals("endTransfer"))
								throw new IOException();
							
								
							if((fileName = in.readUTF()) == null) 
								throw new FileNotFoundException("File " + fileName + " nullo.");
						} catch (FileNotFoundException e) {
							System.err.println("[CLIENT] : Errore -> " + e);
						} catch(IOException e) {
							System.out.println("[CLIENT] : Fine dei file ricevuti.");
							System.out.println("[CLIENT] : File scaricati: " + transferedFiles + "/" + totalFiles);
							notFinished = false;
						} 
						
						if(notFinished) {
							
							if(command.equals("dir") && !(new File("client/" + fileName).exists())) { // caso in cui venga passato il nome del direttorio e non esista
								dirName = "client/" + fileName + "/";
								
								if(!(new File(dirName).mkdir())) {
									System.err.println("[CLIENT] : Errore -> Impossibile creare il direttorio " + fileName);
								}
							} else if(command.equals("dir")) { // caso in cui venga passato il nome del direttorio ed esista già
								dirName = "client/" + fileName + "/";
								System.out.println("[CLIENT] : Il direttorio " + dirName + " è già esistente: verranno aggiunti i file nuovi");
							} else if(command.equals("file") && new File(dirName + fileName).exists()) { // caso in cui venga passato il nome del file ed esista già in quel direttorio
								out.writeUTF("salta");
								
								totalFiles++;
								System.out.println("[CLIENT] : File già esistente: " + dirName + fileName);
							} else { // caso in cui venga passato il nome del file e non esista ancora in quel direttorio
								out.writeUTF("attiva");
								
								totalFiles++;
								System.out.println("[CLIENT] : File nuovo: " + dirName + fileName);
								
								
								int lengthFile = 0;
								try {
									lengthFile = Integer.parseInt(in.readUTF());
									System.out.println("[CLIENT] : Lunghezza file: " + lengthFile);
								} catch(NumberFormatException e) {
									System.err.println("[CLIENT] : Errore -> Lunghezza del file " + dirName + fileName + " non valida: " + e);
								}
								
								outFile = new FileOutputStream(dirName + fileName);

								int nByte = -1;
								byte[] buf = new byte[lengthFile];
								if((nByte = in.read(buf)) != lengthFile) {
									// caso in cui la lunghezza del file passato sia diversa dal numero inviato precedentemente
									// in questo caso si rimuove il file dal direttorio corrente
									System.err.println("[CLIENT] : Errore -> Lunghezza del file " + dirName + fileName + " non corretta: aspettati " + lengthFile + ", ricevuti " + nByte);
									outFile.close();
									
									File fileError = new File(dirName + fileName);
									fileError.delete();
								} else {
									outFile.write(buf);	
									outFile.close();
								}
								
								transferedFiles++;
								System.out.println("[CLIENT] : File " + dirName + fileName + " scritto correttamente.");							
							}										
						}
					}
						
					
				} else if(inputUser.equals("mput")){
					
					System.out.println("========== Caricamento ==========");
					out.writeUTF("mget"); // ordina al servitore di operare in modalità mget
					
					
					System.out.println("Inserisci i nomi dei direttori da caricare e l'eventuale dimensione minima: ");
					inputUser = inConsole.readLine();
					System.out.println("");
					
					
					String[] param = inputUser.split(" ");

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
						System.out.println("[CLIENT] : Non è stato impostato alcun valore di soglia.");
					}
					
					// contatori per tenere traccia di quanti file sono stati trasferiti
					int transferedFiles = 0;
					int totalFiles = 0;
					
					System.out.println("[CLIENT] : Inizio trasferimento...");
					for(int i=0; i<param.length-noThres; i++) {
						String dirName = param[i];
						System.out.println("[CLIENT] : Direttorio: " + dirName);
						
						if(!new File("client/", dirName).exists()) {
							System.err.println("[CLIENT] : Errore -> Il nome della directory " + dirName + " passata non esiste.");
							System.exit(4);
						}
						
						out.writeUTF("dir");
						out.writeUTF(dirName);
						
						File dir = new File("client/", dirName);
						try {
							for(String fileName: dir.list()) {
								File file = new File(dir, fileName);
								
								totalFiles++;
								System.out.print("[CLIENT] : \tValutazione del file: " + fileName + " -> ");
								
								if(file.length() > threshold) {
									out.writeUTF("file");
									out.writeUTF(fileName);
									
									String response = in.readUTF();
									if(response.equals("attiva")) {
										System.out.println("accettato.");
										System.out.println("[CLIENT] : \tInizia il trasferimento del file " + fileName + " (lunghezza: " + file.length() + ") verso il server " + addr + " - " + port);
										
										out.writeUTF("" + file.length());
										
										FileInputStream inFile = new FileInputStream(file);
										byte[] buffer = new byte[4096];
										int bytesRead;
										while ((bytesRead = inFile.read(buffer)) != -1) {
										    out.write(buffer, 0, bytesRead);
										}
										out.flush();
										inFile.close();

										
										out.flush();
										
										inFile.close();
										
										transferedFiles++;
										System.out.println("[CLIENT] : \tTrasferimento del file " + fileName + " concluso.");
									} else if(response.equals("salta")) {
										System.out.println("saltato perché è già presente.");
									} else {
										System.err.println("[CLIENT] : Errore -> Ricevuta una risposta dal server inaspettata.");
										System.exit(6);
									}
								} else System.out.println("saltato perché è più piccolo della soglia passata (" + file.length() + "/" + threshold + ").");
							}					
						} catch(IOException e) {
							System.err.println("[CLIENT] : Errore -> La comunicazione dei file è stata interrotta: " + e);
							System.exit(5);
						}
						
						System.out.println("");
					}
					out.writeUTF("endTransfer");
					System.out.println("[CLIENT] : Trasferimento terminato.");
					System.out.println("[CLIENT] : File caricati: " + transferedFiles + "/" + totalFiles);
					
				} else if(inputUser.equals("end")) {
					isOperating = false;
					
					out.writeUTF("endOperations");
					System.out.println("[CLIENT] : Operazioni Terminate. Chiusura sessione.");
				} else {
					System.err.println("[CLIENT] : Errore -> Inserito il comando passato non esiste.");
				}
			}
			
			inConsole.close();
			in.close();
			out.close();
			socket.close();
		} catch(IOException e) {
			System.err.println("[CLIENT] : Errore -> Comportamento non previsto della socket: " + e);
			System.exit(3);
		}
	}
}