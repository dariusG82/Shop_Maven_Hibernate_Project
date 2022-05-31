package dariusG82.services.file_services;

import dariusG82.custom_exeptions.UserNotFoundException;
import dariusG82.custom_exeptions.WrongDataPathExeption;
import dariusG82.data.interfaces.AdminInterface;
import dariusG82.data.interfaces.FileReaderInterface;
import dariusG82.users.User;
import dariusG82.users.UserType;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static dariusG82.services.file_services.DataFileIndex.CURRENT_DATE;
import static dariusG82.services.file_services.DataPath.USERS_DATA_PATH;

public class AdminFileService implements AdminInterface, FileReaderInterface {

    @Override
    public boolean isUsernameUnique(String username) {
        List<User> users = getAllUsers();
        return users.stream()
                .noneMatch(user -> user.getUsername().equals(username));
    }

    @Override
    public User getUserByUsername(String username) {
        List<User> users = getAllUsers();

        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public User getUser(String username, String password, UserType type) throws UserNotFoundException {
        User user = getUserByUsername(username);

        if(user != null && user.getUserType().equals(type.toString()) && user.getPassword().equals(password)){
            return user;
        }

        throw new UserNotFoundException();
    }

    @Override
    public void addNewUser(User user) throws IOException {
        List<User> users = getAllUsers();

        users.add(user);

        updateAllUsers(users);
    }

    @Override
    public void removeUser(String username) throws UserNotFoundException, IOException {
        List<User> users = getAllUsers();

        if (isUserInDatabase(username)) {
            users.removeIf(user -> user.getUsername().equals(username));
            updateAllUsers(users);
            return;
        }

        throw new UserNotFoundException();
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

    @Override
    public List<User> getAllUsers() {
        try {
            Scanner scanner = new Scanner(new File(USERS_DATA_PATH.getPath()));
            ArrayList<User> users = new ArrayList<>();

            while (scanner.hasNext()) {
                String name = scanner.nextLine();
                String surname = scanner.nextLine();
                String username = scanner.nextLine();
                String password = scanner.nextLine();
                UserType type = getUserType(scanner);
                if (type != null) {
                    User user = new User(name, surname, username, password, type);
                    users.add(user);
                }
                scanner.nextLine();
            }

            return users;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private UserType getUserType(Scanner scanner) {
        String type = scanner.nextLine();
        try {
            return UserType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private boolean isUserInDatabase(String username) {
        List<User> users = getAllUsers();

        return users.stream()
                .anyMatch(user -> user.getUsername().equals(username));
    }

    private void updateAllUsers(List<User> users) throws IOException {
        PrintWriter printWriter = new PrintWriter(new FileWriter(USERS_DATA_PATH.getPath()));

        users.forEach(user -> {
            printWriter.println(user.getName());
            printWriter.println(user.getSurname());
            printWriter.println(user.getUsername());
            printWriter.println(user.getPassword());
            printWriter.println(user.getUserType());
            printWriter.println();
        });

        printWriter.close();
    }
}
