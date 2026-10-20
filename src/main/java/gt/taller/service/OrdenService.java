package gt.taller.service;
import gt.taller.config.Database;
import java.sql.*;
public class OrdenService {
 public void agregarRepuesto(int ordenId,int repuestoId,int cantidad)throws SQLException{
  if(cantidad<=0)throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
  try(Connection c=Database.getConnection()){c.setAutoCommit(false);try{
   String estado;try(PreparedStatement p=c.prepareStatement("SELECT estado FROM ordenes_trabajo WHERE id_orden=? FOR UPDATE")){p.setInt(1,ordenId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("No existe la orden.");estado=r.getString(1);}}
   if(!"EN PROCESO".equals(estado))throw new SQLException("La orden debe estar EN PROCESO.");
   double precio;try(PreparedStatement p=c.prepareStatement("SELECT precio FROM repuestos WHERE id_repuesto=? AND existencia>=? FOR UPDATE")){p.setInt(1,repuestoId);p.setInt(2,cantidad);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("Repuesto inexistente o inventario insuficiente.");precio=r.getDouble(1);}}
   try(PreparedStatement p=c.prepareStatement("UPDATE repuestos SET existencia=existencia-? WHERE id_repuesto=?")){p.setInt(1,cantidad);p.setInt(2,repuestoId);p.executeUpdate();}
   try(PreparedStatement p=c.prepareStatement("MERGE INTO orden_repuestos d USING (SELECT ? id_orden, ? id_repuesto FROM dual) s ON(d.id_orden=s.id_orden AND d.id_repuesto=s.id_repuesto) WHEN MATCHED THEN UPDATE SET d.cantidad=d.cantidad+? WHEN NOT MATCHED THEN INSERT(id_orden,id_repuesto,cantidad,precio_aplicado) VALUES(?,?,?,?)")){
    p.setInt(1,ordenId);p.setInt(2,repuestoId);p.setInt(3,cantidad);p.setInt(4,ordenId);p.setInt(5,repuestoId);p.setInt(6,cantidad);p.setDouble(7,precio);p.executeUpdate();}
   try(PreparedStatement p=c.prepareStatement("UPDATE ordenes_trabajo SET total=total+? WHERE id_orden=?")){p.setDouble(1,precio*cantidad);p.setInt(2,ordenId);p.executeUpdate();}
   c.commit();
  }catch(Exception e){c.rollback();if(e instanceof SQLException)throw(SQLException)e;throw new SQLException(e);}finally{c.setAutoCommit(true);}}
 }
 public void cancelarOrden(int ordenId)throws SQLException{
  try(Connection c=Database.getConnection()){c.setAutoCommit(false);try{
   String estado;try(PreparedStatement p=c.prepareStatement("SELECT estado FROM ordenes_trabajo WHERE id_orden=? FOR UPDATE")){p.setInt(1,ordenId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("No existe la orden.");estado=r.getString(1);}}
   if("FINALIZADA".equals(estado)||"CANCELADA".equals(estado))throw new SQLException("La orden ya está cerrada.");
   try(PreparedStatement p=c.prepareStatement("UPDATE repuestos r SET existencia=existencia+(SELECT NVL(SUM(x.cantidad),0) FROM orden_repuestos x WHERE x.id_repuesto=r.id_repuesto AND x.id_orden=?) WHERE EXISTS(SELECT 1 FROM orden_repuestos x WHERE x.id_repuesto=r.id_repuesto AND x.id_orden=?)")){p.setInt(1,ordenId);p.setInt(2,ordenId);p.executeUpdate();}
   try(PreparedStatement p=c.prepareStatement("UPDATE ordenes_trabajo SET estado='CANCELADA' WHERE id_orden=?")){p.setInt(1,ordenId);p.executeUpdate();}c.commit();
  }catch(Exception e){c.rollback();if(e instanceof SQLException)throw(SQLException)e;throw new SQLException(e);}finally{c.setAutoCommit(true);}}
 }
}
