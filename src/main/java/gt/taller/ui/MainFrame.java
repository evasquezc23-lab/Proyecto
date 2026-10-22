package gt.taller.ui;
import gt.taller.config.Database;
import gt.taller.dao.GenericTableDao;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import javax.imageio.ImageIO;

public class MainFrame extends JFrame {

    private final GenericTableDao dao = new GenericTableDao();
    private final JTabbedPane tabs = new JTabbedPane();

    public MainFrame() {
        setTitle("Taller El Último Arranque");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);

        add(tabs, BorderLayout.CENTER);

        crearInicio();

        addTab(
            "Clientes",
            "SELECT id_cliente, nombre, telefono, correo "
                + "FROM clientes ORDER BY id_cliente",
            new String[]{"ID", "Nombre", "Teléfono", "Correo"},
            4,
            "INSERT INTO clientes(nombre, telefono, correo) VALUES(?,?,?)",
            new String[]{"Nombre", "Teléfono", "Correo"}
        );

        addTab(
            "Vehículos",
            "SELECT id_vehiculo, placa, marca, modelo, id_cliente "
                + "FROM vehiculos ORDER BY id_vehiculo",
            new String[]{"ID", "Placa", "Marca", "Modelo", "ID cliente"},
            5,
            "INSERT INTO vehiculos(placa, marca, modelo, id_cliente) "
                + "VALUES(?,?,?,?)",
            new String[]{"Placa", "Marca", "Modelo", "ID cliente"}
        );

        addTab(
            "Mecánicos",
            "SELECT id_mecanico, nombre, telefono, especialidad "
                + "FROM mecanicos ORDER BY id_mecanico",
            new String[]{"ID", "Nombre", "Teléfono", "Especialidad"},
            4,
            "INSERT INTO mecanicos(nombre, telefono, especialidad) "
                + "VALUES(?,?,?)",
            new String[]{"Nombre", "Teléfono", "Especialidad"}
        );

        addTab(
            "Repuestos",
            "SELECT id_repuesto, nombre, descripcion, precio, existencia "
                + "FROM repuestos ORDER BY id_repuesto",
            new String[]{"ID", "Nombre", "Descripción", "Precio", "Existencia"},
            5,
            "INSERT INTO repuestos(nombre, descripcion, precio, existencia) "
                + "VALUES(?,?,?,?)",
            new String[]{"Nombre", "Descripción", "Precio", "Existencia"}
        );

        addTab(
            "Servicios",
            "SELECT id_servicio, descripcion, costo_mano_obra "
                + "FROM servicios ORDER BY id_servicio",
            new String[]{"ID", "Servicio", "Mano de obra"},
            3,
            "INSERT INTO servicios(descripcion, costo_mano_obra) VALUES(?,?)",
            new String[]{"Descripción", "Costo mano de obra"}
        );

        addTab(
            "Órdenes",
            "SELECT id_orden, id_vehiculo, id_mecanico, estado, total, problema "
                + "FROM ordenes_trabajo ORDER BY id_orden DESC",
            new String[]{"ID", "ID vehículo", "ID mecánico", "Estado",
                "Total", "Problema"},
            6,
            "INSERT INTO ordenes_trabajo"
                + "(id_vehiculo, id_mecanico, problema, estado, total) "
                + "VALUES(?,?,?,'PENDIENTE',0)",
            new String[]{"ID vehículo", "ID mecánico", "Problema"}
        );

        // Nuevo módulo para agendar y consultar citas.
        addTab(
            "Citas",
            "SELECT c.id_cita, cl.nombre, v.placa, v.marca, v.modelo, "
                + "TO_CHAR(c.fecha_cita, 'DD/MM/YYYY HH24:MI'), "
                + "c.motivo, c.estado "
                + "FROM citas c "
                + "JOIN clientes cl ON c.id_cliente = cl.id_cliente "
                + "JOIN vehiculos v ON c.id_vehiculo = v.id_vehiculo "
                + "ORDER BY c.fecha_cita DESC",
            new String[]{"ID", "Cliente", "Placa", "Marca", "Modelo",
                "Fecha y hora", "Motivo", "Estado"},
            8,
            "INSERT INTO citas "
                + "(id_cliente, id_vehiculo, fecha_cita, motivo, estado) "
                + "VALUES(?,?,TO_DATE(?, 'YYYY-MM-DD HH24:MI'),?,'PENDIENTE')",
            new String[]{"ID cliente", "ID vehículo",
                "Fecha y hora (AAAA-MM-DD HH:MM)", "Motivo"}
        );

        tabs.addTab("Reportes", crearReportes());
    }

    private void crearInicio() {
        FondoPanel home = new FondoPanel("/images/taller-inicio.png");
        home.setLayout(new BorderLayout(20, 20));
        home.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));
        encabezado.setOpaque(false);

        JLabel titulo = new JLabel("Taller El Último Arranque");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 30));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel(
            "Gestión de clientes, vehículos, inventario y órdenes"
        );
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitulo.setForeground(Color.WHITE);

        encabezado.add(titulo);
        encabezado.add(Box.createVerticalStrut(8));
        encabezado.add(subtitulo);
        home.add(encabezado, BorderLayout.NORTH);

        JLabel descripcion = new JLabel(
            "<html><div style='width:650px; color:black;'>"
            + "<h2>BIENVENIDO A TALLER EL ÚLTIMO ARRANQUE</h2>"
            + "<p>Sistema de gestión de taller mecánico.</p>"
            + "<p>Administra clientes, vehículos, mecánicos, repuestos, "
            + "servicios, citas y órdenes de trabajo.</p>"
            + "<p>Aplicación desarrollada con Java Swing y Oracle Database.</p>"
            + "</div></html>"
        );
        descripcion.setFont(new Font("SansSerif", Font.PLAIN, 16));
        descripcion.setForeground(Color.BLACK);
        descripcion.setOpaque(false);

        JPanel centro = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        centro.setOpaque(false);
        centro.add(descripcion);
        home.add(centro, BorderLayout.CENTER);

        JButton probar = new JButton("Probar conexión Oracle");
        probar.addActionListener(e -> {
            try {
                Database.testConnection();
                JOptionPane.showMessageDialog(
                    this, "Conexión Oracle correcta."
                );
            } catch (SQLException ex) {
                error(ex);
            }
        });

        JPanel pie = new JPanel();
        pie.setOpaque(false);
        pie.add(probar);
        home.add(pie, BorderLayout.SOUTH);

        tabs.addTab("Inicio", home);
    }

    private void addTab(
        String nombre,
        String sql,
        String[] encabezados,
        int columnas,
        String insert,
        String[] campos
    ) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        DefaultTableModel modelo = new DefaultTableModel(encabezados, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        JTable tabla = new JTable(modelo);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setRowHeight(24);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton actualizar = new JButton("Actualizar");
        JButton nuevo = new JButton("Nuevo");
        JButton csv = new JButton("Exportar CSV");

        actualizar.addActionListener(e -> cargar(tabla, sql, columnas));

        nuevo.addActionListener(e -> {
            JTextField[] entradas = new JTextField[campos.length];
            JPanel formulario = new JPanel(
                new GridLayout(campos.length, 2, 8, 8)
            );

            for (int i = 0; i < campos.length; i++) {
                formulario.add(new JLabel(campos[i] + ":"));
                entradas[i] = new JTextField(22);
                formulario.add(entradas[i]);
            }

            if (JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Nuevo registro - " + nombre,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
            ) != JOptionPane.OK_OPTION) {
                return;
            }

            try {
                Object[] valores = new Object[entradas.length];

                for (int i = 0; i < entradas.length; i++) {
                    String valor = entradas[i].getText().trim();

                    if (valor.isEmpty()) {
                        throw new IllegalArgumentException(
                            "Debes completar: " + campos[i]
                        );
                    }

                    String campo = campos[i].toLowerCase();

                    if (campo.contains("id ")
                        || campo.contains("precio")
                        || campo.contains("existencia")
                        || campo.contains("costo")) {

                        valores[i] = Double.valueOf(valor);

                    } else {
                        valores[i] = valor;
                    }
                }

                dao.execute(insert, valores);
                cargar(tabla, sql, columnas);

                JOptionPane.showMessageDialog(
                    this, "Registro guardado correctamente."
                );

            } catch (Exception ex) {
                error(ex);
            }
        });

        csv.addActionListener(e -> exportarCsv(tabla, nombre));

        controles.add(actualizar);
        controles.add(nuevo);
        controles.add(csv);
        panel.add(controles, BorderLayout.SOUTH);

        tabs.addTab(nombre, panel);
        cargar(tabla, sql, columnas);
    }

    private JPanel crearReportes() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] nombres = {
            "Citas detalladas",
            "Órdenes de trabajo detalladas",
            "Órdenes por estado",
            "Ingresos por día",
            "Órdenes por mecánico",
            "Inventario bajo (5 o menos)"
        };

        String[] consultas = {
            "SELECT c.id_cita, cl.nombre, v.placa, v.marca, v.modelo, "
                + "TO_CHAR(c.fecha_cita,'DD/MM/YYYY HH24:MI'), "
                + "c.motivo, c.estado, c.observaciones "
                + "FROM citas c "
                + "JOIN clientes cl ON c.id_cliente=cl.id_cliente "
                + "JOIN vehiculos v ON c.id_vehiculo=v.id_vehiculo "
                + "ORDER BY c.fecha_cita DESC",

            "SELECT o.id_orden, "
                + "cl.nombre, v.placa, v.marca || ' ' || v.modelo, "
                + "m.nombre, o.problema, o.estado, o.total "
                + "FROM ordenes_trabajo o "
                + "JOIN vehiculos v ON o.id_vehiculo=v.id_vehiculo "
                + "JOIN clientes cl ON v.id_cliente=cl.id_cliente "
                + "JOIN mecanicos m ON o.id_mecanico=m.id_mecanico "
                + "ORDER BY o.id_orden DESC",

            "SELECT estado, COUNT(*) AS cantidad, "
                + "NVL(SUM(total),0) AS total "
                + "FROM ordenes_trabajo GROUP BY estado ORDER BY estado",

            "SELECT TO_CHAR(TRUNC(fecha_creacion),'DD/MM/YYYY'), "
                + "COUNT(*) AS ordenes, NVL(SUM(total),0) AS ingresos, "
                + "ROUND(NVL(AVG(total),0),2) AS promedio "
                + "FROM ordenes_trabajo WHERE estado='FINALIZADA' "
                + "GROUP BY TRUNC(fecha_creacion) "
                + "ORDER BY TRUNC(fecha_creacion) DESC",

            "SELECT m.nombre, COUNT(o.id_orden) AS ordenes, "
                + "NVL(SUM(o.total),0) AS total "
                + "FROM mecanicos m "
                + "LEFT JOIN ordenes_trabajo o "
                + "ON o.id_mecanico=m.id_mecanico "
                + "GROUP BY m.nombre ORDER BY m.nombre",

            "SELECT id_repuesto, nombre, descripcion, existencia, precio "
                + "FROM repuestos WHERE existencia<=5 "
                + "ORDER BY existencia, nombre"
        };

        String[][] encabezados = {
            {"ID cita", "Cliente", "Placa", "Marca", "Modelo",
                "Fecha y hora", "Motivo", "Estado", "Observaciones"},
            {"ID orden", "Cliente", "Placa", "Vehículo", "Mecánico",
                "Problema", "Estado", "Total"},
            {"Estado", "Cantidad", "Total"},
            {"Fecha", "Órdenes finalizadas", "Ingresos", "Promedio"},
            {"Mecánico", "Órdenes", "Total"},
            {"ID repuesto", "Nombre", "Descripción", "Existencia", "Precio"}
        };

        JComboBox<String> seleccion = new JComboBox<>(nombres);

        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        JTable tabla = new JTable(modelo);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setRowHeight(24);

        JButton generar = new JButton("Generar reporte");
        JButton csv = new JButton("Exportar CSV");

        generar.addActionListener(e -> {
            int indice = seleccion.getSelectedIndex();

            try {
                List<String[]> filas = dao.query(
                    consultas[indice],
                    encabezados[indice].length
                );

                modelo.setRowCount(0);
                modelo.setColumnIdentifiers(encabezados[indice]);

                for (String[] fila : filas) {
                    modelo.addRow(fila);
                }

                if (filas.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "La consulta se ejecutó correctamente, "
                            + "pero no hay datos para este reporte."
                    );
                }

            } catch (SQLException ex) {
                error(ex);
            }
        });

        csv.addActionListener(e -> exportarCsv(tabla, "reporte"));

        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        arriba.add(new JLabel("Selecciona un reporte:"));
        arriba.add(seleccion);
        arriba.add(generar);
        arriba.add(csv);

        panel.add(arriba, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        return panel;
    }

    private void cargar(JTable tabla, String sql, int columnas) {
        try {
            List<String[]> filas = dao.query(sql, columnas);
            DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();

            modelo.setRowCount(0);

            for (String[] fila : filas) {
                modelo.addRow(fila);
            }

        } catch (SQLException ex) {
            if (isShowing()) {
                error(ex);
            }
        }
    }

    private void exportarCsv(JTable tabla, String nombre) {
        JFileChooser selector = new JFileChooser();
        selector.setSelectedFile(
            new File(nombre.toLowerCase().replace(" ", "_") + ".csv")
        );

        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (BufferedWriter escritor = new BufferedWriter(
            new OutputStreamWriter(
                new FileOutputStream(selector.getSelectedFile()),
                StandardCharsets.UTF_8
            )
        )) {
            for (int columna = 0;
                 columna < tabla.getColumnCount(); columna++) {
                if (columna > 0) {
                    escritor.write(",");
                }
                escritor.write(comillas(
                    tabla.getColumnName(columna)
                ));
            }
            escritor.newLine();

            for (int fila = 0; fila < tabla.getRowCount(); fila++) {
                for (int columna = 0;
                     columna < tabla.getColumnCount(); columna++) {
                    if (columna > 0) {
                        escritor.write(",");
                    }

                    Object valor = tabla.getValueAt(fila, columna);
                    escritor.write(comillas(
                        valor == null ? "" : valor.toString()
                    ));
                }
                escritor.newLine();
            }

            JOptionPane.showMessageDialog(
                this, "Archivo CSV exportado correctamente."
            );

        } catch (IOException ex) {
            error(ex);
        }
    }

    private String comillas(String texto) {
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }

    private void error(Exception ex) {
        JOptionPane.showMessageDialog(
            this,
            ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private static class FondoPanel extends JPanel {
        private BufferedImage imagen;

        FondoPanel(String recurso) {
            try (InputStream entrada =
                     MainFrame.class.getResourceAsStream(recurso)) {
                if (entrada != null) {
                    imagen = ImageIO.read(entrada);
                }
            } catch (IOException ex) {
                System.err.println(
                    "No se pudo cargar la imagen: " + ex.getMessage()
                );
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (imagen != null) {
                g.drawImage(
                    imagen, 0, 0,
                    getWidth(), getHeight(), this
                );
            } else {
                g.setColor(new Color(225, 225, 225));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }
}
