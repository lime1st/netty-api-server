package lime1st.netty.domain;

import java.sql.*;

public class UserRepository {

    private final Connection conn;

    public UserRepository() {
        try {
            this.conn = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
            initTable();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to H2", e);
        }
    }

    private void initTable() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (id BIGINT AUTO_INCREMENT, email VARCHAR(255), username VARCHAR(255), password VARCHAR(255))");
        }
    }

    public void insertUser(Long id, String email, String username, String password) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "MERGE INTO users KEY(id) VALUES (?, ?, ?, ?)")) {
            stmt.setLong(1, id);
            stmt.setString(2, email);
            stmt.setString(3, username);
            stmt.setString(4, password);
            stmt.executeUpdate();
        }
    }

    public User findByEmail(String email) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?")) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getLong("id"), rs.getString("email"), rs.getString("username"), rs.getString("password"));
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }
    }

    public User findByPassword(String password) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE password = ?")) {
            stmt.setString(1, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getLong("id"), rs.getString("email"), rs.getString("username"), rs.getString("password"));
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by password", e);
        }
    }
}
