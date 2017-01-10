
package basic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 *  @author AonoZan Dejan Petrovic 2016 ©
 */
public class Server {
	private int portNumber;
	private boolean serverActive = true;
	private UserDatabase<User> userDatabase = new UserDatabase<>();;
	
	public Server(int portNumber) {
		this.portNumber = portNumber;
		UserDatabase<User> loadDatabase = userDatabase.load();
		if(loadDatabase != null) {
			userDatabase = loadDatabase;
		}
	}
	
	public void start() {
		try (ServerSocket serverSocket = new ServerSocket(portNumber)){
			System.out.println(serverSocket.getInetAddress());
			while(serverActive) {
				Socket socket = serverSocket.accept();
				
				User user = new User(socket);
				userDatabase.add(user);
				user.start();
				Message newUserMessage = new Message("New user on server: " + user.getNickName());
				answerAll(newUserMessage);
			}
		} catch (Exception e) {
			System.out.println("Can't open port at " + portNumber);
		}
	}
	
	public void answerAll(Message message) {
		for (User user : userDatabase) {
			System.out.println("sending to all: " + message.getMessageContent());
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
					return;
				}
				break;
			default:
				System.out.println("Usage: java basic.Server portNumber");
				System.exit(0);
				break;
		}
		Server server = new Server(portNumber);
		server.start();
	}
	
	public class User extends Thread implements Serializable{
		private static final long serialVersionUID = -4709066551391491898L;
		private Socket socket;
		private ObjectInputStream inStream;
		private ObjectOutputStream outStream;
		
		private String nickName;
		private String ipAdress;
		private Date dateLoggedIn;
		
		Message message;
		
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
//					String messageContent = message.getMessageContent();
					switch (message.getMessageType()) {
						case Message.TYPE_SPEECH:
							answerAll(message);
							break;
						case Message.TYPE_LOGOUT:
							logOut(this);
							break;
						case Message.TYPE_WHOSIN:
							Message whois = new Message(userDatabase.listAll());
							answer(whois);
							break;
						default:
							break;
					}
					
				}catch (IOException e) {
					System.out.println(nickName + ": error reading message.");
					break;
				}catch (Exception e) {
					System.out.println(nickName + ": error.");
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

