package com.dottstudio.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SpecialtyDAO {

    public static List<String> getAllSpecialties() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT SpecialtyName FROM SPECIALTIES ORDER BY SpecialtyName";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("SpecialtyName"));
            }
        }
        return list;
    }
}
