
package chat;

import java.io.Serializable;
import java.util.HashMap;

/**
 *  @author AonoZan Dejan Petrovic 2016 ©
 */
public class UserDatabase<K, V> extends HashMap<K, V> implements Serializable{
	private static final long serialVersionUID = 7064318736599431089L;
	
	public String listAll() {
		StringBuilder all = new StringBuilder();
		for (Object ob : this.values()) {
			all.append(ob);
		}
		
		return all.toString();
	}
}

