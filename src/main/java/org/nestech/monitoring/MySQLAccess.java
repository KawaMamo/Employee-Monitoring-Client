package org.nestech.monitoring;

import com.zkteco.biometric.FingerprintSensorEx;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MySQLAccess {

    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    public void connect() throws Exception{
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.jdbc.Driver");
            // Set up the connection with the DB
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/attendance?useUnicode=yes&characterEncoding=UTF-8&useSSL=false&"
                            + "user=bankUser&password=P@ssw0rd1978");


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

    public int getMaxId() throws Exception {
        int idMax =0;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT MAX(id) from users_data");
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



    public boolean isAvailable(String boxNumber) throws Exception {
        String status = null;
        boolean returned = true;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT status from safeboxes WHERE boxNumber = '"+boxNumber+"'");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                status = resultSet.getString("status");
                if(status.equals("unavailable")){
                    returned = false;
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return returned;
    }


    public boolean isVacation(LocalDate today) throws Exception {
        boolean returned = false;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT reason from vacations WHERE vacation_day = '"+today+"'");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                returned = true;

            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return returned;
    }

    public LocalDate getStartDay(int shift_id){
        LocalDate startDay = null;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT startingFrom FROM shifts WHERE shift_id = ?");
            preparedStatement.setInt(1,shift_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                startDay = LocalDate.parse(resultSet.getString("startingFrom"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return startDay;
    }

    public int getCycleDays(int shift_id){
        int cycleDays = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT cycleDays FROM shifts WHERE shift_id = ?");
            preparedStatement.setInt(1,shift_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                cycleDays = resultSet.getInt("cycleDays");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return cycleDays;
    }

    public int getAcceptedDelay(int shift_id){
        int acceptedDelay = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT acceptedDelay FROM shifts WHERE shift_id = ?");
            preparedStatement.setInt(1,shift_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                acceptedDelay = resultSet.getInt("acceptedDelay");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return acceptedDelay;
    }


    public int getExtraMinutes(int shift_id){
        int acceptedDelay = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT extraMinutes FROM shifts WHERE shift_id = ?");
            preparedStatement.setInt(1,shift_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                acceptedDelay = resultSet.getInt("extraMinutes");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return acceptedDelay;
    }

    public int getShiftDays(int shift_id){
        int shiftDays = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT SUM(shiftDays) AS days FROM sub_shifts WHERE shift_id = ?");
            preparedStatement.setInt(1,shift_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                shiftDays = resultSet.getInt("days");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return shiftDays;
    }



    public int getShiftDays(int shift_id, int shiftNumber){
        int shiftDays = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT shiftDays FROM sub_shifts WHERE shift_id = ? ORDER BY sub_id LIMIT 1 OFFSET ?");
            preparedStatement.setInt(1,shift_id);
            preparedStatement.setInt(2, shiftNumber);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                shiftDays = resultSet.getInt("shiftDays");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return shiftDays;
    }

    public int getOnDaysInShift(int shift_id){
        int onDaysInShift = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT onDaysInShift FROM sub_shifts WHERE shift_id = ?");
            preparedStatement.setInt(1,shift_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                onDaysInShift = resultSet.getInt("onDaysInShift");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return onDaysInShift;
    }

    public int getOnDaysInShift(int shift_id, int dayNumber){
        int onDaysInShift = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT onDaysInShift, shiftDays FROM sub_shifts WHERE shift_id = ?");
            preparedStatement.setInt(1,shift_id);
            resultSet = preparedStatement.executeQuery();
            int sumOfDays = 0;
            while (resultSet.next()){
                sumOfDays += resultSet.getInt("shiftDays");
                if(dayNumber <= sumOfDays){
                    onDaysInShift = resultSet.getInt("onDaysInShift");
                    System.out.println("onDaysInShift "+onDaysInShift);
                    break;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return onDaysInShift;
    }

    public int getDailyHours(int shift_id){
        int dailyHours = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT dailyHours FROM sub_shifts WHERE shift_id = ?");
            preparedStatement.setInt(1,shift_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                dailyHours = resultSet.getInt("dailyHours");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return dailyHours;
    }


    public int getDailyHours(int shift_id, int sub_shift_number){
        int dailyHours = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT dailyHours FROM sub_shifts WHERE shift_id = ? ORDER BY sub_id LIMIT 1 OFFSET ?");

            preparedStatement.setInt(1,shift_id);
            preparedStatement.setInt(2, sub_shift_number);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                dailyHours = resultSet.getInt("dailyHours");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return dailyHours;
    }

    public int getDailyMinutes(int shift_id, int sub_shift_number){
        int dailyHours = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT dailyMinutes FROM sub_shifts WHERE shift_id = ? ORDER BY sub_id LIMIT 1 OFFSET ?");

            preparedStatement.setInt(1,shift_id);
            preparedStatement.setInt(2, sub_shift_number);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                dailyHours = resultSet.getInt("dailyMinutes");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return dailyHours;
    }


    public boolean isFirstMove(int employee_id){
        boolean movement_type = true;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT movement_type FROM movements WHERE employee_id = ? AND DATE(movement_date) = ?");
            preparedStatement.setInt(1,employee_id);
            preparedStatement.setString(2, LocalDate.now().toString());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                movement_type = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return movement_type;
    }

    public Time getStartingHour(int shift_id){
        Time startingHour = null;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT startingHour FROM shifts WHERE shift_id = ?");
            preparedStatement.setInt(1,shift_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                startingHour = resultSet.getTime("startingHour");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return startingHour;
    }

    public Time getStartingHour(int shift_id, int subShiftNumber){
        Time startingHour = null;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT startingHour FROM sub_shifts WHERE shift_id = ? ORDER BY sub_id LIMIT 1 OFFSET ?");
            preparedStatement.setInt(1,shift_id);
            preparedStatement.setInt(2, subShiftNumber);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                startingHour = resultSet.getTime("startingHour");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return startingHour;
    }

    public int getDisplacementHours(int shift_id){
        int displacementHours = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT displacementHours FROM shifts WHERE shift_id = ?");
            preparedStatement.setInt(1,shift_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                displacementHours = resultSet.getInt("displacementHours");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return displacementHours;
    }

    public void getTemplate(long mhDB) throws Exception {
        try {
            connect();

            preparedStatement = connect
                    .prepareStatement("SELECT id, FPBlob from users_data");
            resultSet = preparedStatement.executeQuery();
            byte[] template = null;
            int id =1000;
            while (resultSet.next()) {
                // It is possible to get the columns via name
                // also possible to get the columns via the column number
                // which starts at 1
                // e.g. resultSet.getSTring(2);
                int idFP = resultSet.getInt("id");
                template = resultSet.getBytes("FPBlob");
                FingerprintSensorEx.DBAdd(mhDB, idFP, template);

                id++;
            }
            //return template;

        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }

    }

    public int getAccountType(int id) throws Exception {

        int name =0;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT * from users_data WHERE id = "+id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                name = resultSet.getInt("accountType");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return name;
    }

    public ResultSet getSalaryCat() throws Exception {
        ResultSet resultSet = null;
        int step = 1;
        String query = "SELECT category FROM salary_categories";
        Statement statement = connect.createStatement();
        resultSet = statement.executeQuery(query);
        return resultSet;
    }

    public ResultSet getShiftNames() throws Exception {
        ResultSet resultSet = null;
        String query = "SELECT shiftName FROM shifts";
        Statement statement = connect.createStatement();
        resultSet = statement.executeQuery(query);
        return resultSet;
    }

    public ResultSet getSubShifts(int shift_id) throws Exception {
        connect();
        ResultSet resultSet = null;
        int step = 1;
        String query = "SELECT shiftDays FROM sub_shifts WHERE shift_id = ?";
        PreparedStatement preparedStatement = connect.prepareStatement(query);
        preparedStatement.setInt(1, shift_id);
        resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    public ResultSet getSubShift(int shift_id) throws Exception {
        connect();
        ResultSet resultSet = null;
        int step = 1;
        String query = "SELECT * FROM sub_shifts WHERE shift_id = ?";
        PreparedStatement preparedStatement = connect.prepareStatement(query);
        preparedStatement.setInt(1, shift_id);
        resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    public ResultSet getShift(int shift_id) throws Exception {
        connect();
        ResultSet resultSet = null;
        int step = 1;
        String query = "SELECT * FROM shifts WHERE shift_id = ?";
        PreparedStatement preparedStatement = connect.prepareStatement(query);
        preparedStatement.setInt(1, shift_id);
        resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    public ResultSet getShifts() throws Exception {
        connect();
        ResultSet resultSet = null;
        int step = 1;
        String query = "SELECT * FROM shifts";
        PreparedStatement preparedStatement = connect.prepareStatement(query);
        resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    public void insertBlob(byte[] template, int id, String fullName, String father, String mother, String nationalId, String birthPlace, int shift_id, String branch, String fNumber, String sNumber, String salary_category) throws Exception{
        try {
            connect();
            // PreparedStatements can use variables and are more efficient
            preparedStatement = connect
                    .prepareStatement("insert into users_data values (default, ?, ?, ?, ?, ?, ?, ?, ?, default, default, ?)", statement.RETURN_GENERATED_KEYS);
            // Parameters start with 1
            preparedStatement.setString(1, fullName);
            preparedStatement.setString(2, father);
            preparedStatement.setString(3, mother);
            preparedStatement.setString(4, nationalId);
            preparedStatement.setString(5, birthPlace);
            preparedStatement.setInt(6, shift_id);
            preparedStatement.setString(7, branch);
            preparedStatement.setString(8, salary_category);
            preparedStatement.setBytes(9, template);
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

    public void updateFP(byte[] template, int id) throws Exception{
        try {
            connect();
            // PreparedStatements can use variables and are more efficient
            preparedStatement = connect
                    .prepareStatement("UPDATE users_data SET FPBlob = ? WHERE id = ?", statement.RETURN_GENERATED_KEYS);
            // Parameters start with 1
            preparedStatement.setBytes(1, template);
            preparedStatement.setInt(2, id);
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

    public int authenticate(String userName, String password) throws SQLException {

        preparedStatement = connect
                .prepareStatement("SELECT customerId, FPBlob from comments");
        resultSet = preparedStatement.executeQuery();

        return 0;
    }



    public int insertLoans(String loanNameV, String loanType, String interestType, int minMonthsV, int maxMonthsV, double interestV, double prePaidV, int monthly, int fourthAnnual, int semiAnnual, int annual) throws Exception{
        connect();
        int candidateId = 0;
        try {
            //connect();
            // PreparedStatements can use variables and are more efficient
            preparedStatement = connect
                    .prepareStatement("insert into loans values (default, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, default, default)", statement.RETURN_GENERATED_KEYS);
            // "idFP, fullName, nationalId, summary, center from test.comments");
            // Parameters start with 1
            preparedStatement.setString(1, loanNameV);
            preparedStatement.setString(2, loanType);
            preparedStatement.setString(3, interestType);
            preparedStatement.setInt(4, minMonthsV);
            preparedStatement.setInt(5, maxMonthsV);
            preparedStatement.setDouble(6, interestV);
            preparedStatement.setDouble(7, prePaidV);
            preparedStatement.setInt(8, monthly);
            preparedStatement.setInt(9, fourthAnnual);
            preparedStatement.setInt(10, semiAnnual);
            preparedStatement.setInt(11, annual);

            int rowAffected = preparedStatement.executeUpdate();

            ResultSet rs = null;

            if(rowAffected == 1)
            {
                // get candidate id
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    candidateId = rs.getInt(1);
            }
            System.out.print("insertLoans: "+candidateId);


        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        close();
        return candidateId;
    }

    public String getData(int id) throws Exception {
        String name =null;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT * from users_data WHERE id = "+id);
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

    public String getNation(int id) throws Exception {

        String name =null;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT * from comments WHERE id = "+id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                name = resultSet.getString("nationalId");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return name;
    }

    public String getSalary(int id) throws Exception {

        String name =null;
        try {
            connect();

            preparedStatement = connect
                    .prepareStatement("SELECT * from comments WHERE id = "+id);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                name = resultSet.getString("salary");


            }
            //return template;

        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return name;

    }

    public String getFood(int id) throws Exception {

        String name =null;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT * from comments WHERE id = "+id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                name = resultSet.getString("foodReimbursementInsert");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return name;

    }

    public String getSmoke(int id) throws Exception {

        String name =null;
        try {
            connect();

            preparedStatement = connect
                    .prepareStatement("SELECT * from comments WHERE id = "+id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                name = resultSet.getString("smokingReimbursementInsert");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return name;

    }

    public String getExtra(int id) throws Exception {

        String name =null;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT * from comments WHERE id = "+id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                name = resultSet.getString("extraSalaryInsert");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return name;

    }

    public String getJob(int id) throws Exception {
        String name =null;
        try {
            connect();

            preparedStatement = connect
                    .prepareStatement("SELECT * from comments WHERE id = "+id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                name = resultSet.getString("jobTitle");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return name;
    }

    public String getCenter(int id) throws Exception {
        String name =null;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT * from comments WHERE id = "+id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                name = resultSet.getString("center");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return name;
    }

    public String getDate(int id) throws Exception {
        String name =null;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT insertStartDate from comments WHERE id = "+id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                name = resultSet.getString(1);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return name;
    }

    public String getStatus(int id, String dateString) throws Exception {
        String name =null;
        String status = "Not Paid";
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("SELECT monthYear from payments WHERE idFP = "+id);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                name = resultSet.getString(1);
                if(name.equals(dateString)){
                    status = "Paid";
                }
            }

        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return status;
    }

    public ResultSet getResultSet() throws Exception {
        ResultSet resultSet = null;
        connect();
        String query = "SELECT u.id, u.fullName, u.father, u.mother, u.nationalId, u.birthPlace, u.accountType, u.salary_category, u.branchName, u.dateOfInsertion," +
                " u.activated, s.shiftName FROM users_data u LEFT JOIN shifts s ON u.accountType = s.shift_id" +
                "  ORDER BY u.id DESC";
        Statement statement = connect.createStatement();
        resultSet = statement.executeQuery(query);

        return resultSet;

    }

    public ResultSet getResultSet(String name) throws Exception {
        ResultSet resultSet = null;
        connect();
        String query = "SELECT u.id, u.fullName, u.father, u.mother, u.nationalId, u.birthPlace, u.accountType, u.salary_category, u.branchName, u.dateOfInsertion," +
                " u.activated, s.shiftName " +
                "FROM users_data u " +
                "LEFT JOIN shifts s ON u.accountType = s.shift_id " +
                "WHERE u.fullName LIKE ? ORDER BY u.id DESC";
        PreparedStatement preparedStatement = connect.prepareStatement(query);
        preparedStatement.setString(1, "%"+name+"%");
        resultSet = preparedStatement.executeQuery();

        return resultSet;

    }

    public ResultSet getResultSetLoan() throws Exception {
        ResultSet resultSet = null;
        connect();
        String query = "SELECT id, loanNameV, loanType, interestType, minMonthsV, maxMonthsV, interestV, prePaidV, insertionDate, monthly, fourthAnnual, semiAnnual, annual, activated FROM loans ORDER BY id DESC";
        Statement statement = connect.createStatement();
        resultSet = statement.executeQuery(query);

        return resultSet;

    }

    public ResultSet getCenters() throws Exception {
        ResultSet resultSet = null;
        connect();
        String query = "SELECT * FROM centers ORDER BY id DESC";
        Statement statement = connect.createStatement();
        resultSet = statement.executeQuery(query);

        return resultSet;

    }

    public void setActive(int id) throws Exception{
        connect();
        String query = "UPDATE users_data SET activated = 'yes' WHERE id = '"+id+"'";
        PreparedStatement preparedStatement = connect.prepareStatement(query);
        preparedStatement.executeUpdate();
        connect.close();
    }

    public void setActiveLoan(int id) throws Exception{
        connect();
        String query = "UPDATE loans SET activated = 'yes' WHERE id = '"+id+"'";
        PreparedStatement preparedStatement = connect.prepareStatement(query);
        preparedStatement.executeUpdate();
        connect.close();
    }

    public void setDeactivated(int id) throws Exception{
        connect();
        String query = "UPDATE users_data SET activated = 'no' WHERE id = '"+id+"'";
        PreparedStatement preparedStatement = connect.prepareStatement(query);
        preparedStatement.executeUpdate();
        connect.close();
    }

    public void setDeactivatedLoan(int id) throws Exception{
        connect();
        String query = "UPDATE loans SET activated = 'no' WHERE id = '"+id+"'";
        PreparedStatement preparedStatement = connect.prepareStatement(query);
        preparedStatement.executeUpdate();
        connect.close();
    }

    public int insertDayOff(int employee_id, String dayOffDate) throws Exception{
        connect();
        int candidateId = 0;
        try {
            preparedStatement = connect
                    .prepareStatement("insert into dayoff values (default, ?, ?, default)", statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, employee_id);
            preparedStatement.setString(2, dayOffDate);
            int rowAffected = preparedStatement.executeUpdate();
            ResultSet rs = null;
            if(rowAffected == 1)
            {
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    candidateId = rs.getInt(1);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        close();
        return candidateId;
    }


    public int insertShift(int cycleDays, String startingFrom, int acceptedDelay, int extraMinutes, String shiftName) throws Exception{
        connect();
        int candidateId = 0;
        try {
            preparedStatement = connect
                    .prepareStatement("insert into shifts values (default, ?, ?, ?, ?, ?)", statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, cycleDays);
            preparedStatement.setString(2, startingFrom);
            preparedStatement.setInt(3, acceptedDelay);
            preparedStatement.setInt(4, extraMinutes);
            preparedStatement.setString(5, shiftName);
            int rowAffected = preparedStatement.executeUpdate();
            ResultSet rs = null;
            if(rowAffected == 1)
            {
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    candidateId = rs.getInt(1);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        close();
        return candidateId;
    }

    public int insertSubShift(int shift_id, int shiftDays, int onDaysInShift, String startingHour, int dailyHours, int dailyMinutes) throws Exception{
        connect();
        int candidateId = 0;
        try {
            preparedStatement = connect
                    .prepareStatement("insert into sub_shifts values (default, ?, ?, ?, ?, ?, ?)", statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, shift_id);
            preparedStatement.setInt(2, shiftDays);
            preparedStatement.setInt(3, onDaysInShift);
            preparedStatement.setString(4, startingHour);
            preparedStatement.setInt(5, dailyHours);
            preparedStatement.setInt(6, dailyMinutes);
            int rowAffected = preparedStatement.executeUpdate();
            ResultSet rs = null;
            if(rowAffected == 1)
            {
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    candidateId = rs.getInt(1);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        close();
        return candidateId;
    }

    public int updateSubShift(int sub_id , int shiftDays, int onDaysInShift, String startingHour, int dailyHours, int dailyMinutes) throws Exception {
        int check = 0;
        connect();
        String query = "UPDATE sub_shifts SET shiftDays = ? , onDaysInShift = ?, startingHour = ?, " +
                " dailyHours = ?, dailyMinutes = ?  WHERE sub_id  = ?";
        PreparedStatement preparedStatement = connect.prepareStatement(query, statement.RETURN_GENERATED_KEYS);
        preparedStatement.setInt(1, shiftDays);
        preparedStatement.setInt(2, onDaysInShift);
        preparedStatement.setString(3, startingHour);
        preparedStatement.setInt(4, dailyHours);
        preparedStatement.setInt(5, dailyMinutes);
        preparedStatement.setInt(6, sub_id );
        int rowAffected = preparedStatement.executeUpdate();
        if(rowAffected != 0){
            check = 1;
        }
        connect.close();
        return check;
    }

    public int insertCenter(String centerName) throws Exception{
        connect();
        int candidateId = 0;
        try {
            preparedStatement = connect
                    .prepareStatement("insert into centers values (default, ?)", statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, centerName);
            int rowAffected = preparedStatement.executeUpdate();
            ResultSet rs = null;
            if(rowAffected == 1)
            {
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    candidateId = rs.getInt(1);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        close();
        return candidateId;
    }

    public int insertLimit(String limitName, int limitValue, String currency) throws Exception{
        connect();
        int candidateId = 0;
        try {
            preparedStatement = connect
                    .prepareStatement("insert into limits values (default, ?, ?, ?, default)", statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, limitName);
            preparedStatement.setInt(2, limitValue);
            preparedStatement.setString(3, currency);
            int rowAffected = preparedStatement.executeUpdate();
            ResultSet rs = null;
            if(rowAffected == 1)
            {
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    candidateId = rs.getInt(1);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        close();
        return candidateId;
    }

    public int insertSafeBoxes(String boxNumber, String boxSize, int boxFees, String boxFeesCurrency) throws Exception{
        String availability;
        if(isAvailable(boxNumber)){
            availability = "available";
        }else{
            availability = "unavailable";
        }
        System.out.println("availability "+availability);
        connect();
        int candidateId = 0;
        try {
            preparedStatement = connect
                    .prepareStatement("insert into safeboxes values (default, ?, ?, ?, ?, ?, default)", statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, boxNumber);
            preparedStatement.setString(2, boxSize);
            preparedStatement.setInt(3, boxFees);
            preparedStatement.setString(4, boxFeesCurrency);
            preparedStatement.setString(5, availability);
            int rowAffected = preparedStatement.executeUpdate();
            ResultSet rs = null;
            if(rowAffected == 1)
            {
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    candidateId = rs.getInt(1);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        close();
        return candidateId;
    }

    public int insertTransfering(int amount, String currency) throws Exception{
        int candidateId = 0;
        try {
            preparedStatement = connect
                    .prepareStatement("insert into transferingtomain values (default, ?, ?, ?, ?, default)", statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, "initialize");
            preparedStatement.setDouble(2, amount);
            preparedStatement.setString(3, currency);
            preparedStatement.setString(4, "yes");
            int rowAffected = preparedStatement.executeUpdate();
            ResultSet rs = null;
            if(rowAffected == 1)
            {
                // get candidate id
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    candidateId = rs.getInt(1);
            }
            System.out.print("candidateId: "+candidateId);


        } catch (Exception e) {
            throw e;
        } finally {

        }
        return candidateId;
    }

    public int getTransferring() throws Exception {
        ResultSet resultSet = null;
        int counter = 0;
        String query = "SELECT COUNT(id) as counter FROM transferingtomain WHERE userName = 'initialize'";
        Statement statement = connect.createStatement();
        resultSet = statement.executeQuery(query);
        while (resultSet.next()){
            counter = resultSet.getInt("counter");
        }
        return counter;
    }

    public boolean lockDB()throws Exception{
        boolean test = connect.createStatement().execute("LOCK TABLES approx READ, account READ, loan_request READ, withdrawal READ, loan_withdrawal READ;");
        return test;
    }

    public void UnlockDB()throws Exception{
        close();

    }

    public ResultSet getResultSetFee() throws Exception {
        ResultSet resultSet = null;
        connect();
        String query = "SELECT id, feeName, feeCurrency, feeValue FROM fees A ";
        Statement statement = connect.createStatement();
        resultSet = statement.executeQuery(query);

        return resultSet;

    }

    public ResultSet getStepOfApprox() throws Exception {
        ResultSet resultSet = null;
        int step = 1;
        String query = "SELECT step, currency FROM approx";
        Statement statement = connect.createStatement();
        resultSet = statement.executeQuery(query);
        return resultSet;
    }

    public ResultSet getClientLimit() throws Exception {
        ResultSet resultSet =null;
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT limitValue, limitName, currency from limits ");
            resultSet = preparedStatement.executeQuery();

        } catch (Exception e) {
            throw e;
        } finally {
            //close();
        }
        return resultSet;
    }

    public ResultSet getPenalties(int employeeId) throws Exception {
        connect();
        ResultSet resultSet =null;
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT * from penalties WHERE employee_id = ?");
            preparedStatement.setInt(1, employeeId);
            resultSet = preparedStatement.executeQuery();

        } catch (Exception e) {
            throw e;
        } finally {
            //close();
        }
        return resultSet;
    }

    public ResultSet getPenalties(int employeeId, LocalDate start, LocalDate end) throws Exception {
        connect();
        ResultSet resultSet =null;
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT * from penalties WHERE employee_id = ? AND DATE(penalty_date) BETWEEN ? AND ? ");
            preparedStatement.setInt(1, employeeId);
            preparedStatement.setString(2, start.toString());
            preparedStatement.setString(3, end.toString());
            resultSet = preparedStatement.executeQuery();

        } catch (Exception e) {
            throw e;
        } finally {
            //close();
        }
        return resultSet;
    }

    public int getPenaltiesYear(int employeeId) throws Exception {
        connect();
        int minutes = 0;
        ResultSet resultSet =null;
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT SUM(penalty_minuts) AS sumYear from penalties WHERE employee_id = ? AND YEAR(penalty_date) = ? AND status = 'مبرر' ");
            preparedStatement.setInt(1, employeeId);
            preparedStatement.setInt(2, LocalDate.now().getYear());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                minutes = resultSet.getInt("sumYear");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return minutes;
    }


    public int getDaysOffYear(int employeeId) throws Exception {
        connect();
        int minutes = 0;
        ResultSet resultSet =null;
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT COUNT(dayOff_id) AS sumYear from dayoff WHERE employee_id = ? AND YEAR(dayOffDate) = ?");
            preparedStatement.setInt(1, employeeId);
            preparedStatement.setInt(2, LocalDate.now().getYear());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                minutes = resultSet.getInt("sumYear");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            close();
        }
        return minutes;
    }

    public ResultSet getExtras(int employeeId) throws Exception {
        connect();
        ResultSet resultSet =null;
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT * from extratime WHERE employee_id = ?");
            preparedStatement.setInt(1, employeeId);
            resultSet = preparedStatement.executeQuery();

        } catch (Exception e) {
            throw e;
        } finally {
            //close();
        }
        return resultSet;
    }

    public ResultSet getExtras(int employeeId, LocalDate start, LocalDate end) throws Exception {
        connect();
        ResultSet resultSet =null;
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT * from extratime WHERE employee_id = ? AND DATE(penalty_date) BETWEEN ? AND ?");
            preparedStatement.setInt(1, employeeId);
            preparedStatement.setString(2, start.toString());
            preparedStatement.setString(3, end.toString());
            resultSet = preparedStatement.executeQuery();

        } catch (Exception e) {
            throw e;
        } finally {
            //close();
        }
        return resultSet;
    }

    public ResultSet getMovements(int employeeId, LocalDate start, LocalDate end) throws Exception {
        connect();
        ResultSet resultSet =null;
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT * from movements WHERE employee_id = ? AND DATE(movement_date) BETWEEN ? AND ?");
            preparedStatement.setInt(1, employeeId);
            preparedStatement.setString(2, start.toString());
            preparedStatement.setString(3, end.toString());
            resultSet = preparedStatement.executeQuery();

        } catch (Exception e) {
            throw e;
        } finally {
            //close();
        }
        return resultSet;
    }


    public boolean getMovements(int employeeId, LocalDate start) throws Exception {
        connect();
        boolean status = false;
        ResultSet resultSet =null;
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT COUNT(movement_id) AS counter from movements WHERE employee_id = ? AND DATE(movement_date) = ?");
            preparedStatement.setInt(1, employeeId);
            preparedStatement.setString(2, start.toString());
            resultSet = preparedStatement.executeQuery();

        } catch (Exception e) {
            throw e;
        } finally {
            //close();
        }
        while (resultSet.next()){
            int counter = resultSet.getInt("counter");
            if(counter > 0 ){
                status = true;
            }else {
                status = false;
            }
        }
        return status;
    }

    public int addPenalty(LocalDate now, long difference, int userId) {
        int check = 0;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("insert into penalties values (default, ?, ?, ?, default)", statement.RETURN_GENERATED_KEYS);
            // "idFP, fullName, nationalId, summary, center from test.comments");
            // Parameters start with 1
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, (int)difference);
            preparedStatement.setString(3, now.toString());


            int rowAffected = preparedStatement.executeUpdate();

            ResultSet rs = null;

            if(rowAffected == 1)
            {
                // get candidate id
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    check = rs.getInt(1);
            }
            System.out.print("insertLoans: "+check);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return check;
    }

    public int addExtraTime(LocalDate now, long difference, int userId) {
        int check = 0;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("insert into extratime values (default, ?, ?, ?, default)", statement.RETURN_GENERATED_KEYS);
            // "idFP, fullName, nationalId, summary, center from test.comments");
            // Parameters start with 1
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, (int)difference);
            preparedStatement.setString(3, now.toString());


            int rowAffected = preparedStatement.executeUpdate();

            ResultSet rs = null;

            if(rowAffected == 1)
            {
                // get candidate id
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    check = rs.getInt(1);
            }
            System.out.print("insertLoans: "+check);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return check;
    }

    public int addMove(int userId, String s) {
        int check = 0;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("insert into movements values (default, ?, ?, default)", statement.RETURN_GENERATED_KEYS);
            // "idFP, fullName, nationalId, summary, center from test.comments");
            // Parameters start with 1
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, s);
            int rowAffected = preparedStatement.executeUpdate();
            ResultSet rs = null;
            if(rowAffected == 1)
            {
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    check = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return check;
    }

    public int addVacation(LocalDate vacation_day, String reason) {
        int check = 0;
        try {
            connect();
            preparedStatement = connect
                    .prepareStatement("insert into vacations values (default, ?, ?)", statement.RETURN_GENERATED_KEYS);
            // "idFP, fullName, nationalId, summary, center from test.comments");
            // Parameters start with 1
            preparedStatement.setString(1, vacation_day.toString());
            preparedStatement.setString(2, reason);
            int rowAffected = preparedStatement.executeUpdate();
            ResultSet rs = null;
            if(rowAffected == 1)
            {
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    check = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return check;
    }

    public boolean isEvenPass(int employee_id) {

        boolean movement_type = true;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT COUNT(movement_id) as counter FROM movements WHERE employee_id = ? AND DATE(movement_date) = ?");
            preparedStatement.setInt(1,employee_id);
            preparedStatement.setString(2, LocalDate.now().toString());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                if(resultSet.getInt("counter")%2 == 0){
                    movement_type = true;
                }else {
                    movement_type = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return movement_type;
    }

    public String getLastMoveDate(int employee_id) {
        String lastDate = null;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT TIME(movement_date) as time FROM movements WHERE employee_id = ? AND DATE(movement_date) = ? ORDER BY movement_id DESC LIMIT 1");
            preparedStatement.setInt(1,employee_id);
            preparedStatement.setString(2,LocalDate.now().toString());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                lastDate = resultSet.getString("time");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return lastDate;
    }

    public String getLastMoveDate(int employee_id, LocalDate day) {
        String lastDate = null;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT TIME(movement_date) as time FROM movements WHERE employee_id = ? AND DATE(movement_date) = ? ORDER BY movement_id DESC LIMIT 1");
            preparedStatement.setInt(1,employee_id);
            preparedStatement.setString(2,day.toString());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                lastDate = resultSet.getString("time");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return lastDate;
    }

    public String getLastMoveType(int employee_id) {
        String movement_type = "nothing";
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT movement_type FROM movements WHERE employee_id = ? ORDER BY movement_id DESC LIMIT 1");
            preparedStatement.setInt(1,employee_id);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                movement_type = resultSet.getString("movement_type");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return movement_type;
    }

    public LocalDateTime getLastMoveDateTime(int employee_id) {
        LocalDateTime lastDate = LocalDateTime.now().minusDays(10);
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT DATE(movement_date) as myDate, TIME(movement_date) as myTime FROM movements WHERE employee_id = ? AND movement_type = ? ORDER BY movement_id DESC LIMIT 1");
            preparedStatement.setInt(1,employee_id);
            preparedStatement.setString(2,"first enter");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                lastDate = LocalDateTime.of(LocalDate.parse(resultSet.getString("myDate")), LocalTime.parse(resultSet.getString("myTime")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return lastDate;
    }

    public LocalTime getLastMoveDateTime(int employee_id, LocalDate day) {
        LocalTime lastDate = null;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT DATE(movement_date) as myDate, TIME(movement_date) as myTime FROM movements WHERE employee_id = ? AND DATE(movement_date) = ? ORDER BY movement_id DESC LIMIT 1");
            preparedStatement.setInt(1,employee_id);
            preparedStatement.setString(2, day.toString());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                lastDate = LocalTime.parse(resultSet.getString("myTime"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return lastDate;
    }

    public ResultSet getEmployeeNames() throws Exception {
        ResultSet resultSet =null;
        connect();
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT fullName from users_data");
            resultSet = preparedStatement.executeQuery();

        } catch (Exception e) {
            throw e;
        } finally {
            //close();
        }
        return resultSet;
    }

    public ResultSet getBranches() throws Exception {
        ResultSet resultSet =null;
        connect();
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT DISTINCT(branchName) from users_data");
            resultSet = preparedStatement.executeQuery();

        } catch (Exception e) {
            throw e;
        } finally {
            //close();
        }
        return resultSet;
    }

    public ResultSet getEmployees(String branchName) throws Exception {
        ResultSet resultSet =null;
        connect();
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT id, fullName, father, mother, nationalId, branchName from users_data WHERE branchName = ? AND activated = 'yes'");
            preparedStatement.setString(1, branchName);
            resultSet = preparedStatement.executeQuery();

        } catch (Exception e) {
            throw e;
        } finally {
            //close();
        }
        return resultSet;
    }

    public ResultSet getVacations() throws Exception {
        ResultSet resultSet =null;
        connect();
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT * FROM vacations ");
            resultSet = preparedStatement.executeQuery();

        } catch (Exception e) {
            throw e;
        } finally {
            //close();
        }
        return resultSet;
    }

    public int getEmployeeIdByName(String employeeName){

        int employee_id = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT id FROM users_data WHERE fullName = ?");
            preparedStatement.setString(1,employeeName);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                employee_id = resultSet.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return employee_id;
    }

    public int setPenaltyStatus(int employeeId, String status){
        int check = 0;
        try {
            connect();
            String query = "UPDATE penalties SET status = ? WHERE penalty_id = ?";
            PreparedStatement preparedStatement = connect.prepareStatement(query, statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, status);
            preparedStatement.setInt(2, employeeId);
            int rowAffected = preparedStatement.executeUpdate();
            ResultSet rs = null;
            if(rowAffected == 1)
            {
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    check = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return check;
    }


    public int getShiftIdByName(String text) {
        int employee_id = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT shift_id FROM shifts WHERE shiftName = ?");
            preparedStatement.setString(1,text);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                employee_id = resultSet.getInt("shift_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return employee_id;

    }

    public int getShiftIdByEmployeeId(int userId) {
        int employee_id = 0;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT accountType FROM users_data WHERE id = ?");
            preparedStatement.setInt(1,userId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                employee_id = resultSet.getInt("accountType");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return employee_id;

    }

    public int deleteSchedule(int id) throws Exception {
        int check = 0;
        connect();
        String query = "DELETE FROM shifts WHERE shift_id = ?";

        PreparedStatement preparedStatement = connect.prepareStatement(query, statement.RETURN_GENERATED_KEYS);
        preparedStatement.setInt(1, id);
        int rowAffected = preparedStatement.executeUpdate();
        if(rowAffected != 0){
            check = 1;
        }
        connect.close();
        return check;
    }

    public int deleteSubSchedule(int id) throws Exception {
        int check = 0;
        connect();
        String query = "DELETE FROM sub_shifts WHERE shift_id = ?";

        PreparedStatement preparedStatement = connect.prepareStatement(query, statement.RETURN_GENERATED_KEYS);
        preparedStatement.setInt(1, id);
        int rowAffected = preparedStatement.executeUpdate();
        if(rowAffected != 0){
            check = 1;
        }
        connect.close();
        return check;
    }

    public int deleteSub(int id) throws Exception {
        int check = 0;
        connect();
        String query = "DELETE FROM sub_shifts WHERE sub_id = ?";
        PreparedStatement preparedStatement = connect.prepareStatement(query, statement.RETURN_GENERATED_KEYS);
        preparedStatement.setInt(1, id);
        int rowAffected = preparedStatement.executeUpdate();
        if(rowAffected != 0){
            check = 1;
        }
        connect.close();
        return check;
    }

    public int editEmployee(String fullName, String father, String mother, String nationalId, String branchName, String birthPlace, String salary_category, int shift_id, int id ){
        int check = 0;
        try {
            connect();
            String query = "UPDATE users_data SET fullName = ?, father = ?, mother = ?, nationalId = ?, birthPlace = ?" +
                    ", accountType = ?, branchName = ?, salary_category = ? WHERE id = ?";
            PreparedStatement preparedStatement = connect.prepareStatement(query, statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, fullName);
            preparedStatement.setString(2, father);
            preparedStatement.setString(3, mother);
            preparedStatement.setString(4, nationalId);
            preparedStatement.setString(5, birthPlace);
            preparedStatement.setInt(6, shift_id);
            preparedStatement.setString(7, branchName);
            preparedStatement.setString(8, salary_category);
            preparedStatement.setInt(9, id);
            int rowAffected = preparedStatement.executeUpdate();
            ResultSet rs = null;
            if(rowAffected == 1)
            {
                rs = preparedStatement.getGeneratedKeys();
                if(rs.next())
                    check = rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        System.out.println("check "+check);
        return check;
    }

    public boolean isIgnoredDay(int employeeId, LocalDate tempDate) {

        boolean ignored = false;
        try {
            connect();
            PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM dayoff WHERE employee_id = ? AND dayOffDate = ?");
            preparedStatement.setInt(1,employeeId);
            preparedStatement.setString(2, tempDate.toString());
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                if(resultSet.getInt("dayOff_id") != 0){
                    ignored = true;
                }else {
                    ignored = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
        return ignored;

    }
}
