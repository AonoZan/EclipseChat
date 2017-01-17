package striped.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 *  @author AonoZan Dejan Petrovic 2016 ©
 */
public class Server {
	// u set sačuvaj sve threadove odnosno kliente
	private static Set<ClientHost> allUsers = new HashSet<>();
	// metoda na serveru koja salje svim threadovima (klientima)
	public static void sendAll(Object message) throws IOException {
		for (ClientHost client : allUsers) {
			client.outStream.writeObject(message);
		}
	}
	public static void main(String[] args) throws IOException {
		boolean active = true;
		// otvori lokalni socket na portu 1991
		ServerSocket serverSocket = new ServerSocket(1991);
		while(active) {
			// cekaj dok ne konektuje se klijent i vrati njegov socket
			Socket clientSocket = serverSocket.accept();
			// kreiraj thread sa socketom i pokreni ga 
			ClientHost clientHost = new ClientHost(clientSocket);
			new Thread(clientHost).start();
			// pa cekaj na sledeceg...
		}
		serverSocket.close();
	}
	static class ClientHost implements Runnable {
		Socket socket;
		ObjectInputStream inStream;
		ObjectOutputStream outStream;
	
		public ClientHost (Socket socket) {
			this.socket = socket;
			try {
				// otvori in/out streamove sa klijentskim socketom
				inStream = new ObjectInputStream(socket.getInputStream());
				outStream = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
			}
			// dodaj sebe(this) u listu threadova
			allUsers.add(this);
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					// procitaj poruku kada stigne od threada i posalji je svim threadovima
					Object message = inStream.readObject();
					sendAll(message);
				} catch (ClassNotFoundException | IOException e) {
				}
				
			}
		}
	}
}

