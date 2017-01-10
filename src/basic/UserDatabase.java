
package basic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 *  @author AonoZan Dejan Petrovic 2016 ©
 */
public class UserDatabase<T> extends ArrayList<T> implements Serializable{
	private static final long serialVersionUID = 7064318736599431089L;

	public static final String DATABASE_LOC = System.getProperty("user.dir")
			+ "/storage/users_database/default.dat";
	
	public UserDatabase<T> load() {
		return load(DATABASE_LOC);
	}
	@SuppressWarnings("unchecked")
	public UserDatabase<T> load(String path) {
		UserDatabase<T> database = null;

		try (ObjectInputStream objectStream = new ObjectInputStream(Files.newInputStream(Paths.get(path)))) {
			database = (UserDatabase<T>) objectStream.readObject();
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
	
}

