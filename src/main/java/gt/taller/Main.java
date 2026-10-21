
package gt.taller;

import gt.taller.ui.MainFrame;
import gt.taller.config.Database;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {

    public static void main(String[] args) {

        try {
            Database.testConnection();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(
                null,
                "Error de conexión con Oracle: " + e.getMessage(),
                "Error de base de datos",
                javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );
            } catch (ClassNotFoundException
                    | IllegalAccessException
                    | InstantiationException
                    | UnsupportedLookAndFeelException e) {
                System.out.println(e.getMessage());
            }

            new MainFrame().setVisible(true);
        });
    }
}

