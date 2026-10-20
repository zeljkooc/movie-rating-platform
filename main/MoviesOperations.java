package student;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MoviesOperations implements rs.ac.bg.etf.sab.operations.MoviesOperations {

    private Connection otvoriKonekciju() {
        try {
            return DB.getInstance().getConn();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    @Override
    public Integer addGenreToMovie(Integer movieId, Integer genreId){
        Connection conn = otvoriKonekciju();
        if (conn == null) return null;

        String query = "SELECT COUNT(*) FROM Film WHERE idFilm = ?";
        String query1 = "SELECT COUNT(*) FROM Pripada WHERE idFilm = ? AND idZanr = ?";
        String query2 = "INSERT INTO Pripada(idFilm, idZanr) VALUES (?, ?)";

        try {
            try (PreparedStatement psCheckMovie = conn.prepareStatement(query)) {
                psCheckMovie.setInt(1, movieId);
                try (ResultSet rs = psCheckMovie.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        return null;
                    }
                }
            }

            try (PreparedStatement psCheckDup = conn.prepareStatement(query1)) {
                psCheckDup.setInt(1, movieId);
                psCheckDup.setInt(2, genreId);
                try (ResultSet rs = psCheckDup.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return null;
                    }
                }
            }

            try (PreparedStatement psInsert = conn.prepareStatement(query2)) {
                psInsert.setInt(1, movieId);
                psInsert.setInt(2, genreId);
                int affectedRows = psInsert.executeUpdate();
                if (affectedRows > 0) {
                    return movieId;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Integer addMovie(String title, Integer genreId, String director){
        Connection conn = otvoriKonekciju();
        if (conn == null) return null;

        String query = "INSERT INTO Film(naslov, reziser, status, prosecnaOcena) VALUES (?, ?, 'Inicijalan', 0.0)";

        try {
            int idFilm=-1;
            PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, title);
            ps.setString(2, director);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    idFilm = rs.getInt(1);
                }
            }
            if (idFilm != -1) {
                Integer genreResult = addGenreToMovie(idFilm, genreId);
                if (genreResult != null) {
                    return idFilm;
                }
            }
        } catch(SQLException ex){
            Logger.getLogger(MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<Integer> getAllMovieIds() {
        Connection conn = otvoriKonekciju();
        String query = "SELECT idFilm FROM Film";
        List<Integer> idList = new ArrayList<>();
        if (conn == null) return idList;

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                idList.add(rs.getInt(1));
            }
        } catch(SQLException ex) {
            Logger.getLogger(MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return idList;
    }

    @Override
    public List<Integer> getGenreIdsForMovie(Integer movieId) {
        Connection conn = otvoriKonekciju();
        String query = "SELECT idZanr FROM Pripada WHERE idFilm = ?";
        List<Integer> genreIds = new ArrayList<>();
        if (conn == null) return genreIds;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    genreIds.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return genreIds;
    }

    @Override
    public List<Integer> getMovieIds(String title, String director){
        Connection conn = otvoriKonekciju();
        List<Integer> idList = new ArrayList<>();
        if (conn == null) return idList;

        String query = "SELECT idFilm FROM Film WHERE naslov = ? AND reziser = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, title);
            ps.setString(2, director);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idList.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return idList;
    }

    @Override
    public List<Integer> getMovieIdsByDirector(String director) {
        Connection conn = otvoriKonekciju();
        String query = "SELECT idFilm FROM Film WHERE reziser = ?";
        List<Integer> idList = new ArrayList<>();
        if (conn == null) return idList;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, director);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idList.add(rs.getInt(1));
                }
            }
        } catch(SQLException ex) {
            Logger.getLogger(MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return idList;
    }

    @Override
    public List<Integer> getMovieIdsByGenre(Integer genreId)  {
        Connection conn = otvoriKonekciju();
        String query = "SELECT idFilm FROM Pripada WHERE idZanr = ?";
        List<Integer> idList = new ArrayList<>();
        if (conn == null) return idList;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, genreId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idList.add(rs.getInt(1));
                }
            }
        } catch(SQLException ex) {
            Logger.getLogger(MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return idList;
    }

    @Override
    public String getMovieTrend(Integer movieId) {
        Connection conn = otvoriKonekciju();
        String query = "SELECT status FROM Film WHERE idFilm = ?";
        if (conn == null) return null;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("status");
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Integer removeGenreFromMovie(Integer movieId, Integer genreId) {
        Connection conn = otvoriKonekciju();
        String query = "DELETE FROM Pripada WHERE idFilm = ? AND idZanr = ?";
        if (conn == null) return null;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            ps.setInt(2, genreId);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                return movieId;
            }
        } catch(SQLException ex) {
            Logger.getLogger(MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Integer removeMovie(Integer id) {
        Connection conn = otvoriKonekciju();
        String query = "DELETE FROM Film WHERE idFilm = ?";
        if (conn == null || id==null) return null;

        String checkQuery = "SELECT COUNT(*) FROM Film WHERE idFilm = ?";
        try (PreparedStatement psCheck = conn.prepareStatement(checkQuery)) {
            psCheck.setInt(1, id);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    return null;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        try {
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM Pripada WHERE idFilm = ?")) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM FilmTag WHERE idFilm = ?")) {
                ps2.setInt(1, id);
                ps2.executeUpdate();
            }

            try (PreparedStatement ps3 = conn.prepareStatement("DELETE FROM ListaGledanja WHERE idFilm = ?")) {
                ps3.setInt(1, id);
                ps3.executeUpdate();
            }

            try (PreparedStatement ps4 = conn.prepareStatement("DELETE FROM Ocena WHERE idFilm = ?")) {
                ps4.setInt(1, id);
                ps4.executeUpdate();
            }

            try (PreparedStatement ps5 = conn.prepareStatement("DELETE FROM Film WHERE idFilm = ?")) {
                ps5.setInt(1, id);
                if (ps5.executeUpdate() > 0) {
                    return id;
                }
            }
        } catch(SQLException ex) {
            Logger.getLogger(MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Integer updateMovieDirector(Integer id, String newDirector) {
        Connection conn = otvoriKonekciju();
        String query = "UPDATE Film SET reziser = ? WHERE idFilm = ?";
        if (conn == null) return null;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, newDirector);
            ps.setInt(2, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                return id;
            }
        } catch(SQLException ex){
            Logger.getLogger(MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public Integer updateMovieTitle(Integer id, String newTitle) {
        Connection conn = otvoriKonekciju();
        String query = "UPDATE Film SET naslov = ? WHERE idFilm = ?";
        if (conn == null) return null;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, newTitle);
            ps.setInt(2, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                return id;
            }
        } catch(SQLException ex){
            Logger.getLogger(MoviesOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }
}