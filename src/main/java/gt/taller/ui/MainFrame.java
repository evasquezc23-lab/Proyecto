package gt.taller.ui;
import gt.taller.config.Database;
import gt.taller.dao.GenericTableDao;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
public class MainFrame extends JFrame {
 private final GenericTableDao dao=new GenericTableDao(); private final JTabbedPane tabs=new JTabbedPane();
 public MainFrame(){
  setTitle("Taller El Último Arranque");setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);setSize(1050,680);setLocationRelativeTo(null);
  JPanel home=new JPanel(new BorderLayout(10,10));home.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));
  home.add(new JLabel("<html><h1>Taller El Último Arranque</h1><p>Gestión de clientes, vehículos, inventario y órdenes</p></html>"),BorderLayout.NORTH);
  JTextArea info=new JTextArea("Aplicación académica Java Swing + Oracle JDBC.\nConfigura Database.java y ejecuta database/schema.sql antes de usar.");info.setEditable(false);home.add(info,BorderLayout.CENTER);
  JButton test=new JButton("Probar conexión Oracle");test.addActionListener(e->{try{Database.testConnection();JOptionPane.showMessageDialog(this,"Conexión Oracle correcta.");}catch(SQLException x){error(x);}});home.add(test,BorderLayout.SOUTH);tabs.addTab("Inicio",home);
  addTab("Clientes","SELECT id_cliente,nombre,telefono,correo FROM clientes ORDER BY id_cliente",new String[]{"ID","Nombre","Teléfono","Correo"},4,"INSERT INTO clientes(nombre,telefono,correo) VALUES(?,?,?)",new String[]{"Nombre","Teléfono","Correo"});
  addTab("Vehículos","SELECT id_vehiculo,placa,marca,modelo,id_cliente FROM vehiculos ORDER BY id_vehiculo",new String[]{"ID","Placa","Marca","Modelo","ID cliente"},5,"INSERT INTO vehiculos(placa,marca,modelo,id_cliente) VALUES(?,?,?,?)",new String[]{"Placa","Marca","Modelo","ID cliente"});
  addTab("Mecánicos","SELECT id_mecanico,nombre,telefono,especialidad FROM mecanicos ORDER BY id_mecanico",new String[]{"ID","Nombre","Teléfono","Especialidad"},4,"INSERT INTO mecanicos(nombre,telefono,especialidad) VALUES(?,?,?)",new String[]{"Nombre","Teléfono","Especialidad"});
  addTab("Repuestos","SELECT id_repuesto,nombre,descripcion,precio,existencia FROM repuestos ORDER BY id_repuesto",new String[]{"ID","Nombre","Descripción","Precio","Existencia"},5,"INSERT INTO repuestos(nombre,descripcion,precio,existencia) VALUES(?,?,?,?)",new String[]{"Nombre","Descripción","Precio","Existencia"});
  addTab("Servicios","SELECT id_servicio,descripcion,costo_mano_obra FROM servicios ORDER BY id_servicio",new String[]{"ID","Servicio","Mano de obra"},3,"INSERT INTO servicios(descripcion,costo_mano_obra) VALUES(?,?)",new String[]{"Descripción","Costo mano de obra"});
  addTab("Órdenes","SELECT id_orden,id_vehiculo,id_mecanico,estado,total,problema FROM ordenes_trabajo ORDER BY id_orden DESC",new String[]{"ID","ID vehículo","ID mecánico","Estado","Total","Problema"},6,"INSERT INTO ordenes_trabajo(id_vehiculo,id_mecanico,problema,estado,total) VALUES(?,?,?,'PENDIENTE',0)",new String[]{"ID vehículo","ID mecánico","Problema"});
  tabs.addTab("Reportes",reports());add(tabs,BorderLayout.CENTER);
 }
 private void addTab(String name,String sql,String[] heads,int cols,String insert,String[] fields){
  JPanel panel=new JPanel(new BorderLayout(6,6));JTable table=new JTable(new DefaultTableModel(heads,0));panel.add(new JScrollPane(table),BorderLayout.CENTER);
  JPanel controls=new JPanel();JButton refresh=new JButton("Actualizar"),add=new JButton("Nuevo"),csv=new JButton("Exportar CSV");
  refresh.addActionListener(e->load(table,sql,cols));add.addActionListener(e->{
   JTextField[] f=new JTextField[fields.length];JPanel form=new JPanel(new GridLayout(fields.length,2,5,5));
   for(int i=0;i<f.length;i++){form.add(new JLabel(fields[i]));f[i]=new JTextField();form.add(f[i]);}
   if(JOptionPane.showConfirmDialog(this,form,"Nuevo "+name,JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION)try{
    Object[] v=new Object[f.length];for(int i=0;i<f.length;i++){String s=f[i].getText().trim();if(s.isEmpty())throw new IllegalArgumentException("Completa "+fields[i]);String label=fields[i].toLowerCase();v[i]=(label.contains("id ")||label.contains("precio")||label.contains("existencia")||label.contains("costo"))?Double.valueOf(s):s;}
    dao.execute(insert,v);load(table,sql,cols);JOptionPane.showMessageDialog(this,"Registro guardado.");
   }catch(Exception ex){error(ex);}
  });csv.addActionListener(e->exportCsv(table,name));controls.add(refresh);controls.add(add);controls.add(csv);panel.add(controls,BorderLayout.SOUTH);tabs.addTab(name,panel);load(table,sql,cols);
 }
 private JPanel reports(){
  JPanel p=new JPanel(new BorderLayout());String[] names={"Órdenes por estado","Inventario bajo (<=5)","Órdenes por mecánico","Ingresos por día"};
  String[] sql={"SELECT estado,COUNT(*) FROM ordenes_trabajo GROUP BY estado","SELECT id_repuesto,nombre,existencia FROM repuestos WHERE existencia<=5 ORDER BY existencia","SELECT m.nombre,COUNT(o.id_orden) FROM mecanicos m LEFT JOIN ordenes_trabajo o ON o.id_mecanico=m.id_mecanico GROUP BY m.nombre ORDER BY m.nombre","SELECT TO_CHAR(fecha_creacion,'YYYY-MM-DD'),SUM(total) FROM ordenes_trabajo WHERE estado='FINALIZADA' GROUP BY TO_CHAR(fecha_creacion,'YYYY-MM-DD') ORDER BY 1"};
  JComboBox<String> choice=new JComboBox<>(names);JTable t=new JTable();JButton run=new JButton("Generar");run.addActionListener(e->{int i=choice.getSelectedIndex(),n=i==1?3:2;try{List<String[]> rows=dao.query(sql[i],n);DefaultTableModel m=new DefaultTableModel();for(int j=1;j<=n;j++)m.addColumn("Columna "+j);for(String[] row:rows)m.addRow(row);t.setModel(m);}catch(SQLException ex){error(ex);}});
  JPanel top=new JPanel();top.add(choice);top.add(run);p.add(top,BorderLayout.NORTH);p.add(new JScrollPane(t),BorderLayout.CENTER);return p;
 }
 private void load(JTable t,String sql,int cols){try{List<String[]> rows=dao.query(sql,cols);DefaultTableModel m=(DefaultTableModel)t.getModel();m.setRowCount(0);for(String[] r:rows)m.addRow(r);}catch(SQLException ex){if(isShowing())error(ex);}}
 private void exportCsv(JTable t,String name){JFileChooser c=new JFileChooser();c.setSelectedFile(new File(name.toLowerCase()+".csv"));if(c.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
  try(BufferedWriter w=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(c.getSelectedFile()),StandardCharsets.UTF_8))){for(int j=0;j<t.getColumnCount();j++){if(j>0)w.write(",");w.write(q(t.getColumnName(j)));}w.newLine();for(int i=0;i<t.getRowCount();i++){for(int j=0;j<t.getColumnCount();j++){if(j>0)w.write(",");Object v=t.getValueAt(i,j);w.write(q(v==null?"":v.toString()));}w.newLine();}JOptionPane.showMessageDialog(this,"CSV exportado.");}catch(IOException ex){error(ex);}
 }
 private String q(String s){return "\""+s.replace("\"","\"\"")+"\"";}
 private void error(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
}
