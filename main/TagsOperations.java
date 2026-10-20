package student;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TagsOperations implements rs.ac.bg.etf.sab.operations.TagsOperations {

    private Connection otvoriKonekciju() {
        try {
            return DB.getInstance().getConn();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Integer addTag(Integer movieId, String tag) {
        Connection conn = otvoriKonekciju();
        if (conn == null) return null;

        String proveraFilma = "SELECT COUNT(*) FROM Film WHERE idFilm = ?";
        String proveraVeza = "SELECT COUNT(*) FROM Tag t JOIN FilmTag ft ON t.idTag = ft.idTag WHERE ft.idFilm = ? AND t.naziv = ?";
        String dohvatiTag = "SELECT idTag FROM Tag WHERE naziv = ?";
        String ubaciTag = "INSERT INTO Tag (naziv) VALUES (?)";
        String ubaciFilmTag = "INSERT INTO FilmTag (idFilm, idTag) VALUES (?, ?)";

        try {
            try (PreparedStatement ps = conn.prepareStatement(proveraFilma)) {
                ps.setInt(1, movieId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) return null;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(proveraVeza)) {
                ps.setInt(1, movieId);
                ps.setString(2, tag);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return null;
                }
            }

            int tagId = -1;
            try (PreparedStatement ps = conn.prepareStatement(dohvatiTag)) {
                ps.setString(1, tag);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tagId = rs.getInt(1);
                    }
                }
            }

            if (tagId == -1) {
                try (PreparedStatement ps = conn.prepareStatement(ubaciTag, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, tag);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            tagId = rs.getInt(1);
                        }
                    }
                }
            }

            if (tagId != -1) {
                try (PreparedStatement ps = conn.prepareStatement(ubaciFilmTag)) {
                    ps.setInt(1, movieId);
                    ps.setInt(2, tagId);
                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        return movieId;
                    }
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public List<String> getAllTags() {
        Connection conn = otvoriKonekciju();
        List<String> tagList = new ArrayList<>();
        if (conn == null) return tagList;

        String query = "SELECT DISTINCT naziv FROM Tag";

        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tagList.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            Logger.getLogger(TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return tagList;
    }

    @Override
    public List<Integer> getMovieIdsByTag(String tag) {
        Connection conn = otvoriKonekciju();
        List<Integer> idList = new ArrayList<>();
        if (conn == null) return idList;

        String query = "SELECT ft.idFilm FROM Tag t JOIN FilmTag ft ON t.idTag = ft.idTag WHERE t.naziv = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tag);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    idList.add(rs.getInt(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return idList;
    }

    @Override
    public List<String> getTagsForMovie(Integer movieId) {
        Connection conn = otvoriKonekciju();
        List<String> tagList = new ArrayList<>();
        if (conn == null) return tagList;

        String query = "SELECT t.naziv FROM Tag t JOIN FilmTag ft ON t.idTag = ft.idTag WHERE ft.idFilm = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tagList.add(rs.getString(1));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return tagList;
    }

    @Override
    public boolean hasTag(Integer movieId, String tag) {
        Connection conn = otvoriKonekciju();
        if (conn == null) return false;

        String query = "SELECT 1 FROM Tag t JOIN FilmTag ft ON t.idTag = ft.idTag WHERE ft.idFilm = ? AND t.naziv = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            ps.setString(2, tag);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            Logger.getLogger(TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    @Override
    public int removeAllTagsForMovie(Integer movieId) {
        Connection conn = otvoriKonekciju();
        if (conn == null) return 0;

        String query = "DELETE FROM FilmTag WHERE idFilm = ?";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            return ps.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    @Override
    public Integer removeTag(Integer movieId, String tag) {
        Connection conn = otvoriKonekciju();
        if (conn == null) return null;

        String query = "DELETE FROM FilmTag WHERE idFilm = ? AND idTag = (SELECT idTag FROM Tag WHERE naziv = ?)";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, movieId);
            ps.setString(2, tag);
            int rowsDeleted = ps.executeUpdate();

            if (rowsDeleted > 0) {
                return movieId;
            }
        } catch (SQLException ex) {
            Logger.getLogger(TagsOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}