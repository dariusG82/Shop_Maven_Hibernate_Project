package dariusG82.custom_exeptions;

public class UserNotFoundException extends Exception {

    public UserNotFoundException() {
        super("User cannot be found");
    }
}
