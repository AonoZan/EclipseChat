
package refactored.chat;

import java.util.Scanner;

import javax.swing.JOptionPane;

/**
 *  @author AonoZan Dejan Petrovic 2016 ©
 */
public class Client {
	
	String nickName;
	NetworkBridge networkBridge;
	
	private boolean active = true;
	
	public Client(String nickName, NetworkBridge networkBridge) {
		super();
		this.nickName = nickName;
		this.networkBridge = networkBridge;
	}
	
	public void setUp() {
		InputWorker inWorker = new InputWorker();
		OutputWorker outWorker = new OutputWorker();
		
		inWorker.start();
		outWorker.start();
		
		System.out.println("Finished setup.");
	}
	
	class InputWorker extends Thread{
		@Override
		public void run() {
			Scanner input = new Scanner(System.in);
			while(active) {
				String userInput = input.nextLine();
				System.out.println(userInput);
				int messageType = decodeMessageType(userInput);
				Message message = new Message(messageType, userInput);
				networkBridge.writeObject(message);
			}
			input.close();
		}
		public int decodeMessageType(String message) {
			if(message.startsWith("special")) return Message.TYPE_SPECIAL;
			if(message.length() > 8) return Message.TYPE_SPEECH;
			message = message.toLowerCase();
			
			switch (message) {
			case "whosin":
				return Message.TYPE_WHOSIN;
			case "logout":
				return Message.TYPE_LOGOUT;
			default:
				break;
			}
			return Message.DEFAULT_TYPE;
		}
	}
	class OutputWorker extends Thread {
		@Override
		public void run() {
			while(active) {
				Message message = (Message)networkBridge.readObject();
				System.out.println(">> " + message.getMessageContent());
			}
		}
	}
	
	public static void main(String[] args) {
		
		String nickName = JOptionPane.showInputDialog(
	            "Enter your nick name.", "guest");
		NetworkBridge networkBridge = new NetworkBridge();
		
		if(networkBridge.isSocketNull()) {
			System.out.println("Can't connect to the localhost.\n"
					+ "Try again later");
			System.exit(0);
		}
		
		Client client = new Client(nickName, networkBridge);
		client.setUp();
	}

}

