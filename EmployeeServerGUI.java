import java.io.*;
import java.net.*;
import java.sql.*;

public class EmployeeServerGUI {

    static final String URL = "jdbc:mysql://localhost:3306/company";
    static final String USER = "root";
    static final String PASSWORD = "password";

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(6000);
        System.out.println("Server Started. Waiting for client...");

        Socket socket = serverSocket.accept();
        System.out.println("Client Connected!");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        conn.setAutoCommit(false);

        String insertSQL = "INSERT INTO employees VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(insertSQL);

        String line;
        reader.readLine(); // Skip CSV header row

        while ((line = reader.readLine()) != null && !line.equals("END")) {
            String[] data = line.split(",");
            ps.setInt(1, Integer.parseInt(data[0].trim()));
            ps.setString(2, data[1].trim());
            ps.setString(3, data[2].trim());
            ps.setDouble(4, Double.parseDouble(data[3].trim()));
            ps.addBatch();
        }

        ps.executeBatch();
        conn.commit();
        System.out.println("CSV data inserted into MySQL via JDBC.");

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM employees");

        int count = 0;
        while (rs.next()) {
            writer.println(
                rs.getInt("id") + "," +
                rs.getString("name") + "," +
                rs.getString("department") + "," +
                rs.getDouble("salary")
            );
            count++;
        }

        writer.println("END");
        System.out.println("Sent " + count + " records back to client.");

        rs.close();
        conn.close();
        socket.close();
        serverSocket.close();
        System.out.println("Connection closed.");
    }
}