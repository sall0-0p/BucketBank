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

import com.bucketbank.App;
import com.bucketbank.modules.DatabaseManager;

public class UsersDatabase {
    private final Connection connection;
    private static final App plugin = App.getPlugin();
    private static DatabaseManager databaseManager = App.getDatabaseManager();

    public UsersDatabase(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        // Users table
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS users (" +
                "userId TEXT NOT NULL PRIMARY KEY, " +
                "username TEXT NOT NULL, " +
                "profileCreatedTimestamp NOT NULL BIGINT, " +
                "personalAccountId TEXT NOT NULL, " +
                "suspended BOOL DEFAULT 0, " +
                "debt INT DEFAULT 0, " +
                "deleted BOOL DEFAULT 0" +
            ")");
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    };

    public void createUser(String userId) throws SQLException {
        String sql = "INSERT INTO users (userId, username, profileCreatedTimestamp, personalAccountId) VALUES (?, ?, ?, ?)";
        String username = "_lordBucket";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            long unixTimestamp = (System.currentTimeMillis() / 1000L);
            String personalAccountId = databaseManager.getAccountsDatabase().createAccount(username);

            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, username);
            preparedStatement.setLong(3, unixTimestamp);

            preparedStatement.setString(4, personalAccountId);
            preparedStatement.executeUpdate();
        }
    }

    public void deleteUser(String userId) throws SQLException {
        String sql = "UPDATE accounts SET deleted = ? WHERE accountId = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setBoolean(1, true);
            preparedStatement.setString(2, userId);
            preparedStatement.executeUpdate();
        }
    }

    public boolean userExists(String userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE userId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next();
        }
    }

    // setters
    public void setSuspendedStatus(String userId, boolean freezed) throws SQLException {
        String sql = "UPDATE users SET suspended = ? WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setBoolean(1, freezed);
                preparedStatement.setString(2, userId);
                preparedStatement.executeUpdate();
            }
        }
    }

    // excludes loans and credit card debt!
    public void setDebt(String userId, int debtAmount) throws SQLException {
        String sql = "UPDATE users SET debt = ? WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setInt(1, debtAmount);
                preparedStatement.setString(2, userId);
                preparedStatement.executeUpdate();
            }
        }
    }
    // getters
    public String getUsername(String userId) throws SQLException {
        String sql = "SELECT username FROM users WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getString("username");
                } else {
                    return "";
                }
            }
        } else {
            return "";
        }
    }

    public long getCreationTimestamp(String userId) throws SQLException {
        String sql = "SELECT profileCreatedTimestamp FROM users WHERE userId = ?";

        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getLong("profileCreatedTimestamp");
                } else {
                    return (long) 0;
                }
            }
        } else {
            return (long) 0;
        }
    }

    public String getPersonalAccountId(String userId) throws SQLException {
        String sql = "SELECT personalAccountId FROM users WHERE userId = ?";

        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getString("personalAccountId");
                } else {
                    return "";
                }
            }
        } else {
            return "";
        }
    }

    public boolean getSuspendedStatus(String userId) throws SQLException {
        String sql = "SELECT suspended FROM users WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userId);
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

    public int getDebt(String userId) throws SQLException {
        String sql = "SELECT debt FROM users WHERE userId = ?";
        if (userExists(userId)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userId);
                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getInt("suspended");
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }

    public List<String> getAllAccessibleAccounts(String accountId, String ownerId) throws SQLException {
        String sql = "SELECT accountId FROM account_access WHERE userId = ?";
        List<String> userIds = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, accountId);
            preparedStatement.setString(2, ownerId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    userIds.add(resultSet.getString("userId"));
                }
            }
        }
        return userIds;
    }

    // get all
    public Map<String, Object> getData(String userId) throws SQLException {
        Map<String, Object> data = new HashMap<String, Object>();

        data.put("uuid", userId);
        data.put("username", getUsername(userId));
        data.put("creatingEpoch", getCreationTimestamp(userId));
        data.put("suspended", getSuspendedStatus(userId));
        data.put("personalAccountId", getPersonalAccountId(userId));
        data.put("debt", getDebt(userId));

        return data;
    }

}
