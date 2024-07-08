package com.bucketbank.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

public class AccountsDatabase {
    private final Connection connection;

    public AccountsDatabase(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        // Accounts Table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                "accountId TEXT PRIMARY KEY, " +
                "FOREIGN KEY (ownerId) REFERENCES users(userId), " +
                "balance INTEGER NOT NULL DEFAULT 0, " +
                "suspended BOOL DEFAULT 0, " +
                "deleted BOOL DEFAULT 0" +
                ")");
        }

        // Access Table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS account_access (" +
                "accountId TEXT NOT NULL, " +
                "userId TEXT NOT NULL, " +
                "PRIMARY KEY (accountId, userId), " +
                "FOREIGN KEY (accountId) REFERENCES accounts(accountId), " +
                "FOREIGN KEY (userId) REFERENCES users(userId)" +
                ")");
        }
    };

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    };

    // constructor
    public String createAccount(String accountOwnerId) throws SQLException {
        String accountId = generateAccountId();
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO accounts (accountId, ownerId) VALUES (?, ?)")) {
            preparedStatement.setString(1, accountId);
            // preparedStatement.setString(2, accountOwner.getUniqueId().toString());
            preparedStatement.setString(2, accountOwnerId);
            preparedStatement.executeUpdate();

            // adding user to people with access
            addAccessToAccount(accountId, accountOwnerId);

            return accountId;
        }
    };

    // delete account
    public void deleteAccount(String accountId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET deleted = ? WHERE accountId = ?")) {
            preparedStatement.setBoolean(1, true);
            preparedStatement.setString(2, accountId);
            preparedStatement.executeUpdate();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM account_access WHERE accountId = ?")) {
            preparedStatement.setString(1, accountId);
            preparedStatement.executeUpdate();
        }
    };

    // check if exists
    public boolean accountExists(String accountId) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM accounts WHERE accountId = ?")) {
            preparedStatement.setString(1, accountId);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            return resultSet.next();
        }
    };

    // setters
    public void setBalance(String accountId, int balance) throws SQLException {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET balance = ? WHERE accountId = ?")) {
                preparedStatement.setInt(1, balance);
                preparedStatement.setString(2, accountId);
                preparedStatement.executeUpdate();
            }
        }
    };

    public void setOwner(String accountId, String uuid) throws SQLException {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET ownerId = ? WHERE accountId = ?")) {
                preparedStatement.setString(1, uuid);
                preparedStatement.setString(2, accountId);
                preparedStatement.executeUpdate();
            }
        }
    }

    public void setSuspendedStatus(String accountId, boolean freezed) throws SQLException {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE accounts SET suspended = ? WHERE accountId = ?")) {
                preparedStatement.setBoolean(1, freezed);
                preparedStatement.setString(2, accountId);
                preparedStatement.executeUpdate();
            }
        }
    }

    // getters
    public int getBalance(String accountId) throws SQLException {
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

    public String getOwner(String accountId) throws SQLException {
        if (accountExists(accountId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ownerId FROM accounts WHERE accountId = ?")) {
                preparedStatement.setString(1, accountId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getString("ownerId");
                } else {
                    return "";
                }
            }
        } else {
            return "";
        }
    }

    public boolean getSuspendedStatus(String accountId) throws SQLException {
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
    public Map<String, Object> getData(String accountId) throws SQLException {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("uuid", getOwner(accountId));
        data.put("balance", getBalance(accountId));
        data.put("suspended", getSuspendedStatus(accountId));
        return data;
    }

    // access management
    public void addAccessToAccount(String accountId, String uuid) throws SQLException {
        String sql = "INSERT INTO account_access (accountId, userId) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, uuid);
            preparedStatement.executeUpdate();
        }
    }

    public boolean hasAccessToAccount(String accountId, String uuid) throws SQLException {
        String sql = "SELECT COUNT(*) FROM account_access WHERE accountId = ? AND userId = ?";
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

    public void removeAccessFromAccount(String accountId, String userId) throws SQLException {
        String sql = "DELETE FROM account_access WHERE accountId = ? AND userId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
        }
    }

    public List<String> getAllUsersWithAccess(String accountId, String ownerId) throws SQLException {
        String sql = "SELECT userId FROM account_access WHERE accountId = ?";
        List<String> userIds = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    userIds.add(resultSet.getString("userId"));
                }
            }
        }
        return userIds;
    }

    // private functions
    private String generateAccountId() {
        double rawId = Math.floor((Math.random() * (999999 - 100000) + 100000));
        String accountIdNumber = String.valueOf(rawId);
        try {
            if (!accountExists(accountIdNumber)) {
                return accountIdNumber;
            } else {
                return generateAccountId();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "";
        }
    };
}
