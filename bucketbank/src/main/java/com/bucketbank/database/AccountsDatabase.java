package com.bucketbank.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

public class AccountsDatabase {
    private final Connection connection;

    private String generateAccountId() {
        double rawId = Math.floor((Math.random() * (999999 - 100000) + 100000));
        String accountIdNumber = String.valueOf(rawId);
        try {
            if (!accountExists(accountIdNumber)) {
                return accountIdNumber;
            } else {
                return generateAccountId();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    };

    public AccountsDatabase(String path) throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);
        // Accounts Table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                "accountId TEXT PRIMARY KEY, " +
                "ownerUUID TEXT NOT NULL, " +
                "balance INTEGER NOT NULL DEFAULT 0, " +
                "suspended BOOL DEFAULT 0" +
                ")");
        }

        // Access Table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS account_access (" +
            "accountId TEXT NOT NULL, " +
            "userUUID TEXT NOT NULL, " +
            "PRIMARY KEY (accountId, userUUID), " +
            "FOREIGN KEY (accountId) REFERENCES accounts(accountId)" +
            ")");
        }
    };

    public void closeConnection() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    };

    // constructor
    public String createAccount(String accountOwnerUUID) throws Exception {
        String accountId = generateAccountId();
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO accounts (accountId, ownerUUID) VALUES (?, ?)")) {
            preparedStatement.setString(1, accountId);
            // preparedStatement.setString(2, accountOwner.getUniqueId().toString());
            preparedStatement.setString(2, accountOwnerUUID);
            preparedStatement.executeUpdate();

            return accountId;
        }
    };

    // check if exists
    public boolean accountExists(String accountId) throws Exception {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM accounts WHERE accountId = ?")) {
            preparedStatement.setString(1, accountId);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            return resultSet.next();
        }
    };

    // setters
    public void setBalance(String accountId, int balance) throws Exception {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET balance = ? WHERE accountId = ?")) {
                preparedStatement.setInt(1, balance);
                preparedStatement.setString(2, accountId);
                preparedStatement.executeUpdate();
            }
        }
    };

    public void setOwner(String accountId, String uuid) throws Exception {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET ownerUUID = ? WHERE accountId = ?")) {
                preparedStatement.setString(1, uuid);
                preparedStatement.setString(2, accountId);
                preparedStatement.executeUpdate();
            }
        }
    }

    public void setSuspendedStatus(String accountId, boolean freezed) throws Exception {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET suspended = ? WHERE accountId = ?")) {
                preparedStatement.setBoolean(1, freezed);
                preparedStatement.setString(2, accountId);
                preparedStatement.executeUpdate();
            }
        }
    }

    // getters
    public int getBalance(String accountId) throws Exception {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT balance FROM accounts WHERE accountId = ?")) {
                preparedStatement.setString(1, accountId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getInt("balance");
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }

    public String getOwner(String accountId) throws Exception {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ownerUUID FROM accounts WHERE accountId = ?")) {
                preparedStatement.setString(1, accountId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getString("ownerUUID");
                } else {
                    return "";
                }
            }
        } else {
            return "";
        }
    }

    public boolean getSuspendedStatus(String accountId) throws Exception {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT suspended FROM accounts WHERE accountId = ?")) {
                preparedStatement.setString(1, accountId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getBoolean("suspended");
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    // get all
    public Map<String, Object> getData(String accountId) throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("uuid", getOwner(accountId));
        data.put("balance", getBalance(accountId));
        data.put("suspended", getSuspendedStatus(accountId));
        return data;
    }

    // access management
    public void addAccessToAccount(String accountId, String uuid) throws Exception {
        String sql = "INSERT INTO account_access (accountId, userUUID) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, uuid);
            preparedStatement.executeUpdate();
        }
    }

    public boolean hasAccessToAccount(String accountId, String uuid) throws Exception {
        String sql = "SELECT COUNT(*) FROM account_access WHERE accountId = ? AND userUUID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, uuid);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    public void removeAccessFromAccount(String accountId, String userUUID) throws Exception {
        String sql = "DELETE FROM account_access WHERE accountId = ? AND userUUID = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, userUUID);
            preparedStatement.executeUpdate();
        }
    }

    public List<String> getAllUsersWithAccess(String accountId, String ownerUUID) throws Exception {
        String sql = "SELECT userUUID FROM account_access WHERE accountId = ? AND userUUID != ?";
        List<String> userUUIDs = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, ownerUUID);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    userUUIDs.add(resultSet.getString("userUUID"));
                }
            }
        }
        return userUUIDs;
    }
}
