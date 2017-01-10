
package basic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JOptionPane;

/**
 *  @author AonoZan Dejan Petrovic 2016 ©
 */
public class Client {
	private Socket socket;
	private String userName;
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
	
	public Client(Socket socket, String userName) {
		super();
		this.socket = socket;
		this.userName = userName;
	}
	
	public void start() {
		try {
			System.out.println(socket.getPort());
			outStream = new ObjectOutputStream(socket.getOutputStream());
			outStream.writeObject(userName);
			new PortMonitor().start();
		} catch (IOException e) {
			System.out.println("Can't open client output stream");
		}
	}
	
	public void send(Message message) throws IOException {
		outStream.writeObject(message);
	}
	
	public static void main(String[] args) {
		int portNumber = 1991;
		String serverAddress = JOptionPane.showInputDialog(
	            "Enter IP Address of the chat server.", "localhost");
		String userName = JOptionPane.showInputDialog(
	            "Enter your user name.", "guest");
		
		try (Socket socket = new Socket(serverAddress, portNumber);
			Scanner scan = new Scanner(System.in)) {
			Client client = new Client(socket, userName);
			client.start();
			while(true) {
				String messageContent = scan.nextLine();
				Message message = new Message(messageContent);
				client.send(message);
			}
		} catch (IOException e) {
			System.out.println("Can't open new socket:"
					+ serverAddress + "@" + portNumber);
		}
	}
	class PortMonitor extends Thread{
		public void run() {
			try {
				inStream = new ObjectInputStream(socket.getInputStream());
			} catch (IOException e1) {
				System.out.println("Can't read client input stream.");
				System.exit(0);
			}
			while(true) {
				try {
					Message message = (Message)inStream.readObject();
					System.out.print(">> ");
					System.out.println(message.getMessageContent());
				} catch (Exception e) {
					System.out.println("Cant read object from input stream.");
					System.exit(0);
				}
			}
		}
	}
}

