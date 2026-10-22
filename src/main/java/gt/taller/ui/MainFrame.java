package gt.taller.ui;

import gt.taller.config.Database;
import gt.taller.dao.GenericTableDao;
import gt.taller.service.ReparacionService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainFrame extends JFrame {

    private final GenericTableDao dao = new GenericTableDao();
    private final JTabbedPane tabs = new JTabbedPane();

    public MainFrame() {
        setTitle("Taller El Último Arranque");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);

        add(tabs, BorderLayout.CENTER);

        crearInicio();

        addTab(
            "Clientes",
            "SELECT id_cliente,nombre,telefono,correo FROM clientes ORDER BY id_cliente",
            new String[]{"ID", "Nombre", "Teléfono", "Correo"},
            4,
            "INSERT INTO clientes(nombre,telefono,correo) VALUES(?,?,?)",
            new String[]{"Nombre", "Teléfono", "Correo"}
        );

        addTab(
            "Vehículos",
            "SELECT id_vehiculo,placa,marca,modelo,id_cliente FROM vehiculos ORDER BY id_vehiculo",
            new String[]{"ID", "Placa", "Marca", "Modelo", "ID cliente"},
            5,
            "INSERT INTO vehiculos(placa,marca,modelo,id_cliente) VALUES(?,?,?,?)",
            new String[]{"Placa", "Marca", "Modelo", "ID cliente"}
        );

        addTab(
            "Mecánicos",
            "SELECT id_mecanico,nombre,telefono,especialidad FROM mecanicos ORDER BY id_mecanico",
            new String[]{"ID", "Nombre", "Teléfono", "Especialidad"},
            4,
            "INSERT INTO mecanicos(nombre,telefono,especialidad) VALUES(?,?,?)",
            new String[]{"Nombre", "Teléfono", "Especialidad"}
        );

        addTab(
            "Repuestos",
            "SELECT id_repuesto,nombre,descripcion,precio,existencia FROM repuestos ORDER BY id_repuesto",
            new String[]{"ID", "Nombre", "Descripción", "Precio", "Existencia"},
            5,
            "INSERT INTO repuestos(nombre,descripcion,precio,existencia) VALUES(?,?,?,?)",
            new String[]{"Nombre", "Descripción", "Precio", "Existencia"}
        );

        addTab(
            "Servicios",
            "SELECT id_servicio,descripcion,costo_mano_obra FROM servicios ORDER BY id_servicio",
            new String[]{"ID", "Servicio", "Mano de obra"},
            3,
            "INSERT INTO servicios(descripcion,costo_mano_obra) VALUES(?,?)",
            new String[]{"Descripción", "Costo mano de obra"}
        );

        crearOrdenes();
        crearCitas();
        crearReportes();
    }

    private void crearInicio() {
        FondoPanel home = new FondoPanel();
        home.setLayout(new BorderLayout(15, 15));
        home.setBorder(BorderFactory.createEmptyBorder(25, 25, 20, 25));

        JPanel encabezado = new JPanel();
        encabezado.setOpaque(false);
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Taller El Último Arranque");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 30));
        titulo.setForeground(Color.WHITE);

        JLabel subtitulo = new JLabel(
            "Gestión de clientes, vehículos, inventario y órdenes"
        );
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 17));
        subtitulo.setForeground(Color.WHITE);

        encabezado.add(titulo);
        encabezado.add(Box.createVerticalStrut(8));
        encabezado.add(subtitulo);

        home.add(encabezado, BorderLayout.NORTH);

        JTextArea info = new JTextArea(
            "BIENVENIDO A TALLER EL ÚLTIMO ARRANQUE\n\n"
            + "Sistema de gestión de taller mecánico.\n"
            + "Administra clientes, vehículos, mecánicos, repuestos,\n"
            + "servicios, citas y órdenes de trabajo.\n"
            + "Aplicación desarrollada con Java Swing y Oracle Database."
        );

        info.setEditable(false);
        info.setFocusable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        info.setOpaque(false);
        info.setForeground(Color.BLACK);
        info.setFont(new Font("SansSerif", Font.PLAIN, 18));
        info.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(140, 20, 10, 20);

        centro.add(info, gbc);
        home.add(centro, BorderLayout.CENTER);

        JButton test = new JButton("Probar conexión Oracle");
        test.addActionListener(e -> {
            try {
                Database.testConnection();
                JOptionPane.showMessageDialog(
                    this,
                    "Conexión Oracle correcta."
                );
            } catch (SQLException ex) {
                error(ex);
            }
        });

        JPanel inferior = new JPanel();
        inferior.setOpaque(false);
        inferior.add(test);
        home.add(inferior, BorderLayout.SOUTH);

        tabs.addTab("Inicio", home);
    }

    private class FondoPanel extends JPanel {
        private Image imagen;

        FondoPanel() {
            try (InputStream in = getClass().getResourceAsStream(
                    "/images/taller-inicio.png")) {
                if (in != null) {
                    imagen = javax.imageio.ImageIO.read(in);
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
                g.setColor(new Color(65, 75, 85));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    private void addTab(
            String nombre,
            String sql,
            String[] encabezados,
            int columnas,
            String insert,
            String[] campos) {

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        JTable tabla = new JTable(new DefaultTableModel(encabezados, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        });
        tabla.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel controles = new JPanel();
        JButton actualizar = new JButton("Actualizar");
        JButton nuevo = new JButton("Nuevo");
        JButton guardar = new JButton("Guardar cambios");
        JButton exportar = new JButton("Exportar CSV");

        actualizar.addActionListener(e -> cargar(tabla, sql, columnas));
        guardar.addActionListener(e -> guardarCambiosTabla(nombre, tabla));

        nuevo.addActionListener(e -> {
            JTextField[] entradas = new JTextField[campos.length];
            JPanel formulario = new JPanel(
                new GridLayout(campos.length, 2, 5, 5)
            );

            for (int i = 0; i < campos.length; i++) {
                formulario.add(new JLabel(campos[i]));
                entradas[i] = new JTextField();
                formulario.add(entradas[i]);
            }

            int opcion = JOptionPane.showConfirmDialog(
                this, formulario, "Nuevo " + nombre,
                JOptionPane.OK_CANCEL_OPTION
            );

            if (opcion != JOptionPane.OK_OPTION) {
                return;
            }

            try {
                Object[] valores = new Object[entradas.length];

                for (int i = 0; i < entradas.length; i++) {
                    String valor = entradas[i].getText().trim();

                    if (valor.isEmpty()) {
                        throw new IllegalArgumentException(
                            "Completa el campo: " + campos[i]
                        );
                    }

                    String campo = campos[i].toLowerCase();

                    if (campo.startsWith("id ")
                            || campo.contains("precio")
                            || campo.contains("existencia")
                            || campo.contains("costo")) {
                        valores[i] = new java.math.BigDecimal(valor);
                    } else {
                        valores[i] = valor;
                    }
                }

                dao.execute(insert, valores);
                cargar(tabla, sql, columnas);
                JOptionPane.showMessageDialog(this, "Registro guardado.");

            } catch (Exception ex) {
                error(ex);
            }
        });

        exportar.addActionListener(e -> exportarCsv(tabla, nombre));

        controles.add(actualizar);
        controles.add(nuevo);
        controles.add(guardar);
        controles.add(exportar);
        panel.add(controles, BorderLayout.SOUTH);

        tabs.addTab(nombre, panel);
        cargar(tabla, sql, columnas);
    }

    private void crearOrdenes() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));

        String sql =
            "SELECT id_orden,id_vehiculo,id_mecanico,estado,total,problema "
            + "FROM ordenes_trabajo ORDER BY id_orden DESC";

        String[] columnas = {
            "ID orden", "ID vehículo", "ID mecánico",
            "Estado", "Total", "Problema"
        };

        JTable tabla = new JTable(new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return column == 1 || column == 2 || column == 5;
            }
        });
        tabla.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton actualizar = new JButton("Actualizar");
        JButton guardarDatos = new JButton("Guardar cambios");
        JButton nuevaOrden = new JButton("Nueva orden");
        JComboBox<String> selectorEstado = new JComboBox<>();
        selectorEstado.setPreferredSize(new Dimension(155, 26));
        selectorEstado.addItem("Selecciona estado");
        JButton guardarEstado = new JButton("Guardar cambio de estado");
        JButton comprobante = new JButton("Generar comprobante");
        JButton agregarServicio = new JButton("Agregar servicio");
        JButton agregarRepuesto = new JButton("Agregar repuesto");

        actualizar.addActionListener(e -> {
            cargar(tabla, sql, 6);
            actualizarOpcionesEstado(tabla, selectorEstado);
        });
        guardarDatos.addActionListener(e -> guardarCambiosTabla("Órdenes", tabla));
        nuevaOrden.addActionListener(e -> crearNuevaOrden(tabla, sql));
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarOpcionesEstado(tabla, selectorEstado);
            }
        });
        guardarEstado.addActionListener(e -> cambiarEstadoOrden(tabla, selectorEstado));
        comprobante.addActionListener(e -> generarComprobante(tabla));
        agregarServicio.addActionListener(e -> agregarServicioAOrden(tabla, sql));
        agregarRepuesto.addActionListener(e -> agregarRepuestoAOrden(tabla, sql));

        JPanel botones = new JPanel();
        botones.add(actualizar);
        botones.add(nuevaOrden);
        botones.add(guardarDatos);
        botones.add(new JLabel("Nuevo estado:"));
        botones.add(selectorEstado);
        botones.add(guardarEstado);
        botones.add(agregarServicio);
        botones.add(agregarRepuesto);
        botones.add(comprobante);
        panel.add(botones, BorderLayout.SOUTH);

        tabs.addTab("Órdenes", panel);
        cargar(tabla, sql, 6);
    }

    private void crearNuevaOrden(JTable tabla, String sql) {
        JTextField campoVehiculo = new JTextField();
        JTextField campoMecanico = new JTextField();
        JTextField campoProblema = new JTextField();

        JPanel formulario = new JPanel(new GridLayout(3, 2, 6, 6));
        formulario.add(new JLabel("ID del vehículo:"));
        formulario.add(campoVehiculo);
        formulario.add(new JLabel("ID del mecánico:"));
        formulario.add(campoMecanico);
        formulario.add(new JLabel("Problema reportado:"));
        formulario.add(campoProblema);

        int opcion = JOptionPane.showConfirmDialog(
            this,
            formulario,
            "Registrar nueva orden de trabajo",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String vehiculoTexto = campoVehiculo.getText().trim();
            String mecanicoTexto = campoMecanico.getText().trim();
            String problema = campoProblema.getText().trim();

            if (vehiculoTexto.isEmpty() || mecanicoTexto.isEmpty()
                    || problema.isEmpty()) {
                throw new IllegalArgumentException(
                    "Completa el ID del vehículo, el ID del mecánico y el problema."
                );
            }

            int idVehiculo = Integer.parseInt(vehiculoTexto);
            int idMecanico = Integer.parseInt(mecanicoTexto);

            String insertar =
                "INSERT INTO ordenes_trabajo "
                + "(id_vehiculo, id_mecanico, estado, total, problema) "
                + "VALUES (?, ?, 'PENDIENTE', 0, ?)";

            try (Connection c = Database.getConnection();
                 PreparedStatement ps = c.prepareStatement(insertar)) {
                ps.setInt(1, idVehiculo);
                ps.setInt(2, idMecanico);
                ps.setString(3, problema);
                ps.executeUpdate();
            }

            cargar(tabla, sql, 6);

            JOptionPane.showMessageDialog(
                this,
                "Nueva orden registrada correctamente.\n"
                + "Estado inicial: PENDIENTE."
            );

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Los ID del vehículo y del mecánico deben ser números enteros.",
                "Datos incorrectos",
                JOptionPane.WARNING_MESSAGE
            );
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void agregarServicioAOrden(JTable tabla, String sql) {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una orden de trabajo.");
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);
        int idOrden = Integer.parseInt(
            tabla.getModel().getValueAt(filaModelo, 0).toString()
        );
        String estado = tabla.getModel().getValueAt(filaModelo, 3)
            .toString().trim().toUpperCase();

        if (!"EN PROCESO".equals(estado)) {
            JOptionPane.showMessageDialog(
                this, "La orden debe estar EN PROCESO para agregar servicios."
            );
            return;
        }

        JTextField campoServicio = new JTextField();
        JPanel formulario = new JPanel(new GridLayout(1, 2, 5, 5));
        formulario.add(new JLabel("ID del servicio:"));
        formulario.add(campoServicio);

        int opcion = JOptionPane.showConfirmDialog(
            this, formulario, "Agregar servicio a la orden " + idOrden,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int idServicio = Integer.parseInt(campoServicio.getText().trim());
            ReparacionService servicio = new ReparacionService();
            servicio.agregarServicio(idOrden, idServicio);

            cargar(tabla, sql, 6);
            seleccionarOrden(tabla, idOrden);

            JOptionPane.showMessageDialog(
                this, "Servicio agregado correctamente. El total se actualizó."
            );
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this, "El ID del servicio debe ser un número entero.",
                "Dato incorrecto", JOptionPane.WARNING_MESSAGE
            );
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void agregarRepuestoAOrden(JTable tabla, String sql) {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una orden de trabajo.");
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);
        int idOrden = Integer.parseInt(
            tabla.getModel().getValueAt(filaModelo, 0).toString()
        );
        String estado = tabla.getModel().getValueAt(filaModelo, 3)
            .toString().trim().toUpperCase();

        if (!"EN PROCESO".equals(estado)) {
            JOptionPane.showMessageDialog(
                this, "La orden debe estar EN PROCESO para agregar repuestos."
            );
            return;
        }

        JTextField campoId = new JTextField();
        JTextField campoCantidad = new JTextField();

        JPanel formulario = new JPanel(new GridLayout(2, 2, 5, 5));
        formulario.add(new JLabel("ID del repuesto:"));
        formulario.add(campoId);
        formulario.add(new JLabel("Cantidad:"));
        formulario.add(campoCantidad);

        int opcion = JOptionPane.showConfirmDialog(
            this, formulario, "Agregar repuesto a la orden " + idOrden,
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            int idRepuesto = Integer.parseInt(campoId.getText().trim());
            int cantidad = Integer.parseInt(campoCantidad.getText().trim());

            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(
                    this, "La cantidad debe ser mayor que cero.",
                    "Cantidad incorrecta", JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            ReparacionService servicio = new ReparacionService();
            servicio.agregarRepuesto(idOrden, idRepuesto, cantidad);

            cargar(tabla, sql, 6);
            seleccionarOrden(tabla, idOrden);

            JOptionPane.showMessageDialog(
                this,
                "Repuesto agregado correctamente. El inventario y el total se actualizaron."
            );
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this,
                "El ID del repuesto y la cantidad deben ser números enteros.",
                "Datos incorrectos", JOptionPane.WARNING_MESSAGE
            );
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void seleccionarOrden(JTable tabla, int idOrden) {
        for (int filaVista = 0; filaVista < tabla.getRowCount(); filaVista++) {
            int filaModelo = tabla.convertRowIndexToModel(filaVista);
            int id = Integer.parseInt(
                tabla.getModel().getValueAt(filaModelo, 0).toString()
            );
            if (id == idOrden) {
                tabla.setRowSelectionInterval(filaVista, filaVista);
                return;
            }
        }
    }

    private void actualizarOpcionesEstado(JTable tabla, JComboBox<String> selectorEstado) {
        int fila = tabla.getSelectedRow();
        selectorEstado.removeAllItems();

        if (fila < 0) {
            selectorEstado.addItem("Selecciona una orden");
            selectorEstado.setEnabled(false);
            return;
        }

        int modeloFila = tabla.convertRowIndexToModel(fila);
        String estado = tabla.getModel().getValueAt(modeloFila, 3)
                .toString().trim().toUpperCase();

        selectorEstado.setEnabled(true);
        if ("PENDIENTE".equals(estado)) {
            selectorEstado.addItem("EN PROCESO");
            selectorEstado.addItem("CANCELADA");
        } else if ("EN PROCESO".equals(estado)) {
            selectorEstado.addItem("FINALIZADA");
            selectorEstado.addItem("CANCELADA");
        } else {
            selectorEstado.addItem("Sin cambios disponibles");
            selectorEstado.setEnabled(false);
        }
    }

    private void cambiarEstadoOrden(JTable tabla, JComboBox<String> selectorEstado) {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una orden primero.");
            return;
        }

        Object seleccion = selectorEstado.getSelectedItem();
        if (seleccion == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un estado válido.");
            return;
        }

        String nuevoEstado = seleccion.toString();
        if (!"EN PROCESO".equals(nuevoEstado)
                && !"FINALIZADA".equals(nuevoEstado)
                && !"CANCELADA".equals(nuevoEstado)) {
            JOptionPane.showMessageDialog(this, "Selecciona un estado válido.");
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);
        int idOrden = Integer.parseInt(
            tabla.getModel().getValueAt(filaModelo, 0).toString()
        );
        String estadoActual = tabla.getModel().getValueAt(filaModelo, 3)
            .toString().trim().toUpperCase();

        if ("PENDIENTE".equals(estadoActual)) {
            if (!"EN PROCESO".equals(nuevoEstado)
                    && !"CANCELADA".equals(nuevoEstado)) {
                JOptionPane.showMessageDialog(
                    this, "Una orden pendiente solo puede iniciar o cancelarse."
                );
                return;
            }
        } else if ("EN PROCESO".equals(estadoActual)) {
            if (!"FINALIZADA".equals(nuevoEstado)
                    && !"CANCELADA".equals(nuevoEstado)) {
                JOptionPane.showMessageDialog(
                    this, "Una orden en proceso solo puede finalizarse o cancelarse."
                );
                return;
            }
        } else {
            JOptionPane.showMessageDialog(
                this, "Esta orden ya está cerrada y no permite cambios."
            );
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Confirmas cambiar la orden " + idOrden + " a " + nuevoEstado + "?",
            "Confirmar cambio",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        boolean finalizada = false;

        try {
            ReparacionService reparacion = new ReparacionService();

            if ("EN PROCESO".equals(nuevoEstado)) {
                reparacion.iniciarOrden(idOrden);
            } else if ("FINALIZADA".equals(nuevoEstado)) {
                reparacion.finalizarOrden(idOrden);
                finalizada = true;
            } else if ("CANCELADA".equals(nuevoEstado)) {
                reparacion.cancelarOrden(idOrden);
            }

            String sqlOrdenes =
                "SELECT id_orden,id_vehiculo,id_mecanico,estado,total,problema "
                + "FROM ordenes_trabajo ORDER BY id_orden DESC";

            cargar(tabla, sqlOrdenes, 6);
            actualizarOpcionesEstado(tabla, selectorEstado);
            seleccionarOrden(tabla, idOrden);

            JOptionPane.showMessageDialog(
                this, "Orden " + idOrden + " actualizada a " + nuevoEstado + "."
            );

            if (finalizada && JOptionPane.showConfirmDialog(
                    this,
                    "¿Deseas generar el comprobante de reparación?",
                    "Comprobante",
                    JOptionPane.YES_NO_OPTION
                ) == JOptionPane.YES_OPTION) {
                generarComprobante(tabla);
            }

        } catch (Exception ex) {
            error(ex);
            cargar(
                tabla,
                "SELECT id_orden,id_vehiculo,id_mecanico,estado,total,problema "
                + "FROM ordenes_trabajo ORDER BY id_orden DESC",
                6
            );
            actualizarOpcionesEstado(tabla, selectorEstado);
        }
    }

    private int contarServiciosOrden(int idOrden) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT COUNT(*) FROM orden_servicios WHERE id_orden=?")) {
            ps.setInt(1, idOrden);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return 0;
    }

    private void actualizarTotalOrden(int idOrden) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE ordenes_trabajo SET total = "
                 + "(SELECT NVL(SUM(costo_aplicado),0) "
                 + "FROM orden_servicios WHERE id_orden=?) "
                 + "+ (SELECT NVL(SUM(cantidad * precio_aplicado),0) "
                 + "FROM orden_repuestos WHERE id_orden=?) "
                 + "WHERE id_orden=?")) {

            ps.setInt(1, idOrden);
            ps.setInt(2, idOrden);
            ps.setInt(3, idOrden);
            ps.executeUpdate();
        }
    }

    private void generarComprobante(JTable tabla) {
        int fila = tabla.getSelectedRow();

        if (fila < 0) {
            JOptionPane.showMessageDialog(
                this, "Selecciona una orden para generar su comprobante."
            );
            return;
        }

        int filaModelo = tabla.convertRowIndexToModel(fila);
        int idOrden = Integer.parseInt(
            tabla.getModel().getValueAt(filaModelo, 0).toString()
        );

        StringBuilder texto = new StringBuilder();
        texto.append("==================================================\n");
        texto.append("          TALLER EL ÚLTIMO ARRANQUE\n");
        texto.append("             COMPROBANTE DE REPARACIÓN\n");
        texto.append("==================================================\n");
        texto.append("Fecha de emisión: ")
            .append(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            )).append("\n");
        texto.append("Número de orden: ").append(idOrden).append("\n\n");

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT o.estado, o.total, o.problema, "
                 + "v.placa, v.marca, v.modelo, m.nombre AS mecanico, "
                 + "cl.nombre AS cliente, cl.telefono AS telefono_cliente, "
                 + "cl.correo AS correo_cliente "
                 + "FROM ordenes_trabajo o "
                 + "JOIN vehiculos v ON v.id_vehiculo = o.id_vehiculo "
                 + "JOIN clientes cl ON cl.id_cliente = v.id_cliente "
                 + "JOIN mecanicos m ON m.id_mecanico = o.id_mecanico "
                 + "WHERE o.id_orden = ?")) {

            ps.setInt(1, idOrden);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(
                        this, "No se encontró la orden seleccionada."
                    );
                    return;
                }

                String estado = rs.getString("estado");
                texto.append("ESTADO: ").append(estado).append("\n\n");

                texto.append("---------------- DATOS DEL CLIENTE ----------------\n");
                texto.append("Cliente: ").append(valorSeguro(rs.getString("cliente"))).append("\n");
                texto.append("Teléfono: ").append(valorSeguro(rs.getString("telefono_cliente"))).append("\n");
                texto.append("Correo: ").append(valorSeguro(rs.getString("correo_cliente"))).append("\n\n");

                texto.append("---------------- DATOS DEL VEHÍCULO ---------------\n");
                texto.append("Vehículo: ").append(valorSeguro(rs.getString("marca")))
                    .append(" ").append(valorSeguro(rs.getString("modelo"))).append("\n");
                texto.append("Placa: ").append(valorSeguro(rs.getString("placa"))).append("\n");
                texto.append("Mecánico responsable: ").append(valorSeguro(rs.getString("mecanico"))).append("\n");
                texto.append("Problema reportado: ").append(valorSeguro(rs.getString("problema"))).append("\n\n");

                texto.append("---------------- SERVICIOS REALIZADOS -------------\n");
                try (PreparedStatement psServicios = c.prepareStatement(
                    "SELECT s.descripcion, os.costo_aplicado "
                    + "FROM orden_servicios os "
                    + "JOIN servicios s ON s.id_servicio = os.id_servicio "
                    + "WHERE os.id_orden = ? ORDER BY s.descripcion")) {

                    psServicios.setInt(1, idOrden);
                    try (ResultSet rsServicios = psServicios.executeQuery()) {
                        boolean hayServicios = false;
                        while (rsServicios.next()) {
                            hayServicios = true;
                            texto.append("Servicio: ")
                                .append(valorSeguro(rsServicios.getString("descripcion")))
                                .append("\n  Costo: Q ")
                                .append(rsServicios.getBigDecimal("costo_aplicado"))
                                .append("\n");
                        }
                        if (!hayServicios) {
                            texto.append("No hay servicios registrados.\n");
                        }
                    }
                }

                texto.append("\n---------------- REPUESTOS UTILIZADOS -------------\n");
                try (PreparedStatement psRepuestos = c.prepareStatement(
                    "SELECT r.nombre, orp.cantidad, orp.precio_aplicado "
                    + "FROM orden_repuestos orp "
                    + "JOIN repuestos r ON r.id_repuesto = orp.id_repuesto "
                    + "WHERE orp.id_orden = ? ORDER BY r.nombre")) {

                    psRepuestos.setInt(1, idOrden);
                    try (ResultSet rsRepuestos = psRepuestos.executeQuery()) {
                        boolean hayRepuestos = false;
                        while (rsRepuestos.next()) {
                            hayRepuestos = true;
                            int cantidad = rsRepuestos.getInt("cantidad");
                            java.math.BigDecimal precio = rsRepuestos.getBigDecimal("precio_aplicado");
                            java.math.BigDecimal subtotal = precio.multiply(
                                java.math.BigDecimal.valueOf(cantidad)
                            );

                            texto.append("Repuesto: ")
                                .append(valorSeguro(rsRepuestos.getString("nombre"))).append("\n");
                            texto.append("  Cantidad: ").append(cantidad).append("\n");
                            texto.append("  Precio unitario: Q ").append(precio).append("\n");
                            texto.append("  Subtotal: Q ").append(subtotal).append("\n");
                        }
                        if (!hayRepuestos) {
                            texto.append("No hay repuestos registrados en esta orden.\n");
                        }
                    }
                }

                texto.append("\n==================================================\n");
                texto.append("TOTAL DE LA REPARACIÓN: Q ")
                    .append(rs.getBigDecimal("total")).append("\n");
                texto.append("Estado final: ").append(estado).append("\n");
                texto.append("==================================================\n");
                texto.append("Gracias por confiar en Taller El Último Arranque.\n");
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Guardar comprobante de reparación");
            chooser.setSelectedFile(
                new File("comprobante_orden_" + idOrden + ".txt")
            );

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File archivo = chooser.getSelectedFile();
                if (!archivo.getName().toLowerCase().endsWith(".txt")) {
                    archivo = new File(
                        archivo.getParentFile(), archivo.getName() + ".txt"
                    );
                }

                try (Writer writer = new OutputStreamWriter(
                        new FileOutputStream(archivo), StandardCharsets.UTF_8)) {
                    writer.write(texto.toString());
                }

                JOptionPane.showMessageDialog(
                    this,
                    "Comprobante mejorado guardado correctamente en:\n"
                    + archivo.getAbsolutePath(),
                    "Comprobante generado",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (Exception ex) {
            error(ex);
        }
    }

    private String valorSeguro(String valor) {
        return valor == null || valor.trim().isEmpty() ? "No registrado" : valor;
    }

    private void crearCitas() {
        String sql =
            "SELECT id_cita,id_cliente,id_vehiculo,"
            + "TO_CHAR(fecha_cita,'YYYY-MM-DD HH24:MI'),motivo,estado "
            + "FROM citas ORDER BY fecha_cita DESC";

        JPanel panel = new JPanel(new BorderLayout(6, 6));
        DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID cita", "ID cliente", "ID vehículo", "Fecha y hora", "Motivo", "Estado"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) {
                return column >= 1 && column <= 4;
            }
        };
        JTable tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton actualizar = new JButton("Actualizar");
        JButton nuevo = new JButton("Nueva cita");
        JButton guardar = new JButton("Guardar cambios");
        JButton exportar = new JButton("Exportar CSV");
        JComboBox<String> selectorEstado = new JComboBox<>();
        selectorEstado.addItem("Selecciona una cita");
        selectorEstado.setEnabled(false);
        JButton guardarEstado = new JButton("Guardar estado");

        actualizar.addActionListener(e -> cargar(tabla, sql, 6));
        nuevo.addActionListener(e -> crearNuevaCita(tabla, sql));
        guardar.addActionListener(e -> guardarCambiosTabla("Citas", tabla));
        exportar.addActionListener(e -> exportarCsv(tabla, "citas"));
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) actualizarOpcionesEstadoCita(tabla, selectorEstado);
        });
        guardarEstado.addActionListener(e -> cambiarEstadoCita(tabla, selectorEstado, sql));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        controles.add(actualizar);
        controles.add(nuevo);
        controles.add(guardar);
        controles.add(exportar);
        controles.add(new JLabel("Nuevo estado:"));
        controles.add(selectorEstado);
        controles.add(guardarEstado);
        panel.add(controles, BorderLayout.SOUTH);

        tabs.addTab("Citas", panel);
        cargar(tabla, sql, 6);
    }

    private void crearNuevaCita(JTable tabla, String sql) {
        JTextField cliente = new JTextField();
        JTextField vehiculo = new JTextField();
        JTextField fecha = new JTextField("2026-10-25 09:00");
        JTextField motivo = new JTextField();

        JPanel formulario = new JPanel(new GridLayout(4, 2, 5, 5));
        formulario.add(new JLabel("ID cliente:"));
        formulario.add(cliente);
        formulario.add(new JLabel("ID vehículo:"));
        formulario.add(vehiculo);
        formulario.add(new JLabel("Fecha (YYYY-MM-DD HH:MM):"));
        formulario.add(fecha);
        formulario.add(new JLabel("Motivo:"));
        formulario.add(motivo);

        int opcion = JOptionPane.showConfirmDialog(
            this, formulario, "Nueva cita", JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );
        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String clienteTexto = cliente.getText().trim();
            String vehiculoTexto = vehiculo.getText().trim();
            String fechaTexto = fecha.getText().trim();
            String motivoTexto = motivo.getText().trim();

            if (clienteTexto.isEmpty() || vehiculoTexto.isEmpty()
                    || fechaTexto.isEmpty() || motivoTexto.isEmpty()) {
                throw new IllegalArgumentException("Completa todos los campos.");
            }

            int idCliente = Integer.parseInt(clienteTexto);
            int idVehiculo = Integer.parseInt(vehiculoTexto);

            // Validar el formato de fecha antes de consultar Oracle.
            java.time.LocalDateTime.parse(
                fechaTexto,
                java.time.format.DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
            );

            try (Connection c = Database.getConnection()) {
                c.setAutoCommit(false);
                try {
                    // Comprobar ambas claves foráneas y que el vehículo
                    // esté registrado a nombre del cliente seleccionado.
                    String comprobar =
                        "SELECT COUNT(*) FROM vehiculos "
                        + "WHERE id_vehiculo = ? AND id_cliente = ?";
                    try (PreparedStatement ps = c.prepareStatement(comprobar)) {
                        ps.setInt(1, idVehiculo);
                        ps.setInt(2, idCliente);
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            if (rs.getInt(1) == 0) {
                                throw new SQLException(
                                    "El vehículo no existe o no pertenece al cliente indicado. "
                                    + "Revisa los ID en las pestañas Clientes y Vehículos."
                                );
                            }
                        }
                    }

                    String insertar =
                        "INSERT INTO citas "
                        + "(id_cliente, id_vehiculo, fecha_cita, motivo, estado) "
                        + "VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD HH24:MI'), ?, 'PENDIENTE')";
                    try (PreparedStatement ps = c.prepareStatement(insertar)) {
                        ps.setInt(1, idCliente);
                        ps.setInt(2, idVehiculo);
                        ps.setString(3, fechaTexto);
                        ps.setString(4, motivoTexto);
                        ps.executeUpdate();
                    }
                    c.commit();
                } catch (Exception ex) {
                    try {
                        c.rollback();
                    } catch (SQLException rollbackEx) {
                        ex.addSuppressed(rollbackEx);
                    }
                    throw ex;
                } finally {
                    try {
                        c.setAutoCommit(true);
                    } catch (SQLException ignored) {
                        // La conexión se cerrará al salir del try-with-resources.
                    }
                }
            }

            cargar(tabla, sql, 6);
            JOptionPane.showMessageDialog(
                this, "Cita creada correctamente en estado PENDIENTE."
            );
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this, "Los ID del cliente y del vehículo deben ser números enteros.",
                "Datos incorrectos", JOptionPane.WARNING_MESSAGE
            );
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                this, "La fecha debe tener el formato YYYY-MM-DD HH:MM, por ejemplo 2026-10-25 09:00.",
                "Fecha incorrecta", JOptionPane.WARNING_MESSAGE
            );
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void actualizarOpcionesEstadoCita(JTable tabla, JComboBox<String> selector) {
        selector.removeAllItems();
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            selector.addItem("Selecciona una cita");
            selector.setEnabled(false);
            return;
        }
        int mf = tabla.convertRowIndexToModel(fila);
        String estado = String.valueOf(tabla.getModel().getValueAt(mf, 5)).trim().toUpperCase();
        selector.setEnabled(true);
        if ("PENDIENTE".equals(estado)) {
            selector.addItem("CONFIRMADA");
            selector.addItem("CANCELADA");
        } else if ("CONFIRMADA".equals(estado)) {
            selector.addItem("COMPLETADA");
            selector.addItem("CANCELADA");
        } else {
            selector.addItem("Sin cambios disponibles");
            selector.setEnabled(false);
        }
    }

    private void cambiarEstadoCita(JTable tabla, JComboBox<String> selector, String sql) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona una cita primero."); return; }
        Object elegido = selector.getSelectedItem();
        if (elegido == null || !("CONFIRMADA".equals(elegido) || "COMPLETADA".equals(elegido) || "CANCELADA".equals(elegido))) {
            JOptionPane.showMessageDialog(this, "Selecciona un estado válido."); return;
        }
        int mf = tabla.convertRowIndexToModel(fila);
        int id = Integer.parseInt(tabla.getModel().getValueAt(mf, 0).toString());
        String actual = tabla.getModel().getValueAt(mf, 5).toString().trim().toUpperCase();
        String nuevo = elegido.toString();
        boolean permitido = ("PENDIENTE".equals(actual) && ("CONFIRMADA".equals(nuevo) || "CANCELADA".equals(nuevo)))
                || ("CONFIRMADA".equals(actual) && ("COMPLETADA".equals(nuevo) || "CANCELADA".equals(nuevo)));
        if (!permitido) { JOptionPane.showMessageDialog(this, "Ese cambio de estado no está permitido."); return; }
        if (JOptionPane.showConfirmDialog(this, "¿Guardar estado " + nuevo + " para la cita " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            int n = dao.execute("UPDATE citas SET estado=? WHERE id_cita=? AND estado=?", nuevo, id, actual);
            if (n != 1) throw new SQLException("No se guardó el cambio. Actualiza la tabla e inténtalo de nuevo.");
            cargar(tabla, sql, 6);
            actualizarOpcionesEstadoCita(tabla, selector);
            JOptionPane.showMessageDialog(this, "Estado de cita actualizado a " + nuevo + ".");
        } catch (Exception ex) { error(ex); }
    }

    private void guardarCambiosTabla(String nombre, JTable tabla) {
        if (tabla.isEditing()) tabla.getCellEditor().stopCellEditing();
        if (tabla.getRowCount() == 0) { JOptionPane.showMessageDialog(this, "No hay registros para guardar."); return; }
        String sql;
        switch (nombre) {
            case "Clientes": sql = "UPDATE clientes SET nombre=?,telefono=?,correo=? WHERE id_cliente=?"; break;
            case "Vehículos": sql = "UPDATE vehiculos SET placa=?,marca=?,modelo=?,id_cliente=? WHERE id_vehiculo=?"; break;
            case "Mecánicos": sql = "UPDATE mecanicos SET nombre=?,telefono=?,especialidad=? WHERE id_mecanico=?"; break;
            case "Repuestos": sql = "UPDATE repuestos SET nombre=?,descripcion=?,precio=?,existencia=? WHERE id_repuesto=?"; break;
            case "Servicios": sql = "UPDATE servicios SET descripcion=?,costo_mano_obra=? WHERE id_servicio=?"; break;
            case "Citas": sql = "UPDATE citas SET id_cliente=?,id_vehiculo=?,fecha_cita=TO_DATE(?, 'YYYY-MM-DD HH24:MI'),motivo=? WHERE id_cita=?"; break;
            case "Órdenes": sql = "UPDATE ordenes_trabajo SET id_vehiculo=?,id_mecanico=?,problema=? WHERE id_orden=?"; break;
            default: JOptionPane.showMessageDialog(this, "Esta tabla no tiene edición general habilitada."); return;
        }
        int guardados = 0;
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                for (int r = 0; r < tabla.getRowCount(); r++) {
                    int mr = tabla.convertRowIndexToModel(r);
                    if ("Clientes".equals(nombre)) {
                        ps.setString(1, celda(tabla,mr,1)); ps.setString(2, celda(tabla,mr,2)); ps.setString(3, celda(tabla,mr,3)); ps.setInt(4, entero(tabla,mr,0));
                    } else if ("Vehículos".equals(nombre)) {
                        ps.setString(1,celda(tabla,mr,1)); ps.setString(2,celda(tabla,mr,2)); ps.setString(3,celda(tabla,mr,3)); ps.setInt(4,entero(tabla,mr,4)); ps.setInt(5,entero(tabla,mr,0));
                    } else if ("Mecánicos".equals(nombre)) {
                        ps.setString(1,celda(tabla,mr,1)); ps.setString(2,celda(tabla,mr,2)); ps.setString(3,celda(tabla,mr,3)); ps.setInt(4,entero(tabla,mr,0));
                    } else if ("Repuestos".equals(nombre)) {
                        ps.setString(1,celda(tabla,mr,1)); ps.setString(2,celda(tabla,mr,2)); ps.setBigDecimal(3,new java.math.BigDecimal(celda(tabla,mr,3))); ps.setInt(4,entero(tabla,mr,4)); ps.setInt(5,entero(tabla,mr,0));
                    } else if ("Servicios".equals(nombre)) {
                        ps.setString(1,celda(tabla,mr,1)); ps.setBigDecimal(2,new java.math.BigDecimal(celda(tabla,mr,2))); ps.setInt(3,entero(tabla,mr,0));
                    } else if ("Citas".equals(nombre)) {
                        ps.setInt(1,entero(tabla,mr,1)); ps.setInt(2,entero(tabla,mr,2)); ps.setString(3,celda(tabla,mr,3)); ps.setString(4,celda(tabla,mr,4)); ps.setInt(5,entero(tabla,mr,0));
                    } else if ("Órdenes".equals(nombre)) {
                        ps.setInt(1,entero(tabla,mr,1)); ps.setInt(2,entero(tabla,mr,2)); ps.setString(3,celda(tabla,mr,5)); ps.setInt(4,entero(tabla,mr,0));
                    }
                    ps.addBatch();
                }
                int[] resultados = ps.executeBatch();
                for (int resultado : resultados) if (resultado > 0 || resultado == Statement.SUCCESS_NO_INFO) guardados++;
                c.commit();
            } catch (Exception ex) { c.rollback(); throw ex; }
            finally { try { c.setAutoCommit(true); } catch (SQLException ignored) {} }
            JOptionPane.showMessageDialog(this, "Cambios guardados correctamente en Oracle.");
        } catch (Exception ex) { error(ex); }
    }

    private String celda(JTable tabla, int fila, int columna) {
        Object valor = tabla.getModel().getValueAt(fila, columna);
        return valor == null ? "" : valor.toString().trim();
    }

    private int entero(JTable tabla, int fila, int columna) {
        return Integer.parseInt(celda(tabla, fila, columna));
    }

    private void crearReportes() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));

        String[] nombres = {
            "Órdenes por estado",
            "Inventario bajo (5 o menos)",
            "Órdenes por mecánico",
            "Ingresos por día",
            "Detalle de citas"
        };

        String[] consultas = {
            "SELECT estado,COUNT(*) FROM ordenes_trabajo GROUP BY estado",
            "SELECT id_repuesto,nombre,existencia FROM repuestos "
                + "WHERE existencia<=5 ORDER BY existencia",
            "SELECT m.nombre,COUNT(o.id_orden) "
                + "FROM mecanicos m LEFT JOIN ordenes_trabajo o "
                + "ON o.id_mecanico=m.id_mecanico "
                + "GROUP BY m.nombre ORDER BY m.nombre",
            "SELECT TO_CHAR(fecha_creacion,'YYYY-MM-DD'),SUM(total) "
                + "FROM ordenes_trabajo WHERE estado='FINALIZADA' "
                + "GROUP BY TO_CHAR(fecha_creacion,'YYYY-MM-DD') ORDER BY 1",
            "SELECT c.id_cita,cl.nombre,v.placa,c.fecha_cita,c.motivo,c.estado "
                + "FROM citas c JOIN clientes cl ON cl.id_cliente=c.id_cliente "
                + "JOIN vehiculos v ON v.id_vehiculo=c.id_vehiculo "
                + "ORDER BY c.fecha_cita DESC"
        };

        JComboBox<String> seleccion = new JComboBox<>(nombres);
        JTable tabla = new JTable();
        JButton generar = new JButton("Generar reporte");
        JButton exportar = new JButton("Exportar CSV");

        generar.addActionListener(e -> {
            int indice = seleccion.getSelectedIndex();

            try {
                List<String[]> filas = dao.query(
                    consultas[indice],
                    indice == 1 ? 3 : indice == 4 ? 6 : 2
                );

                DefaultTableModel modelo = new DefaultTableModel();

                int columnas = indice == 1 ? 3 : indice == 4 ? 6 : 2;

                for (int i = 1; i <= columnas; i++) {
                    modelo.addColumn("Columna " + i);
                }

                for (String[] fila : filas) {
                    modelo.addRow(fila);
                }

                tabla.setModel(modelo);

            } catch (SQLException ex) {
                error(ex);
            }
        });

        exportar.addActionListener(e -> exportarCsv(tabla, "reporte"));

        JPanel arriba = new JPanel();
        arriba.add(seleccion);
        arriba.add(generar);
        arriba.add(exportar);

        panel.add(arriba, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        tabs.addTab("Reportes", panel);
    }

    private void cargar(JTable tabla, String sql, int columnas) {
        try {
            List<String[]> filas = dao.query(sql, columnas);
            DefaultTableModel modelo =
                (DefaultTableModel) tabla.getModel();

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
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(nombre.toLowerCase() + ".csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                    new FileOutputStream(chooser.getSelectedFile()),
                    StandardCharsets.UTF_8))) {

            for (int col = 0; col < tabla.getColumnCount(); col++) {
                if (col > 0) {
                    writer.write(",");
                }
                writer.write(csv(tabla.getColumnName(col)));
            }
            writer.newLine();

            for (int fila = 0; fila < tabla.getRowCount(); fila++) {
                for (int col = 0; col < tabla.getColumnCount(); col++) {
                    if (col > 0) {
                        writer.write(",");
                    }

                    Object valor = tabla.getValueAt(fila, col);
                    writer.write(csv(valor == null ? "" : valor.toString()));
                }
                writer.newLine();
            }

            JOptionPane.showMessageDialog(this, "Archivo CSV exportado.");

        } catch (IOException ex) {
            error(ex);
        }
    }

    private String csv(String valor) {
        return "\"" + valor.replace("\"", "\"\"") + "\"";
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
