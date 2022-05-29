package dariusG82.data.interfaces;

import dariusG82.custom_exeptions.UserNotFoundException;
import dariusG82.users.User;
import dariusG82.users.UserType;

import java.io.IOException;

public interface AdminInterface {

    boolean isUsernameUnique(String username);

    void addNewUser(User user) throws IOException;

    void removeUser(String username) throws UserNotFoundException, IOException;

    User getUserByType(String username, String password, UserType type) throws UserNotFoundException;

    User getUserByUsername(String username);
}
