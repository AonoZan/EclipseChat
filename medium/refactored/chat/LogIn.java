
package refactored.chat;

import java.io.Serializable;

/**
 *  @author AonoZan Dejan Petrovic 2016 ©
 */
public class LogIn implements Serializable{
	private static final long serialVersionUID = 3590718468796054165L;
	public static final int REQUEST = 0;
	public static final int APPROVED = 1;
	public static final int NICK_TAKEN = 2;
	
	public String nickName;
	public int type;

	public LogIn(String nickName) {
		super();
		this.nickName = nickName;
	}
}

