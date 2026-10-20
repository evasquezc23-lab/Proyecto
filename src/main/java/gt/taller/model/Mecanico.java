package gt.taller.model;
public class Mecanico extends Persona {
 private String especialidad;
 public Mecanico(int id,String nombre,String telefono,String especialidad){super(id,nombre,telefono);this.especialidad=especialidad;}
 public String getEspecialidad(){return especialidad;} public void setEspecialidad(String e){especialidad=e;}
 @Override public String getTipo(){return "Mecánico";}
}
