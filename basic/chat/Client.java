
package chat;

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
			Scanner input = new Scanner(System.in)) {
			Client client = new Client(socket, userName);
			client.start();
			while(true) {
				String messageContent = input.nextLine();
				Message message = null;
				switch (messageContent.toLowerCase()) {
				case "whosin":
					message = new Message(1, messageContent);
					break;
				case "logout":
					message = new Message(2, messageContent);
					break;
				default:
					message = new Message(userName + ": " + messageContent);
					break;
				}
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
				System.out.println("Server shutdown.");
				System.exit(0);
			}
			while(true) {
				try {
					Message message = (Message)inStream.readObject();
					System.out.print(">> ");
					System.out.println(message.getMessageContent());
				} catch (Exception e) {
					System.out.println("Logout.");
					System.exit(0);
				}
			}
		}
	}
}

