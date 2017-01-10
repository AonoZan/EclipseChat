
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
	private int portNumber = 1991;
	private boolean serverActive = true;
	private UserDatabase<User> userDatabase = new UserDatabase<>();
	
	public Server(int portNumber) {
		this.portNumber = portNumber;
		try {
			userDatabase = userDatabase.load();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void start() {
		try (ServerSocket serverSocket = new ServerSocket(portNumber)){
			while(serverActive) {
				Socket socket = serverSocket.accept();
				
				User user = new User(socket);
				userDatabase.add(user);
				user.start();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public void answerAll(String message) {
		for (Object object : userDatabase) {
			((User)object).answer(message);
		}
	}
	
	private void logOut(User user) {
		try {
			user.close();
			userDatabase.remove(user);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		int portNumber = 1991;
		switch (args.length) {
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
		
		private String name;
		private String ipAdress;
		private Date dateLoggedIn;
		
		Message message;
		
		public User(Socket socket) {
			this.socket = socket;
			try {
				inStream = new ObjectInputStream(socket.getInputStream());
				outStream = new ObjectOutputStream(socket.getOutputStream());
				
				name = (String)inStream.readObject();
				ipAdress = socket.getLocalAddress().getHostAddress();
				dateLoggedIn = new Date();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}


		@Override
		public void run() {
			while(serverActive) {
				try {
					message = (Message)inStream.readObject();
					String messageContent = message.getMessageContent();
					switch (message.getMessageType()) {
					case Message.TYPE_SPEECH:
						answerAll(messageContent);
						break;
					case Message.TYPE_LOGOUT:
						logOut(this);
						break;
					case Message.TYPE_WHOSIN:
						answer(userDatabase.listAll());
						break;
					default:
						break;
					}
					
				}catch (IOException e) {
					System.out.println(name + ": error reading message.");
					break;
				}catch (Exception e) {
					System.out.println(name + ": error.");
					break;
				}
			}
		}
		
		private void answer(String message) {
			try {
				outStream.writeObject(message);
			} catch (Exception e) {
				// TODO: handle exception
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
					+ (name.toLowerCase().equals("guest")
					? "guest@" + ipAdress : name)
					+ "\n";
		}
	}
}

