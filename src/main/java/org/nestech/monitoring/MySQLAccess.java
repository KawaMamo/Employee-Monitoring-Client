package org.nestech.monitoring;

import com.zkteco.biometric.FingerprintSensorEx;

import java.sql.*;

public class MySQLAccess {

    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    public void connect() throws Exception{
        // load config file
        String fileName = "app.config";
        Config config = new Config(fileName);
        String serverIp = config.prop.getProperty("serverIp");
        String dbName = config.prop.getProperty("dbName");
        String dbUser = config.prop.getProperty("dbUser");
        String dbPassword = config.prop.getProperty("dbPassword");
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Set up the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://"+serverIp+"/"+dbName+"?useUnicode=yes&characterEncoding=UTF-8&useSSL=false&"
                            + "user="+dbUser+"&password="+dbPassword+"");

            // Statements allow to issue SQL queries to the database
            statement = connect.createStatement();
        }catch (Exception e){
            throw e;
        }
    }

    // You need to close the resultSet
    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception e) {

        }
    }

    public void getTemplate(long mhDB) throws Exception {
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT id, FPBlob from employees");
            resultSet = preparedStatement.executeQuery();
            byte[] template = null;
            int id =1000;
            while (resultSet.next()) {
                int idFP = resultSet.getInt("id");
                template = resultSet.getBytes("FPBlob");
                FingerprintSensorEx.DBAdd(mhDB, idFP, template);

                id++;
            }

        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }

    }

    public String getData(int id) throws Exception {
        String name =null;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT * from employees WHERE id = "+id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                name = resultSet.getString("fullName");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return name;
    }

    public int getDataOfEmp(int empId) throws Exception {
        int fingers =0;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT COUNT(id) as fingers from employees WHERE empId = "+empId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                fingers = resultSet.getInt("fingers");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return fingers;
    }

    public int getId(int id) throws Exception {
        int empId =0;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT empId from employees WHERE id = "+id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                empId = resultSet.getInt("empId");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return empId;
    }

    public int getMaxId() throws Exception {
        int idMax =0;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT COUNT(id) from employees");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                idMax = resultSet.getInt(1);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return idMax;
    }

    public void insertBlob(byte[] template, int id, String fullName) throws Exception{
        try {
            connect();
            // PreparedStatements can use variables and are more efficient
            preparedStatement = connect
                    .prepareStatement("insert into employees values (default, ?, ?, ?)", statement.RETURN_GENERATED_KEYS);
            // Parameters start with 1
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, fullName);
            preparedStatement.setBytes(3, template);
            int rowAffected = preparedStatement.executeUpdate();

            ResultSet rs = null;
            int candidateId = 0;
            if(rowAffected == 1)
            {
                // get candidate id
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    candidateId = rs.getInt(1);
            }

        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
    }

}
