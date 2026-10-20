package gt.taller.model;
public class Cliente extends Persona {
 private String correo;
 public Cliente(int id,String nombre,String telefono,String correo){super(id,nombre,telefono);this.correo=correo;}
 public String getCorreo(){return correo;} public void setCorreo(String c){correo=c;}
 @Override public String getTipo(){return "Cliente";}
}
