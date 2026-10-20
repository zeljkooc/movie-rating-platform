package student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenresOperations implements rs.ac.bg.etf.sab.operations.GenresOperations{

    private Connection otvoriKonekciju() {
        try {
            return DB.getInstance().getConn();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public Integer addGenre(String name) {

        Connection conn = otvoriKonekciju();

        if(doesGenreExist(name)==true)
                return null;

        String query = "INSERT INTO Zanr(naziv) VALUES (?)";

        try{
            PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1,name);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if(rs.next())
                return rs.getInt(1);
        } catch(SQLException ex){
            Logger.getLogger(GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public boolean doesGenreExist(String name){
        Connection conn = otvoriKonekciju();
        String query = "SELECT idZanr FROM Zanr WHERE naziv=?";

        try{
            PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1,name);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch(SQLException ex) {
            Logger.getLogger(GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    @Override
    public List<Integer> getAllGenreIds()  {
        Connection conn = otvoriKonekciju();
        String query = "SELECT idZanr FROM Zanr";

        List<Integer> idList = new ArrayList<>();

        try{
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                idList.add(rs.getInt(1));
            }
            return idList;
        } catch(SQLException ex) {
            Logger.getLogger(GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return idList;
    }

    @Override
    public Integer getGenreId(String name) {
        Connection conn = otvoriKonekciju();
        String query = "SELECT idZanr FROM Zanr WHERE naziv=?";

        try{
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1,name);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                return rs.getInt(1);
        } catch(SQLException ex) {
            Logger.getLogger(GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public Integer removeGenre(Integer id){
        Connection conn = otvoriKonekciju();
        String query = "DELETE FROM Zanr WHERE idZanr=?";

        try{
            PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1,id);

            int obrisaniRedovi = ps.executeUpdate();
            if(obrisaniRedovi>0)
                return id;
        } catch(SQLException ex) {
            Logger.getLogger(GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }


        return null;
    }

    @Override
    public Integer updateGenre(Integer id,String newName) {
        Connection conn = otvoriKonekciju();

        String query = "UPDATE Zanr SET naziv=? WHERE idZanr=?";

        try{
            PreparedStatement ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1,newName);
            ps.setInt(2,id);

            int brojUpdated = ps.executeUpdate();

            if(brojUpdated>0)
                return id;
        } catch(SQLException ex){
            Logger.getLogger(GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static void main(String[] args) throws SQLException {
        //System.out.println(removeGenre(5));

        GenresOperations operacije = new GenresOperations();
        for(int i=0;i<2;++i){
            System.out.println(operacije.addGenre("Horor3"));
        }

       // updateGenre(3,"Romanticni");

//        System.out.println(addGenre("Horor"));

    }

}
