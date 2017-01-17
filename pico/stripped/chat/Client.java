
package stripped.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 *  @author AonoZan Dejan Petrovic 2016 ©
 */
public class Client {
	static boolean active = true;
	static ObjectInputStream inStream;
	static ObjectOutputStream outStream;
	static Socket socket;
	public static void main(String[] args) throws UnknownHostException, IOException {
		Scanner input = new Scanner(System.in);
		String name = "user"+(System.currentTimeMillis()%1000);
		// kreiraj socket na localhostu i portu 1991
		socket = new Socket("localhost", 1991);
		try {
			// out stream se moze otvoriti odmah
			outStream = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
		}
		// posalji prvu poruku
		outStream.writeObject(name + " se ulogovao");
		// kreiraaj thread koji ce da slusa poruke koje stignu i da ih printa
		InputListener inListener = new InputListener();
		new Thread(inListener).start();
		while(active) {
			// uzmi poruku od korisnika i posalji je sa output streamom
			String message = input.nextLine();
			outStream.writeObject(name + ": " + message);
			// ako listener nije jos uvjek inicijaliziran
			// uzimaj sledecu poruku od korisnika i salji je serveru
		}
		input.close();
		socket.close();
	}
	static class InputListener implements Runnable{

		@Override
		public void run() {
			try {
				// in stream se moze sada vjerovatno otvoriti jer je server odgovorio 
				inStream = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e1) {
			}
			while(active) {
				try {
					// printaj sve poruke koje stignu over and over...
					System.out.println(">> " + inStream.readObject());
				} catch (ClassNotFoundException | IOException e) {
				}
			}
		}
		
	}
}

