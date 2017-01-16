package refactored.chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 *  @author AonoZan Dejan Petrovic 2016 ©
 */
public class NetworkBridge {
	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 1991;
	
	private Socket socket = null;
	
	private ObjectInputStream inStream = null;
	private ObjectOutputStream outStream = null;
	
	public NetworkBridge() {
		this(DEFAULT_HOST, DEFAULT_PORT);
	}
	public NetworkBridge(String host, int port) {
		try {
			this.socket = new Socket(host, port);
		} catch (IOException e) {
			System.out.println("Network bridge host/port error");
		}
	}
	public NetworkBridge(Socket socket){
		this.socket = socket;
	}
	
	public boolean isSocketNull() {
		return socket == null;
	}
	
	public Object readObject() {
		try {
			if(inStream == null) {
				this.inStream = new ObjectInputStream(socket.getInputStream());
			}
			return inStream.readObject();
		} catch (ClassNotFoundException | IOException e) {
			System.out.println("Network bridge read error");
		}
		
		return null;
	}
	public boolean writeObject(Object obj) {
		try {
			if(outStream == null) {
				this.outStream = new ObjectOutputStream(socket.getOutputStream());
			}
			outStream.writeObject(obj);
		} catch (IOException e) {
			System.out.println("Network bridge write error");
		}
		return false;
	}
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

