package refactored.chat;

import java.io.Serializable;

/**
 *  Generic chat message object for easier network exchange of messages.
 *  @author AonoZan Dejan Petrovic 2016 ©
 *  @messageType represents type of the message. There are 4 types:
 *  @messageContent contains message.
 *  @TYPE_SPEECH its simple text message.
 *  @TYPE_WHOSIN request to get all active users on server.
 *  @TYPE_LOGOUT message of this type means that user has logged out.
 *  @TYPE_SPECIAL special message for various hacks/commands.
 */
public class Message implements Serializable{
	private static final long serialVersionUID = -1417092197358128858L;
	public static final int TYPE_SPEECH = 0;
	public static final int TYPE_WHOSIN = 1;
	public static final int TYPE_LOGOUT = 2;
	public static final int TYPE_SPECIAL = 3;
	
	public static final int DEFAULT_TYPE = 0;
	public static final String DEFAULT_CONTENT = new String();
	
	private int messageType;
	private String messageContent;
	
	public Message() {
		this(DEFAULT_TYPE, DEFAULT_CONTENT);
	}
	public Message(int messageType) {
		this(messageType, DEFAULT_CONTENT);
	}
	public Message(String messageContent) {
		this(DEFAULT_TYPE, messageContent);
	}
	public Message(int messageType, String messageContent) {
		super();
		
		final int typeMin = 0, typeMax = 3;
		if (messageType < typeMin | messageType > typeMax) {
			messageType = DEFAULT_TYPE;
		}
		if (messageContent == null) {
			messageContent = DEFAULT_CONTENT;
		}
		
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

