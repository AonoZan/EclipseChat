
package basic;

import java.io.Serializable;

/**
 *  @author AonoZan Dejan Petrovic 2016 ©
 */
public class Message implements Serializable{
	private static final long serialVersionUID = -1417092197358128858L;
	public static final int TYPE_SPEECH = 0;
	public static final int TYPE_WHOSIN = 1;
	public static final int TYPE_LOGOUT = 2;
	
	private int messageType;
	private String messageContent;
	
	public Message(String messageContent) {
		this(0, messageContent);
	}
	public Message(int messageType, String messageContent) {
		super();
		this.messageType = messageType;
		this.messageContent = messageContent;
	}
	
	public int getMessageType() {
		return messageType;
	}
	public String getMessageContent() {
		return messageContent;
	}
	
	
	
}

