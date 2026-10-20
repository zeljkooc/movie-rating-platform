package student;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WatchlistsOperations implements rs.ac.bg.etf.sab.operations.WatchlistsOperations{

    private Connection otvoriKonekciju() {
        try {
            return DB.getInstance().getConn();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public boolean addMovieToWatchlist(Integer userId, Integer movieId) {
        Connection conn = otvoriKonekciju();
        if (conn == null) return false;

        String provera = "SELECT COUNT(*) FROM ListaGledanja WHERE idKorisnik = ? AND idFilm = ?";
        String upis = "INSERT INTO ListaGledanja(idKorisnik, idFilm, status) VALUES (?, ?, 'Gledace')";

        try {
            try (PreparedStatement ps = conn.prepareStatement(provera)) {
                ps.setInt(1, userId);
                ps.setInt(2, movieId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false;
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(upis)) {
                ps.setInt(1, userId);
                ps.setInt(2, movieId);
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException ex) {
            Logger.getLogger(WatchlistsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public List<Integer> getMoviesInWatchlist(Integer userId) {
        Connection conn = otvoriKonekciju();
        List<Integer> list = new ArrayList<>();
        if (conn == null) return list;

        String query = "SELECT idFilm FROM ListaGledanja WHERE idKorisnik = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(WatchlistsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    @Override
    public List<Integer> getUsersWithMovieInWatchlist(Integer movieId) {
        Connection conn = otvoriKonekciju();
        List<Integer> list = new ArrayList<>();
        if (conn == null) return list;

        String query = "SELECT idKorisnik FROM ListaGledanja WHERE idFilm = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(WatchlistsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    @Override
    public boolean isMovieInWatchlist(Integer userId, Integer movieId) {
        Connection conn = otvoriKonekciju();
        if (conn == null) return false;

        String query = "SELECT 1 FROM ListaGledanja WHERE idKorisnik = ? AND idFilm = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            Logger.getLogger(WatchlistsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean removeMovieFromWatchlist(Integer userId, Integer movieId) {
        Connection conn = otvoriKonekciju();
        if (conn == null) return false;

        String query = "DELETE FROM ListaGledanja WHERE idKorisnik = ? AND idFilm = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException ex) {
            Logger.getLogger(WatchlistsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

}
