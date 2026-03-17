import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.*;

public class EmployeeClientGUI extends JFrame {

    private JTable table;
    private DefaultTableModel model;
    private File selectedFile;

    public EmployeeClientGUI() {

        setTitle("Employee CSV Uploader - Meharry MSDS 535");
        setSize(650, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JButton chooseButton = new JButton("Choose CSV File");
        JButton sendButton = new JButton("Send to Server");
        JLabel statusLabel = new JLabel("No file selected.");

        JPanel panel = new JPanel();
        panel.add(chooseButton);
        panel.add(sendButton);
        panel.add(statusLabel);

        model = new DefaultTableModel(
                new String[]{"ID", "Name", "Department", "Salary"}, 0);
        table = new JTable(model);
        table.setFillsViewportHeight(true);

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        chooseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
                statusLabel.setText("Selected: " + selectedFile.getName());
            }
        });

        sendButton.addActionListener(e -> {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(this,
                    "Please choose a CSV file first!");
                return;
            }
            try {
                Socket socket = new Socket("localhost", 6000);
                PrintWriter writer = new PrintWriter(
                    socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                BufferedReader fileReader = new BufferedReader(
                    new FileReader(selectedFile));

                String line;
                while ((line = fileReader.readLine()) != null) {
                    writer.println(line);
                }
                writer.println("END");

                model.setRowCount(0);
                while (!(line = reader.readLine()).equals("END")) {
                    String[] data = line.split(",");
                    model.addRow(data);
                }

                socket.close();
                fileReader.close();
                statusLabel.setText("✓ " + model.getRowCount()
                    + " records loaded from database.");
                JOptionPane.showMessageDialog(this,
                    "Success! " + model.getRowCount()
                    + " records retrieved from MySQL.");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage());
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new EmployeeClientGUI().setVisible(true));
    }
}