
package gt.taller.dao;

import gt.taller.config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenericTableDao {

    public List<String[]> query(String sql, int columns)
            throws SQLException {

        List<String[]> rows = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                String[] row = new String[columns];

                for (int i = 0; i < columns; i++) {
                    row[i] = result.getString(i + 1);
                }

                rows.add(row);
            }
        }

        return rows;
    }

    public int execute(String sql, Object... values)
            throws SQLException {

        try (Connection connection = Database.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }

            return statement.executeUpdate();
        }
    }
}
