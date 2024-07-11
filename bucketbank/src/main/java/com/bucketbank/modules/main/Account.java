package com.bucketbank.modules.main;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.bucketbank.App;
import com.bucketbank.database.AccountsDatabase;
import com.bucketbank.modules.DatabaseManager;

// data.put("uuid", getOwner(accountId));
// data.put("balance", getBalance(accountId));
// data.put("suspended", getSuspendedStatus(accountId));
// data.put("deleted", isDeleted(accountId));

public class Account {
    private static final App plugin = App.getPlugin();
    private static final Logger logger = plugin.getLogger();
    private static DatabaseManager databaseManager = plugin.getDatabaseManager();
    private static AccountsDatabase accountsDatabase = databaseManager.getAccountsDatabase();

    private String accountId;
    private String ownerId;
    private String displayName;
    private int balance;
    private boolean suspended;
    private boolean deleted;

    // constructor (load from database)
    public Account(String newAccountId) {
        try {
            Map<String, Object> accountData = accountsDatabase.getData(newAccountId);

            this.accountId = newAccountId;
            this.ownerId = (String) accountData.get("uuid");
            this.balance = (Integer) accountData.get("balance");
            this.displayName = (String) accountData.get("displayName");
            this.suspended = (Boolean) accountData.get("suspended");
            this.deleted = (Boolean) accountData.get("deleted");
        } catch (SQLException e) {
            logger.severe("Failed to load account " + this.accountId);;
            e.printStackTrace();
        }
        
    }

    // constructor (create new)
    public Account(String userId, boolean createNewAccount) {
        try {
            this.accountId = accountsDatabase.createAccount(userId);

            this.ownerId = userId;
            this.balance = accountsDatabase.getBalance(this.accountId);
            this.displayName = accountsDatabase.getDisplayName(this.accountId);
            this.suspended = accountsDatabase.getSuspendedStatus(this.accountId);
            this.deleted = accountsDatabase.isDeleted(this.accountId);
        } catch (SQLException e) {
            logger.severe("Failed to create account " + this.accountId);;
            e.printStackTrace();
        }
    }

    // getters

    public String getAccountId() {
        return this.accountId;
    }

    public String getOwnerId() {
        try {
            return accountsDatabase.getOwner(this.accountId);
        } catch (SQLException e) {
            logger.severe("Failed to get owner of account " + this.accountId);;
            e.printStackTrace();
            return "";
        }
    }

    public String getDisplayName() {
        try {
            return accountsDatabase.getDisplayName(this.accountId);
        } catch (SQLException e) {
            logger.severe("Failed to get display name of account " + this.accountId);;
            e.printStackTrace();
            return "Account";
        }
    }

    public Integer getBalance() {
        try {
            return accountsDatabase.getBalance(this.accountId);
        } catch (SQLException e) {
            return (Integer) 0;
        }
    }

    public boolean isPersonal() {
        try {
            return accountsDatabase.isPersonal(this.accountId);
        } catch (SQLException e) {
            logger.severe("Failed to check if account is personal:  " + this.accountId);;
            e.printStackTrace();
            return false;
        }
    }

    public boolean isSuspended() {
        try {
            User owner = new User(this.ownerId);
            return accountsDatabase.getSuspendedStatus(this.accountId) || owner.isSuspended();
        } catch (Exception e) {
            logger.severe("Failed to check if account is suspended:  " + this.accountId);;
            e.printStackTrace();
            return false;
        }
    }

    public boolean isDeleted() {
        try {
            return accountsDatabase.isDeleted(this.accountId);
        } catch (SQLException e) {
            logger.severe("Failed to check if account is suspended:  " + this.accountId);;
            e.printStackTrace();
            return false;
        }
    }

    // setters

    public void modifyBalance(Integer amount) throws Exception {
        try {
            // Checks
            if (isSuspended()) {
                throw new Exception("Account is suspended, cannot modify balance!");
            }

            if (isDeleted()) {
                throw new Exception("Account is deleted, cannot modify balance!");
            }

            // logger.info(String.valueOf(getBalance()));
            if (getBalance() + amount < 0) {
                throw new Exception("Insufficient funds. Cannot modify balance.");
            }

            // Update balance
            this.balance += amount;
            accountsDatabase.setBalance(this.accountId, this.balance);
        } catch (SQLException e) {
            logger.severe("Failed to modify balance:  " + this.accountId + " by " + amount);
            e.printStackTrace();
        }
    }

    public void setBalance(Integer balance) throws Exception {
        try {
            // Checks
            if (isDeleted()) {
                throw new Exception("Account is deleted, cannot modify balance!");
            }

            // Update balance
            this.balance = balance;
            accountsDatabase.setBalance(this.accountId, this.balance);
        } catch (SQLException e) {
            logger.severe("Failed to set balance:  " + this.accountId + " by " + balance);
            e.printStackTrace();
        }
    }

    public void setDisplayName(String name) {
        try {
            accountsDatabase.setDisplayName(this.accountId, name);
        } catch (SQLException e) {
            logger.severe("Failed to set display name of account " + this.accountId);;
            e.printStackTrace();
        }
    }

    public void changeOwner(User user) {
        try {
            this.ownerId = user.getUserId(); 
            accountsDatabase.setOwner(this.accountId, this.ownerId);
        } catch (SQLException e) {
            logger.severe("Failed to modify owner:  " + this.accountId);
        }
    }

    // develop suspensions system
    public void suspend() {
        try {
            accountsDatabase.setSuspendedStatus(this.accountId, true);
            this.suspended = true;
        } catch (SQLException e) {
            logger.severe("Failed to suspend account " + this.accountId);
            e.printStackTrace();
        }
    }

    public void reinstate() {
        try {
            accountsDatabase.setSuspendedStatus(this.accountId, true);
            this.suspended = false;
        } catch (SQLException e) {
            logger.severe("Failed to suspend account " + this.accountId);
            e.printStackTrace();
        }
    }

    // access function
    public void addUser(User user) {
        try {
            accountsDatabase.addAccessToAccount(this.accountId, user.getUserId());
        } catch (SQLException e) {
            logger.severe("Failed to add user to account " + this.accountId + " | " + user.getUserId());
            e.printStackTrace();
        }
    }

    public void removeUser(User user) {
        try {
            accountsDatabase.removeAccessFromAccount(this.accountId, user.getUserId());
        } catch (SQLException e) {
            logger.severe("Failed to remove user from account " + this.accountId + " | " + user.getUserId());
            e.printStackTrace();
        }
    }

    public List<User> getUsers() {
        try {
            List<User> users = new ArrayList<>();
            List<String> userIds = accountsDatabase.getAllUsersWithAccess(this.accountId, this.ownerId);

            for (String userId : userIds) {
                try {
                    users.add(new User(userId));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return users;
        } catch (SQLException e) {
            logger.severe("Failed to get users from account " + this.accountId);
            e.printStackTrace();

            return new ArrayList<>();
        }
    }

    public boolean hasAccess(User user) {
        try {
            String userId = user.getUserId();

            return accountsDatabase.hasAccessToAccount(this.accountId, userId);
        } catch (SQLException e) {
            logger.severe("Failed to check if user has access to account " + this.accountId + " | " + user.getUserId());
            e.printStackTrace();

            return false;
        }
    }

    // delete function
    public void delete() {
        try {
            accountsDatabase.deleteAccount(this.accountId);
            this.deleted = true;
        } catch (SQLException e) {
            logger.severe("Failed to delete account " + this.accountId);
            e.printStackTrace();
        }
    }

    // exists

    public static boolean exists(String accountId) {
        try {
            return accountsDatabase.accountExists(accountId);
        } catch (SQLException e) {
            logger.severe("Failed to verify if account exists |" + accountId);
            e.printStackTrace();
            return false;
        }
    }
}
