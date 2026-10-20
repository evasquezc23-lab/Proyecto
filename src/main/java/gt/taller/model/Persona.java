package gt.taller.model;
public abstract class Persona {
 private int id; private String nombre; private String telefono;
 protected Persona(int id,String nombre,String telefono){this.id=id;this.nombre=nombre;this.telefono=telefono;}
 public int getId(){return id;} public String getNombre(){return nombre;} public String getTelefono(){return telefono;}
 public void setNombre(String n){nombre=n;} public void setTelefono(String t){telefono=t;}
 public abstract String getTipo();
}
