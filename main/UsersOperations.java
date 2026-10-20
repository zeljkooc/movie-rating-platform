package org.example;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsersOperations implements rs.ac.bg.etf.sab.operations.UsersOperations {

    private Connection otvoriKonekciju() {
        try {
            return DB.getInstance().getConn();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Integer addUser(String username) {
        Connection conn = otvoriKonekciju();
        if (conn == null || username == null || username.trim().isEmpty()) return null;

        if (doesUserExist(username)) return null;

        String query = "INSERT INTO Korisnik(korisnickoIme, brojNagrada, opis) VALUES (?, 0, 'undefined')";

        try (PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
        }
        return null;
    }

    @Override
    public boolean doesUserExist(String username) {
        Connection conn = otvoriKonekciju();
        if (conn == null || username == null) return false;

        String query = "SELECT 1 FROM Korisnik WHERE korisnickoIme = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public List<Integer> getAllUserIds() {
        Connection conn = otvoriKonekciju();
        List<Integer> idList = new ArrayList<>();
        if (conn == null) return idList;

        String query = "SELECT idKorisnik FROM Korisnik";

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                idList.add(rs.getInt(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idList;
    }

    @Override
    public List<Integer> getRecommendedMoviesFromFavoriteGenres(Integer userId) {
        Connection conn = otvoriKonekciju();
        List<Integer> idList = new ArrayList<>();
        if (conn == null || userId == null) return idList;

        String query =
                "SELECT f.idFilm " +
                        "FROM Film f " +
                        "JOIN Pripada p ON f.idFilm = p.idFilm " +
                        "WHERE p.idZanr IN ( " +
                        "    SELECT p2.idZanr " +
                        "    FROM Ocena o " +
                        "    JOIN Pripada p2 ON o.idFilm = p2.idFilm " +
                        "    WHERE o.idKorisnik = ? " +
                        "    GROUP BY p2.idZanr " +
                        "    HAVING AVG(o.ocena * 1.0) >= 8.0 " +
                        ") " +
                        "AND NOT EXISTS (SELECT 1 FROM Ocena o2 WHERE o2.idFilm = f.idFilm AND o2.idKorisnik = ?) " +
                        "AND NOT EXISTS (SELECT 1 FROM ListaGledanja lg WHERE lg.idFilm = f.idFilm AND lg.idKorisnik = ?) " +
                        "AND ( " +
                        "    ((SELECT COUNT(*) FROM Ocena WHERE idFilm = f.idFilm) >= 4 AND f.prosecnaOcena >= 7.5) " +
                        "    OR " +
                        "    ((SELECT COUNT(*) FROM Ocena WHERE idFilm = f.idFilm) < 4 AND f.prosecnaOcena >= 9.0) " +
                        ") " +
                        "GROUP BY f.idFilm, f.prosecnaOcena " +
                        "ORDER BY f.prosecnaOcena DESC, f.idFilm ASC";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idList.add(rs.getInt("idFilm"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idList;
    }

    @Override
    public Integer getRewards(Integer userId) {
        Connection conn = otvoriKonekciju();
        if (conn == null || userId == null) return 0;

        String query = "SELECT brojNagrada FROM Korisnik WHERE idKorisnik = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("brojNagrada");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public List<String> getThematicSpecializations(Integer userId) {
        Connection conn = otvoriKonekciju();
        List<String> tagList = new ArrayList<>();
        if (conn == null || userId == null) return tagList;

        String query =
                "SELECT t.naziv " +
                        "FROM Tag t " +
                        "WHERE t.idTag IN ( " +
                        "    SELECT ft.idTag " +
                        "    FROM Ocena o " +
                        "    JOIN FilmTag ft ON o.idFilm = ft.idFilm " +
                        "    WHERE o.idKorisnik = ? AND o.ocena >= 8 " +
                        "    GROUP BY ft.idTag " +
                        "    HAVING COUNT(*) >= 2 " +
                        ")";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tagList.add(rs.getString("naziv"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tagList;
    }

    @Override
    public String getUserDescription(Integer userId) {
        Connection conn = otvoriKonekciju();
        if (conn == null || userId == null) return "undefined";

        String query = "SELECT opis FROM Korisnik WHERE idKorisnik = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String opis = rs.getString("opis");
                    return opis != null ? opis : "undefined";
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "undefined";
    }

    @Override
    public Integer getUserId(String username) {
        Connection conn = otvoriKonekciju();
        if (conn == null || username == null) return null;

        String query = "SELECT idKorisnik FROM Korisnik WHERE korisnickoIme = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Integer removeUser(Integer id) {
        Connection conn = otvoriKonekciju();
        if (conn == null || id == null) return null;

        String queryOcene = "DELETE FROM Ocena WHERE idKorisnik = ?";
        String queryLista = "DELETE FROM ListaGledanja WHERE idKorisnik = ?";
        String queryKorisnik = "DELETE FROM Korisnik WHERE idKorisnik = ?";

        try {
            try (PreparedStatement ps = conn.prepareStatement(queryOcene)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(queryLista)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(queryKorisnik)) {
                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    return id;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Integer updateUser(Integer id, String newUsername) {
        Connection conn = otvoriKonekciju();
        if (conn == null || id == null || newUsername == null) return null;

        String query = "UPDATE Korisnik SET korisnickoIme = ? WHERE idKorisnik = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, newUsername);
            ps.setInt(2, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                return id;
            }
        } catch (SQLException ex) {
            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}