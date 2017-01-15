
package refactored.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *  @author AonoZan Dejan Petrovic 2016 ©
 */
public class Server extends Thread{
	public static int guestUniqueId = 0;
	public static final int DEFAULT_PORT = 1991;
	public static final String ACTIVE_USERS_PATH = System.getProperty("user.dir")
			+ "/registeredUsers.dat";
	
	private ServerSocket serverSocket = null;
	private boolean active = true;
	private ArrayList<String> registeredUsers = new ArrayList<>();
	private Map<String, ClientHost> activeUsers = new HashMap<>();
	
	private ExecutorService executor = Executors.newFixedThreadPool(32);
	
	public Server() {
		serialize("load");
	}
	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(DEFAULT_PORT);
			while (active) {
				Socket clientSocket = serverSocket.accept();
				NetworkBridge clientBridge = new NetworkBridge(clientSocket);
				ClientHost clientHost = new ClientHost(clientBridge);
				executor.execute(clientHost);
			}
		}catch (IOException e) {
		}
	}
	public void answerAll(Message message) {
		String messageContent = message.getMessageContent();
		System.out.println("Message broadcast:");
		for (ClientHost client : activeUsers.values()) {
			System.out.println("|| to:" + client.logIn.nickName + " >> " + messageContent);
			client.answer(message);
		}
		System.out.print(">> ");
	}
	@SuppressWarnings("unchecked")
	public void serialize(String mode) {
		try {
			switch(mode){
			case "load":
				ObjectInputStream objectInStream = new ObjectInputStream(Files.newInputStream(Paths.get(ACTIVE_USERS_PATH)));
				ArrayList<String> loadedObj = (ArrayList<String>) objectInStream.readObject();
				if (loadedObj != null) registeredUsers = loadedObj;
			case "save":
				ObjectOutputStream objectOutStream = new ObjectOutputStream(Files.newOutputStream(Paths.get(ACTIVE_USERS_PATH)));
				objectOutStream.writeObject(registeredUsers);
			default:
				break;
			}
			
		} catch (IOException ioe) {
			System.out.println("Can't load from path:" + ACTIVE_USERS_PATH);
		} catch (ClassNotFoundException cnf) {
			System.out.println("Class is not defined.");
		} catch (Exception e) {
			System.out.println("Can't save object.");
		}
	}
	public void close() {
		try {
			active = false;
			serialize("save");
			for (ClientHost client : activeUsers.values()) {
				client.close();
			}
			executor.shutdown();
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();//TODO serversocket close
		}
	}
	class ClientHost implements Runnable{
		NetworkBridge networkBridge;
		LogIn logIn;

		public ClientHost(NetworkBridge networkBridge) {
			super();
			this.networkBridge = networkBridge;
		}

		@Override
		public void run() {
			logIn = new LogIn(null);
			String nickName = "guest";
			while(logIn.type != LogIn.APPROVED) {
				logIn = (LogIn)networkBridge.readObject();
				nickName = logIn.nickName;
				if(nickName == null) {
					close();
					break;
				}else if (nickName.equals("guest")) {
					nickName = nickName + guestUniqueId++;
					logIn.nickName = nickName;
				}
				if (activeUsers.containsKey(nickName)){
					logIn.type = LogIn.NICK_TAKEN;
				}else {
					if(registeredUsers.contains(nickName)) {
						
						answerAll(new Message(nickName + " joined party."));
					}else {
						answerAll(new Message(nickName + " have registered."));
						registeredUsers.add(nickName);
					}
					logIn.type = LogIn.APPROVED;
				}
				networkBridge.writeObject(logIn);
			}
			if(nickName != null) {
				activeUsers.put(nickName, this);
				new InputWorker().start();
			}
			
		}
		public void answer(Message message) {
			networkBridge.writeObject(message);
		}
		public void close() {
			networkBridge.close();
		}
		class InputWorker extends Thread {
			@Override
			public void run() {
				while(active) {
					Message message = (Message)networkBridge.readObject();
					if (message != null) {
						int messageType = message.getMessageType();
						if (messageType == Message.TYPE_SPEECH) {
							String messageContent = message.getMessageContent();
							Message returnMessage = new Message(logIn.nickName
									+ ":" + messageContent);
							answerAll(returnMessage);
						}else if (messageType == Message.TYPE_WHOSIN) {
							answerAll(new Message(activeUsers.toString()));
						}else if (messageType == Message.TYPE_LOGOUT) {
							answerAll(new Message(logIn.nickName + ":Logout."));
							activeUsers.remove(logIn.nickName);
							networkBridge.close();
							break;
						}
					}
				}
			}
		}
		
	}
	
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		String serverCommand = "";
		Server server = new Server();
		server.start();
		
		while(!serverCommand.equals("exit")) {
			System.out.print(">> ");
			serverCommand = input.nextLine();
		}
		
		input.close();
		server.close();
	}
}

