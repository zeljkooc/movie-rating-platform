package student;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RatingsOperations implements rs.ac.bg.etf.sab.operations.RatingsOperations {

    private Connection otvoriKonekciju() {
        try {
            return DB.getInstance().getConn();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean addRating(Integer userId, Integer movieId, Integer score) {
        Connection conn = otvoriKonekciju();
        if (conn == null || userId == null || movieId == null || score == null) return false;

        if (getRating(userId, movieId) != null) {
            return false;
        }

        String query = "INSERT INTO Ocena(ocena, idKorisnik, idFilm, vreme) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, score);
            ps.setInt(2, userId);
            ps.setInt(3, movieId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (CallableStatement cs = conn.prepareCall("{CALL SP_REWARD_USER_MAIN(?, ?)}")) {
                    cs.setInt(1, userId);
                    cs.setInt(2, movieId);
                    cs.execute();
                }
                return true;
            }
        } catch (SQLException ex) {
            if (ex.getMessage() != null && (ex.getMessage().contains("blokiran") || ex.getMessage().contains("trigger"))) {
                return false;
            }
        }
        return false;
    }

    @Override
    public List<Integer> getRatedMoviesByUser(Integer userId) {
        Connection conn = otvoriKonekciju();
        List<Integer> idList = new ArrayList<>();
        if (conn == null) return idList;

        String query = "SELECT idFilm FROM Ocena WHERE idKorisnik = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idList.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RatingsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return idList;
    }

    @Override
    public Integer getRating(Integer userId, Integer movieId) {
        Connection conn = otvoriKonekciju();
        if (conn == null) return null;

        String query = "SELECT ocena FROM Ocena WHERE idKorisnik = ? AND idFilm = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RatingsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<Integer> getUsersWhoRatedMovie(Integer movieId) {
        Connection conn = otvoriKonekciju();
        List<Integer> idList = new ArrayList<>();
        if (conn == null) return idList;

        String query = "SELECT idKorisnik FROM Ocena WHERE idFilm = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idList.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RatingsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return idList;
    }

    @Override
    public boolean removeRating(Integer userId, Integer movieId) {
        Connection conn = otvoriKonekciju();
        if (conn == null) return false;

        String query = "DELETE FROM Ocena WHERE idKorisnik = ? AND idFilm = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);

            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            Logger.getLogger(RatingsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean updateRating(Integer userId, Integer movieId, Integer newScore) {
        Connection conn = otvoriKonekciju();
        if (conn == null || userId == null || movieId == null || newScore == null) return false;

        if (getRating(userId, movieId) == null) {
            return false;
        }

        String query = "UPDATE Ocena SET ocena = ?, vreme = CURRENT_TIMESTAMP WHERE idKorisnik = ? AND idFilm = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, newScore);
            ps.setInt(2, userId);
            ps.setInt(3, movieId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (CallableStatement cs = conn.prepareCall("{CALL SP_REWARD_USER_MAIN(?, ?)}")) {
                    cs.setInt(1, userId);
                    cs.setInt(2, movieId);
                    cs.execute();
                }
                return true;
            }
        } catch (SQLException ex) {
            if (ex.getMessage() != null && (ex.getMessage().contains("blokiran") || ex.getMessage().contains("trigger"))) {
                return false;
            }
        }
        return false;
    }
}