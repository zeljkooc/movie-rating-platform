package student;


import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GeneralOperations implements rs.ac.bg.etf.sab.operations.GeneralOperations {

    private Connection otvoriKonekciju() {
        try {
            return DB.getInstance().getConn();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void eraseAll() {
        Connection conn=otvoriKonekciju();

        String[] tabele = {"FilmTag","ListaGledanja","Ocena","Pripada","Film","Korisnik","Zanr","Tag"};

        try{
            Statement s = conn.createStatement();
            for(String tabela : tabele){
                s.executeUpdate("DELETE FROM " + tabela);;
            }
        } catch(SQLException ex) {
            Logger.getLogger(GenresOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
