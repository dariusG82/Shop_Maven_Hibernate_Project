package dariusG82.services.file_services;

import dariusG82.custom_exeptions.UserNotFoundException;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.data.interfaces.AdminInterface;
import dariusG82.data.interfaces.DataManagement;
import dariusG82.data.interfaces.FileReaderInterface;
import dariusG82.users.User;
import dariusG82.users.UserType;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static dariusG82.services.file_services.DataFileIndex.CURRENT_DATE;
import static dariusG82.services.file_services.DataPath.USERS_DATA_PATH;

public class AdminFileService implements AdminInterface, FileReaderInterface {

    private final DataManagement dataService;
    public AdminFileService(DataManagement dataService){
        this.dataService = dataService;
    }

    @Override
    public boolean isUsernameUnique(String username) {
        List<User> users = dataService.getAllUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addNewUser(User user) throws IOException {
        List<User> users = dataService.getAllUsers();

        users.add(user);

        updateAllUsers(users);
    }

    @Override
    public void removeUser(String username) throws UserNotFoundException, IOException {
        List<User> users = dataService.getAllUsers();

        if (isUserInDatabase(username)) {
            users.removeIf(user -> user.getUsername().equals(username));
            updateAllUsers(users);
            return;
        }

        throw new UserNotFoundException();
    }

    @Override
    public User getUserByType(String username, String password, UserType type) throws UserNotFoundException {
        User user = getUserByUsername(username);

        if(user != null && user.getUserType().equals(type.toString()) && user.getPassword().equals(password)){
            return user;
        }

        throw new UserNotFoundException();
    }

    @Override
    public User getUserByUsername(String username) {
        List<User> users = dataService.getAllUsers();

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public void updateCurrentDateInDataString(LocalDate currentDate) throws WrongDataPathExeption, IOException {
        ArrayList<String> datalist = reader.getDataStrings();

        if (datalist == null) {
            throw new WrongDataPathExeption();
        }

        for (int index = 0; index < datalist.size(); index++) {
            String data = datalist.get(index);
            if (data.startsWith(CURRENT_DATE.getIndex())) {
                String newData = data.substring(0, data.indexOf("-") + 1) + currentDate;
                datalist.set(index, newData);
                break;
            }
        }
        reader.updateDataStrings(datalist);
    }

    private boolean isUserInDatabase(String username) {
        List<User> users = dataService.getAllUsers();

        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    private void updateAllUsers(List<User> users) throws IOException {
        PrintWriter printWriter = new PrintWriter(new FileWriter(USERS_DATA_PATH.getPath()));

        for (User user : users) {
            printWriter.println(user.getName());
            printWriter.println(user.getSurname());
            printWriter.println(user.getUsername());
            printWriter.println(user.getPassword());
            printWriter.println(user.getUserType());
            printWriter.println();
        }

        printWriter.close();
    }
}