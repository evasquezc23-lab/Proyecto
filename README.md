# Taller El Último Arranque
Proyecto académico Java Swing + Oracle JDBC para gestionar clientes, vehículos, mecánicos, repuestos, servicios y órdenes.

## Abrir en NetBeans
1. Descomprime el ZIP.
2. En NetBeans selecciona File > Open Project y elige esta carpeta Maven.
3. Espera a que Maven descargue ojdbc11.
4. Ejecuta `database/schema.sql` en SQL Developer, conectado a tu esquema Oracle.
5. Configura `src/main/java/gt/taller/config/Database.java` con URL, usuario y contraseña reales.
6. Ejecuta `gt.taller.Main`.

La URL de ejemplo no conecta hasta reemplazar HOST, SERVICE, USUARIO_ORACLE y CAMBIAR_PASSWORD. Para Oracle Cloud Autonomous Database debes usar el método de conexión y wallet indicado por Oracle.

Incluye UI Swing, operaciones JDBC con PreparedStatement, tablas JTable, exportación CSV, reportes, SQL y clases Persona/Cliente/Mecanico. Es una base funcional académica que debes probar con una instancia Oracle real. No inventa commits: registra cinco commits reales en diferentes días mientras avances.
