package com.bucketbank.modules;

import java.sql.SQLException;

import com.bucketbank.App;
import com.bucketbank.database.AccountsDatabase;
import com.bucketbank.database.UsersDatabase;

public class User {
    private static final App plugin = App.getPlugin();
    private static DatabaseManager databaseManager = plugin.getDatabaseManager();
    private static AccountsDatabase accountsDatabase = databaseManager.getAccountsDatabase();
    private static UsersDatabase usersDatabase = databaseManager.getUsersDatabase();

    private String userId;
    private String username;
    private long profileCreatedTimestamp;
    private String personalAccountId;
    private boolean suspended;
    private int debt;
    private boolean deleted;

    // constructor (load user)
    public void User(String userId) {

    }
    // constructor (create new user)
    public void User(String userId, boolean createNewUser) throws SQLException {
        
    }

    // getters
    
    // setters

    public void changeOwner(String newOwnerId) {

    }
}
