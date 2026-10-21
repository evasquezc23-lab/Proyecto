
package gt.taller.ui;
import gt.taller.config.Database;
import gt.taller.dao.GenericTableDao;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class MainFrame extends JFrame {

    private final GenericTableDao dao = new GenericTableDao();
    private final JTabbedPane tabs = new JTabbedPane();

    public MainFrame() {

        setTitle("Taller El Último Arranque");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 680);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);

        FondoPanel home = new FondoPanel();
        home.setLayout(new BorderLayout(15, 15));
        home.setBorder(
            BorderFactory.createEmptyBorder(24, 24, 24, 24)
        );

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);

        JLabel titulo = new JLabel(
            "<html><h1 style='color:white;'>"
            + "Taller El Último Arranque"
            + "</h1>"
            + "<p style='color:white;'>"
            + "Gestión de clientes, vehículos, inventario y órdenes"
            + "</p></html>"
        );

        encabezado.add(titulo, BorderLayout.NORTH);
        home.add(encabezado, BorderLayout.NORTH);

      
        
     
JLabel info = new JLabel(
    "<html><div style='width:540px; color:black;'>"
    + "<h2>BIENVENIDO A TALLER EL ÚLTIMO ARRANQUE</h2>"
    + "<p>Sistema de gestión de taller mecánico.</p>"
    + "<p>Administra clientes, vehículos, mecánicos, repuestos, "
    + "servicios y órdenes de trabajo.</p>"
    + "<p>La aplicación utiliza Java Swing y Oracle Database.</p>"
    + "</div></html>"
);

info.setFont(new Font("SansSerif", Font.PLAIN, 16));
info.setForeground(Color.BLACK);
info.setOpaque(false);

JPanel centro = new JPanel(new BorderLayout());
centro.setOpaque(false);
centro.add(info, BorderLayout.WEST);

home.add(centro, BorderLayout.CENTER);

     
        JButton test = new JButton("Probar conexión Oracle");
        test.setFont(new Font("SansSerif", Font.BOLD, 14));

        test.addActionListener(e -> {
            try {
                Database.testConnection();

                JOptionPane.showMessageDialog(
                    this,
                    "Conexión Oracle correcta.",
                    "Conexión exitosa",
                    JOptionPane.INFORMATION_MESSAGE
                );

            } catch (SQLException ex) {
                error(ex);
            }
        });

        JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pie.setOpaque(false);
        pie.add(test);

        home.add(pie, BorderLayout.SOUTH);

        tabs.addTab("Inicio", home);

        addTab(
            "Clientes",
            "SELECT id_cliente,nombre,telefono,correo "
            + "FROM clientes ORDER BY id_cliente",
            new String[]{"ID", "Nombre", "Teléfono", "Correo"},
            4,
            "INSERT INTO clientes(nombre,telefono,correo) "
            + "VALUES(?,?,?)",
            new String[]{"Nombre", "Teléfono", "Correo"}
        );

        addTab(
            "Vehículos",
            "SELECT id_vehiculo,placa,marca,modelo,id_cliente "
            + "FROM vehiculos ORDER BY id_vehiculo",
            new String[]{
                "ID", "Placa", "Marca", "Modelo", "ID cliente"
            },
            5,
            "INSERT INTO vehiculos(placa,marca,modelo,id_cliente) "
            + "VALUES(?,?,?,?)",
            new String[]{
                "Placa", "Marca", "Modelo", "ID cliente"
            }
        );

        addTab(
            "Mecánicos",
            "SELECT id_mecanico,nombre,telefono,especialidad "
            + "FROM mecanicos ORDER BY id_mecanico",
            new String[]{
                "ID", "Nombre", "Teléfono", "Especialidad"
            },
            4,
            "INSERT INTO mecanicos(nombre,telefono,especialidad) "
            + "VALUES(?,?,?)",
            new String[]{
                "Nombre", "Teléfono", "Especialidad"
            }
        );


        addTab(
            "Repuestos",
            "SELECT id_repuesto,nombre,descripcion,precio,existencia "
            + "FROM repuestos ORDER BY id_repuesto",
            new String[]{
                "ID", "Nombre", "Descripción", "Precio", "Existencia"
            },
            5,
            "INSERT INTO repuestos(nombre,descripcion,precio,existencia) "
            + "VALUES(?,?,?,?)",
            new String[]{
                "Nombre", "Descripción", "Precio", "Existencia"
            }
        );


        addTab(
            "Servicios",
            "SELECT id_servicio,descripcion,costo_mano_obra "
            + "FROM servicios ORDER BY id_servicio",
            new String[]{
                "ID", "Servicio", "Mano de obra"
            },
            3,
            "INSERT INTO servicios(descripcion,costo_mano_obra) "
            + "VALUES(?,?)",
            new String[]{
                "Descripción", "Costo mano de obra"
            }
        );

        addTab(
            "Órdenes",
            "SELECT id_orden,id_vehiculo,id_mecanico,estado,total,problema "
            + "FROM ordenes_trabajo ORDER BY id_orden DESC",
            new String[]{
                "ID", "ID vehículo", "ID mecánico",
                "Estado", "Total", "Problema"
            },
            6,
            "INSERT INTO ordenes_trabajo"
            + "(id_vehiculo,id_mecanico,problema,estado,total) "
            + "VALUES(?,?,?,'PENDIENTE',0)",
            new String[]{
                "ID vehículo", "ID mecánico", "Problema"
            }
        );


        tabs.addTab("Reportes", reports());

        add(tabs, BorderLayout.CENTER);
    }

  
    private class FondoPanel extends JPanel {

        private BufferedImage imagen;

        public FondoPanel() {

            try {
                URL ruta = getClass().getResource(
                    "/images/taller-inicio.png"
                );

                if (ruta != null) {
                    imagen = ImageIO.read(ruta);
                } else {
                    System.err.println(
                        "No se encontró la imagen: "
                        + "src/main/resources/images/taller-inicio.png"
                    );
                }

            } catch (IOException ex) {
                System.err.println(
                    "No se pudo cargar la imagen: "
                    + ex.getMessage()
                );
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (imagen != null) {
                Graphics2D g2 = (Graphics2D) g.create();

                g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
                );

                g2.drawImage(
                    imagen,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    this
                );

                g2.dispose();

            } else {
                // Fondo alternativo si la imagen no está disponible
                g.setColor(new Color(55, 65, 75));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private void addTab(
        String name,
        String sql,
        String[] heads,
        int cols,
        String insert,
        String[] fields
    ) {

        JPanel panel = new JPanel(new BorderLayout(6, 6));

        JTable table = new JTable(
            new DefaultTableModel(heads, 0)
        );

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel controls = new JPanel();

        JButton refresh = new JButton("Actualizar");
        JButton addButton = new JButton("Nuevo");
        JButton csv = new JButton("Exportar CSV");

        refresh.addActionListener(e -> load(table, sql, cols));

        addButton.addActionListener(e -> {

            JTextField[] f = new JTextField[fields.length];

            JPanel form = new JPanel(
                new GridLayout(fields.length, 2, 5, 5)
            );

            for (int i = 0; i < f.length; i++) {
                form.add(new JLabel(fields[i]));
                f[i] = new JTextField();
                form.add(f[i]);
            }

            if (JOptionPane.showConfirmDialog(
                this,
                form,
                "Nuevo " + name,
                JOptionPane.OK_CANCEL_OPTION
            ) == JOptionPane.OK_OPTION) {

                try {
                    Object[] v = new Object[f.length];

                    for (int i = 0; i < f.length; i++) {

                        String s = f[i].getText().trim();

                        if (s.isEmpty()) {
                            throw new IllegalArgumentException(
                                "Completa " + fields[i]
                            );
                        }

                        String label = fields[i].toLowerCase();

                        if (label.contains("id ")
                            || label.contains("precio")
                            || label.contains("existencia")
                            || label.contains("costo")) {

                            v[i] = Double.valueOf(s);

                        } else {
                            v[i] = s;
                        }
                    }

                    dao.execute(insert, v);
                    load(table, sql, cols);

                    JOptionPane.showMessageDialog(
                        this,
                        "Registro guardado correctamente."
                    );

                } catch (Exception ex) {
                    error(ex);
                }
            }
        });

        csv.addActionListener(e -> exportCsv(table, name));

        controls.add(refresh);
        controls.add(addButton);
        controls.add(csv);

        panel.add(controls, BorderLayout.SOUTH);
        tabs.addTab(name, panel);

        load(table, sql, cols);
    }

    private JPanel reports() {

        JPanel p = new JPanel(new BorderLayout());

        String[] names = {
            "Órdenes por estado",
            "Inventario bajo (<=5)",
            "Órdenes por mecánico",
            "Ingresos por día"
        };

        String[] sql = {
            "SELECT estado,COUNT(*) "
            + "FROM ordenes_trabajo GROUP BY estado",

            "SELECT id_repuesto,nombre,existencia "
            + "FROM repuestos WHERE existencia<=5 "
            + "ORDER BY existencia",

            "SELECT m.nombre,COUNT(o.id_orden) "
            + "FROM mecanicos m LEFT JOIN ordenes_trabajo o "
            + "ON o.id_mecanico=m.id_mecanico "
            + "GROUP BY m.nombre ORDER BY m.nombre",

            "SELECT TO_CHAR(fecha_creacion,'YYYY-MM-DD'),SUM(total) "
            + "FROM ordenes_trabajo WHERE estado='FINALIZADA' "
            + "GROUP BY TO_CHAR(fecha_creacion,'YYYY-MM-DD') "
            + "ORDER BY 1"
        };

        JComboBox<String> choice = new JComboBox<>(names);
        JTable t = new JTable();
        JButton run = new JButton("Generar");

        run.addActionListener(e -> {

            int i = choice.getSelectedIndex();
            int n = i == 1 ? 3 : 2;

            try {
                List<String[]> rows = dao.query(sql[i], n);

                DefaultTableModel m = new DefaultTableModel();

                for (int j = 1; j <= n; j++) {
                    m.addColumn("Columna " + j);
                }

                for (String[] row : rows) {
                    m.addRow(row);
                }

                t.setModel(m);

            } catch (SQLException ex) {
                error(ex);
            }
        });

        JPanel top = new JPanel();
        top.add(choice);
        top.add(run);

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(t), BorderLayout.CENTER);

        return p;
    }

   
    private void load(JTable t, String sql, int cols) {

        try {
            List<String[]> rows = dao.query(sql, cols);

            DefaultTableModel m =
                (DefaultTableModel) t.getModel();

            m.setRowCount(0);

            for (String[] r : rows) {
                m.addRow(r);
            }

        } catch (SQLException ex) {

            if (isShowing()) {
                error(ex);
            }
        }
    }

    
    private void exportCsv(JTable t, String name) {

        JFileChooser c = new JFileChooser();

        c.setSelectedFile(
            new File(name.toLowerCase() + ".csv")
        );

        if (c.showSaveDialog(this)
            != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (
            BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(c.getSelectedFile()),
                    StandardCharsets.UTF_8
                )
            )
        ) {

            for (int j = 0; j < t.getColumnCount(); j++) {
                if (j > 0) {
                    w.write(",");
                }

                w.write(q(t.getColumnName(j)));
            }

            w.newLine();

            for (int i = 0; i < t.getRowCount(); i++) {

                for (int j = 0; j < t.getColumnCount(); j++) {

                    if (j > 0) {
                        w.write(",");
                    }

                    Object v = t.getValueAt(i, j);

                    w.write(q(v == null ? "" : v.toString()));
                }

                w.newLine();
            }

            JOptionPane.showMessageDialog(
                this,
                "CSV exportado correctamente."
            );

        } catch (IOException ex) {
            error(ex);
        }
    }

    private String q(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    
    private void error(Exception ex) {

        JOptionPane.showMessageDialog(
            this,
            ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
