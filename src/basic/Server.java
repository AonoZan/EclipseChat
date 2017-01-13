
package basic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 *  @author AonoZan Dejan Petrovic 2016 ©
 */
public class Server {
	private int portNumber;
	private boolean serverActive = true;
	private UserDatabase<String, User> userDatabase = new UserDatabase<>();
	private ArrayList<String> userNickNames = new ArrayList<>();
	public static final String DATABASE_LOC = System.getProperty("user.dir")
			+ "/default.dat";
	
	public Server(int portNumber) {
		this.portNumber = portNumber;
		ArrayList<String> userNickNamesLoad = load();
		if(userNickNamesLoad != null) {
			userNickNames = userNickNamesLoad;
		}
	}
	
	public void start() {
		try (ServerSocket serverSocket = new ServerSocket(portNumber)){
			while(serverActive) {
				Socket socket = serverSocket.accept();
				User user = new User(socket);
				String userNickName = user.getNickName();
				
				if(!userDatabase.values().contains(userNickName)) {
					userDatabase.put(userNickName, user);
					if(!userNickName.equals("guest")) {
						userNickNames.add(user.getNickName());
					}
					user.start();
					Message newUserMessage = null;
					if (userNickNames.contains(userNickName)) {
						newUserMessage = new Message("Welcome back: " + userNickName);
					}else {
						newUserMessage = new Message("New user on server: " + userNickName);
					}
					answerAll(newUserMessage);
				}else {
					Message newUserMessage = new Message("User name '" + userNickName + "' is already taken.\n");
					System.out.println("Someone tried");
					user.answer(newUserMessage);
					socket.close();
				}
			}
			
		} catch (Exception e) {
			System.out.println("Can't open port at " + portNumber);
		}
	}
	
	public void stop() {
		save(userNickNames);
		System.out.println("Server stoped.");
		System.exit(0);
	}
	
	public ArrayList<String> load() {
		return load(DATABASE_LOC);
	}
	@SuppressWarnings("unchecked")
	public ArrayList<String> load(String path) {
		ArrayList<String> database = null;

		try (ObjectInputStream objectStream = new ObjectInputStream(Files.newInputStream(Paths.get(path)))) {
			database = (ArrayList<String>) objectStream.readObject();
		} catch (IOException ioe) {
			System.out.println("Can't load from path:" + path);
		} catch (ClassNotFoundException cnf) {
			System.out.println("Class is not defined.");
		} catch (Exception e) {
			System.out.println("Can't save object.");
		}

		return database;
	}
	
	public void save(Object database) {
		save(database, DATABASE_LOC);
	}
	public void save(Object database, String path) {
		try (ObjectOutputStream objectStream = new ObjectOutputStream(Files.newOutputStream(Paths.get(path)))) {
			objectStream.writeObject(database);
		} catch (IOException ioe) {
			System.out.println("Can't save to path: " + path);
			ioe.printStackTrace();
		} catch (Exception e) {
			System.out.println("Can't save object.");
		}
	}
	
	
	public void answerAll(Message message) {
		for (User user : userDatabase.values()) {
			System.out.println("[sending to " + user.getNickName()
								+ "] " + message.getMessageContent());
			if(user.isAlive()) {
				user.answer(message);
			} else {
				logOut(user);
			}
		}
	}
	
	private void logOut(User user) {
		try {
			user.close();
			userDatabase.remove(user);
		} catch (IOException e) {
			System.out.println("Can't close user.");
		}
	}
	
	
	public static void main(String[] args) {
		int portNumber = 1991;
		
		switch (args.length) {
			case 0:
				// for no arg call use default settings
				break;
			case 1:
				try {
					portNumber = Integer.parseInt(args[0]);
				} catch (Exception e) {
					System.out.println("Cant parse argument. Please provide port number.");
				}
				break;
			default:
				System.out.println("Usage: java basic.Server portNumber");
				System.exit(0);
				break;
		}
		
		Server server = new Server(portNumber);
		class ServerInterpreter implements Runnable{
			@Override
			public void run() {
				Scanner input = new Scanner(System.in);
				System.out.println("Server running.");
				while(true) {
					String in = input.nextLine();
					if (in.equals("exit")) {
						server.stop();
						break;
					}
				}
				input.close();
			}
		}
		Thread interpreter = new Thread(new ServerInterpreter());
		
		interpreter.start();
		server.start();
		
	}
	
	
	public class User extends Thread implements Serializable{
		private static final long serialVersionUID = -4709066551391491898L;
		private transient Socket socket;
		private transient ObjectInputStream inStream;
		private transient ObjectOutputStream outStream;
		
		private String nickName;
		private String ipAdress;
		private transient Date dateLoggedIn;
		
		private transient Message message;
		
		public User(Socket socket) {
			this.socket = socket;
			try {
				inStream = new ObjectInputStream(socket.getInputStream());
				outStream = new ObjectOutputStream(socket.getOutputStream());
				
				nickName = (String)inStream.readObject();
				ipAdress = socket.getLocalAddress().getHostAddress();
				dateLoggedIn = new Date();
			} catch (Exception e) {
				System.out.println("Error opening user in/out stream.");
			}
		}


		@Override
		public void run() {
			while(serverActive) {
				try {
					message = (Message)inStream.readObject();
					switch (message.getMessageType()) {
						case Message.TYPE_SPEECH:
							answerAll(message);
							break;
						case Message.TYPE_LOGOUT:
							logOut(this);
							break;
						case Message.TYPE_WHOSIN:
							Message whois = new Message("Logged in users:\n"
									+ userDatabase.listAll());
							answer(whois);
							break;
						default:
							break;
					}
					
				}catch (IOException e) {
					System.out.printf("%s@%s: Logout.\n", nickName, ipAdress);
					break;
				}catch (Exception e) {
					System.out.printf("%s@%s: Error.\n", nickName, ipAdress);
					break;
				}
			}
		}
		
		public String getNickName() {
			return nickName;
		}

		public String getIpAdress() {
			return ipAdress;
		}

		public Date getDateLoggedIn() {
			return dateLoggedIn;
		}

		public Message getMessage() {
			return message;
		}

		private void answer(Message message) {
			try {
				outStream.writeObject(message);
			} catch (Exception e) {
				System.out.println("Cant write object to user.");
			}
		}
		
		public void close() throws IOException {
			socket.close();
			inStream.close();
			outStream.close();
		}
		
		@Override
		public String toString() {
			return dateLoggedIn + ": " 
					+ (nickName.toLowerCase().equals("guest")
					? "guest@" + ipAdress : nickName)
					+ "\n";
		}
	}
}

