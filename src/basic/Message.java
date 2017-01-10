
package basic;
/**
 *  @author AonoZan Dejan Petrovic 2016 ©
 */
public class Message {
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

