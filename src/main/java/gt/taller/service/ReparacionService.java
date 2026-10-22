
package gt.taller.service;

import gt.taller.config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.math.BigDecimal;

public class ReparacionService {

    public void iniciarOrden(int idOrden) throws SQLException {
  String sql =
    "UPDATE ordenes_trabajo " +
    "SET estado = 'EN PROCESO' " +
    "WHERE id_orden = ? AND estado = 'PENDIENTE'";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idOrden);

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                    "La orden no existe o no está PENDIENTE."
                );
            }
        }
    }

    public void agregarServicio(int idOrden, int idServicio)
            throws SQLException {

        String comprobar =
            "SELECT estado FROM ordenes_trabajo " +
            "WHERE id_orden = ? FOR UPDATE";

        String precio =
            "SELECT costo_mano_obra FROM servicios " +
            "WHERE id_servicio = ?";

        String insertar =
            "INSERT INTO orden_servicios " +
            "(id_orden, id_servicio, costo_aplicado) " +
            "VALUES (?, ?, ?)";

        String total =
            "UPDATE ordenes_trabajo SET total = (" +
            "SELECT NVL(SUM(costo_aplicado),0) " +
            "FROM orden_servicios WHERE id_orden = ?) + (" +
            "SELECT NVL(SUM(cantidad * precio_aplicado),0) " +
            "FROM orden_repuestos WHERE id_orden = ?) " +
            "WHERE id_orden = ?";

        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(comprobar)) {
                    ps.setInt(1, idOrden);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()
                                || !"EN PROCESO".equals(rs.getString(1))) {
                            throw new SQLException(
                                "La orden debe estar EN PROCESO."
                            );
                        }
                    }
                }

                BigDecimal costo;
                try (PreparedStatement ps = c.prepareStatement(precio)) {
                    ps.setInt(1, idServicio);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                "El servicio no existe."
                            );
                        }
                        costo = rs.getBigDecimal(1);
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(insertar)) {
                    ps.setInt(1, idOrden);
                    ps.setInt(2, idServicio);
                    ps.setBigDecimal(3, costo);
                    ps.executeUpdate();
                }

                actualizarTotal(c, total, idOrden);
                c.commit();

            } catch (Exception ex) {
                c.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException("No se pudo agregar el servicio.", ex);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void agregarRepuesto(int idOrden, int idRepuesto, int cantidad)
            throws SQLException {

        if (cantidad <= 0) {
            throw new SQLException("La cantidad debe ser mayor que cero.");
        }

        String orden =
            "SELECT estado FROM ordenes_trabajo " +
            "WHERE id_orden = ? FOR UPDATE";

        String inventario =
            "SELECT precio, existencia FROM repuestos " +
            "WHERE id_repuesto = ? FOR UPDATE";

        String descontar =
            "UPDATE repuestos SET existencia = existencia - ? " +
            "WHERE id_repuesto = ? AND existencia >= ?";

        String insertar =
            "INSERT INTO orden_repuestos " +
            "(id_orden, id_repuesto, cantidad, precio_aplicado) " +
            "VALUES (?, ?, ?, ?)";

        String total =
            "UPDATE ordenes_trabajo SET total = (" +
            "SELECT NVL(SUM(costo_aplicado),0) " +
            "FROM orden_servicios WHERE id_orden = ?) + (" +
            "SELECT NVL(SUM(cantidad * precio_aplicado),0) " +
            "FROM orden_repuestos WHERE id_orden = ?) " +
            "WHERE id_orden = ?";

        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(orden)) {
                    ps.setInt(1, idOrden);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()
                                || !"EN PROCESO".equals(rs.getString(1))) {
                            throw new SQLException(
                                "La orden debe estar EN PROCESO."
                            );
                        }
                    }
                }

                BigDecimal precio;
                try (PreparedStatement ps = c.prepareStatement(inventario)) {
                    ps.setInt(1, idRepuesto);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Repuesto inexistente.");
                        }

                        precio = rs.getBigDecimal("precio");
                        int existencia = rs.getInt("existencia");

                        if (existencia < cantidad) {
                            throw new SQLException(
                                "Existencias insuficientes. Disponibles: "
                                + existencia
                            );
                        }
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(descontar)) {
                    ps.setInt(1, cantidad);
                    ps.setInt(2, idRepuesto);
                    ps.setInt(3, cantidad);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                            "No fue posible actualizar el inventario."
                        );
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(insertar)) {
                    ps.setInt(1, idOrden);
                    ps.setInt(2, idRepuesto);
                    ps.setInt(3, cantidad);
                    ps.setBigDecimal(4, precio);
                    ps.executeUpdate();
                }

                actualizarTotal(c, total, idOrden);
                c.commit();

            } catch (Exception ex) {
                c.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException("No se pudo agregar el repuesto.", ex);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private void actualizarTotal(
            Connection c, String sql, int idOrden) throws SQLException {

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            ps.setInt(2, idOrden);
            ps.setInt(3, idOrden);

            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar el total.");
            }
        }
    }

    public void finalizarOrden(int idOrden) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                String comprobar =
                    "SELECT estado FROM ordenes_trabajo " +
                    "WHERE id_orden = ? FOR UPDATE";

                try (PreparedStatement ps = c.prepareStatement(comprobar)) {
                    ps.setInt(1, idOrden);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()
                                || !"EN PROCESO".equals(rs.getString(1))) {
                            throw new SQLException(
                                "La orden debe estar EN PROCESO."
                            );
                        }
                    }
                }

                String servicios =
                    "SELECT COUNT(*) FROM orden_servicios WHERE id_orden = ?";

                try (PreparedStatement ps = c.prepareStatement(servicios)) {
                    ps.setInt(1, idOrden);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) == 0) {
                            throw new SQLException(
                                "No se puede finalizar sin servicios."
                            );
                        }
                    }
                }

                String total =
                    "UPDATE ordenes_trabajo SET total = (" +
                    "SELECT NVL(SUM(costo_aplicado),0) " +
                    "FROM orden_servicios WHERE id_orden = ?) + (" +
                    "SELECT NVL(SUM(cantidad * precio_aplicado),0) " +
                    "FROM orden_repuestos WHERE id_orden = ?) " +
                    "WHERE id_orden = ?";

                actualizarTotal(c, total, idOrden);

                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE ordenes_trabajo SET estado = 'FINALIZADA' " +
                        "WHERE id_orden = ? AND estado = 'EN PROCESO'")) {
                    ps.setInt(1, idOrden);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("No se pudo finalizar la orden.");
                    }
                }

                c.commit();
            } catch (Exception ex) {
                c.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException("Error al finalizar la orden.", ex);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public void cancelarOrden(int idOrden) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                String consulta =
                    "SELECT estado FROM ordenes_trabajo " +
                    "WHERE id_orden = ? FOR UPDATE";

                try (PreparedStatement ps = c.prepareStatement(consulta)) {
                    ps.setInt(1, idOrden);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("La orden no existe.");
                        }

                        String estado = rs.getString(1);
                        if (!"PENDIENTE".equals(estado)
                                && !"EN PROCESO".equals(estado)) {
                            throw new SQLException(
                                "Solo se cancelan órdenes pendientes o en proceso."
                            );
                        }
                    }
                }

                String devolver =
                    "UPDATE repuestos r SET existencia = existencia + (" +
                    "SELECT NVL(SUM(orj.cantidad),0) " +
                    "FROM orden_repuestos orj " +
                    "WHERE orj.id_orden = ? " +
                    "AND orj.id_repuesto = r.id_repuesto) " +
                    "WHERE EXISTS (SELECT 1 FROM orden_repuestos orj " +
                    "WHERE orj.id_orden = ? " +
                    "AND orj.id_repuesto = r.id_repuesto)";

                try (PreparedStatement ps = c.prepareStatement(devolver)) {
                    ps.setInt(1, idOrden);
                    ps.setInt(2, idOrden);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM orden_repuestos WHERE id_orden = ?")) {
                    ps.setInt(1, idOrden);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "DELETE FROM orden_servicios WHERE id_orden = ?")) {
                    ps.setInt(1, idOrden);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE ordenes_trabajo SET estado = 'CANCELADA', " +
                        "total = 0 WHERE id_orden = ?")) {
                    ps.setInt(1, idOrden);
                    ps.executeUpdate();
                }

                c.commit();
            } catch (Exception ex) {
                c.rollback();
                if (ex instanceof SQLException) {
                    throw (SQLException) ex;
                }
                throw new SQLException("Error al cancelar la orden.", ex);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public String generarComprobante(int idOrden) throws SQLException {
        StringBuilder sb = new StringBuilder();

        String sql =
            "SELECT o.id_orden, o.fecha_creacion, o.problema, " +
            "o.estado, o.total, cl.nombre, v.placa, v.marca, v.modelo, " +
            "m.nombre " +
            "FROM ordenes_trabajo o " +
            "JOIN vehiculos v ON v.id_vehiculo = o.id_vehiculo " +
            "JOIN clientes cl ON cl.id_cliente = v.id_cliente " +
            "JOIN mecanicos m ON m.id_mecanico = o.id_mecanico " +
            "WHERE o.id_orden = ?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idOrden);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("No se encontró la orden.");
                }

                sb.append("TALLER EL ÚLTIMO ARRANQUE\n");
                sb.append("COMPROBANTE DE REPARACIÓN\n\n");
                sb.append("Orden: ").append(rs.getInt(1)).append("\n");
                sb.append("Fecha: ").append(rs.getTimestamp(2)).append("\n");
                sb.append("Cliente: ").append(rs.getString(6)).append("\n");
                sb.append("Vehículo: ").append(rs.getString(7)).append(" ")
                  .append(rs.getString(8)).append(" ")
                  .append(rs.getString(9)).append("\n");
                sb.append("Mecánico: ").append(rs.getString(10)).append("\n");
                sb.append("Problema: ").append(rs.getString(3)).append("\n");
                sb.append("Estado: ").append(rs.getString(4)).append("\n\n");
                sb.append("SERVICIOS\n");

                try (PreparedStatement ps2 = c.prepareStatement(
                    "SELECT s.descripcion, os.costo_aplicado " +
                    "FROM orden_servicios os JOIN servicios s " +
                    "ON s.id_servicio = os.id_servicio WHERE os.id_orden = ?")) {
                    ps2.setInt(1, idOrden);
                    try (ResultSet r2 = ps2.executeQuery()) {
                        while (r2.next()) {
                            sb.append("- ").append(r2.getString(1))
                              .append(": Q").append(r2.getBigDecimal(2))
                              .append("\n");
                        }
                    }
                }

                sb.append("\nREPUESTOS\n");

                try (PreparedStatement ps3 = c.prepareStatement(
                    "SELECT r.nombre, orj.cantidad, orj.precio_aplicado " +
                    "FROM orden_repuestos orj JOIN repuestos r " +
                    "ON r.id_repuesto = orj.id_repuesto " +
                    "WHERE orj.id_orden = ?")) {
                    ps3.setInt(1, idOrden);
                    try (ResultSet r3 = ps3.executeQuery()) {
                        while (r3.next()) {
                            sb.append("- ").append(r3.getString(1))
                              .append(" x").append(r3.getInt(2))
                              .append(": Q")
                              .append(r3.getBigDecimal(3)
                                  .multiply(BigDecimal.valueOf(r3.getInt(2))))
                              .append("\n");
                        }
                    }
                }

                sb.append("\nTOTAL: Q").append(rs.getBigDecimal(5)).append("\n");
            }
        }

        return sb.toString();
    }
}
