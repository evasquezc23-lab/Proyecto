package gt.taller.dao;
import gt.taller.config.Database;
import java.sql.*;
import java.util.*;
public class GenericTableDao {
 public List<String[]> query(String sql,int columns)throws SQLException{
  List<String[]> rows=new ArrayList<>();
  try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
   while(r.next()){String[] row=new String[columns];for(int i=0;i<columns;i++)row[i]=r.getString(i+1);rows.add(row);}
  }return rows;
 }
 public int execute(String sql,Object... values)throws SQLException{
  try(Connection c=Database.getConnection();PreparedStatement p=c.prepareStatement(sql)){
   for(int i=0;i<values.length;i++)p.setObject(i+1,values[i]);return p.executeUpdate();
  }
 }
}
