/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.sql;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.time.CalendarUtil;

/**
 * Work on a database and move all dates in time.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class MoveInTime {

    /** Format to match an MySQL Date */
    public static final SimpleDateFormat DATE_FORMAT    = CalendarUtil.newSimpleDateFormat("yyyy-MM-dd"); 
    
    /** Format to match an MySQL Date/Time */
    public static final SimpleDateFormat DT_FORMAT      = CalendarUtil.newSimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 

    /** The name of the database to be used. */
    private String dbName;

    /** The time difference between old and new reference date. */
    private long timeDiff = 0;

    public MoveInTime() {
        this("posdemo");
    }

    /** 
     * Create a new instance for moving in time.
     * 
     * @param    aDatabase    The name of the database to be used.
     * @throws   IllegalArgumentException    If given name is <code>null</code> or empty.
     */
    public MoveInTime(String aDatabase) throws IllegalArgumentException {
        if (StringServices.isEmpty(aDatabase)) {
            throw new IllegalArgumentException("Name of database is null or empty!");
        }

        this.dbName = aDatabase;
    }

    @Override
	public String toString() {
        return this.getClass().getName() + " ["
                + "database: '" + this.getDatabaseName()
                + "', timeshift: " + this.getTimeSlip()
                + "]";
    }

    public void makeItSo() {
        Connection theConn = null;

        try {
            System.out.println("Moving content from '" + this.getDatabaseName() + "' in time!");
            System.out.println("\nOld reference date: " + this.getReferenceDate());
            System.out.println("New reference date: " + this.getNewReferenceDate());

            theConn = this.getConnection();

            DatabaseMetaData theBaseMetaData = theConn.getMetaData();
            ResultSet        theTables       = theBaseMetaData.getTables(this.getDatabaseName(), null, null, null);
            long             theSlip         = this.getTimeSlip();
            String           theTable;
            long             theTimer;
            List<String>     theInserts = new ArrayList<>();

            while (theTables.next()) {
                theTable = theTables.getString(3);
                theTimer = System.currentTimeMillis();

                System.out.println("++++ Working on table '" + theTable + "'!");
                this.moveTable(theInserts, theConn, theBaseMetaData, theTable, theSlip);
                System.out.println("---- Working on table '" + theTable + "' lasts " + (System.currentTimeMillis() - theTimer) + " msec!");
            }

            if (theInserts.size() > 0 && this.executeInserts(theConn, theInserts)) {
                System.out.println("Finished generation of update script.");
            }

        }
        catch (Exception ex) {
            Logger.error("Error in execution!", ex, this);
        }
        finally {
            if (theConn != null) {
                try {
                    theConn.close();
                }
                catch (SQLException ex) {
                    Logger.error("Unable to close connection!", ex, this);
                }
            }
        }
    }

    protected Collection moveTable( List<String> aColl, Connection theConn, DatabaseMetaData aBaseMetaData, String theTable, long aSlip) throws SQLException  {
        if ("do_storage".equals(theTable)) {
            // Inspect the DO_STORAGE
            String            theQuery     = "SELECT TYPE, IDENTIFIER, ATTR, LVAL FROM " + theTable + " WHERE VAL_TYPE=68;";
            PreparedStatement theStatement = theConn.prepareStatement(theQuery);

            if (theStatement.execute()) {

				Collection theColl;
				try (ResultSet resultSet = theStatement.getResultSet()) {
					theColl = this.prepareDOInserts(resultSet, aSlip);
				}

                if (theColl.size() > 0) {
                    aColl.addAll(theColl);
                }
            }
            else {
                System.out.println("Unable to execute '" + theQuery + "'!");
            }
        }
        else if ("token".equals(theTable)) {
              // ignore these
        }
        else if (theTable.startsWith("fin_store")) {
            String            theQuery     = "SELECT * FROM " + theTable;
            PreparedStatement theStatement = theConn.prepareStatement(theQuery);

            if (theStatement.execute()) {
                Collection theColl = this.prepareFinStoreInserts(theStatement.getResultSet(), aSlip, theTable.endsWith("_freeze"));
    
                if (theColl.size() > 0) {
                    aColl.addAll(theColl);
                }
            }
            else {
                System.out.println("Unable to execute '" + theQuery + "'!");
            }
        }
        else if (theTable.startsWith("fin__year")) {
            String            theQuery     = "SELECT * FROM " + theTable;
            PreparedStatement theStatement = theConn.prepareStatement(theQuery);

            if (theStatement.execute()) {
                Collection theColl = this.prepareFinYearInserts(theStatement.getResultSet(), aSlip, theTable.endsWith("_freeze"));
    
                if (theColl.size() > 0) {
                    aColl.addAll(theColl);
                }
            }
            else {
                System.out.println("Unable to execute '" + theQuery + "'!");
            }
        }
        else if ("fin_freeze_info".equals(theTable)) {
            String            theQuery     = "SELECT * FROM " + theTable;
            PreparedStatement theStatement = theConn.prepareStatement(theQuery);

            if (theStatement.execute()) {
                Collection theColl = this.prepareFinInfoInserts(theStatement.getResultSet(), aSlip);
    
                if (theColl.size() > 0) {
                    aColl.addAll(theColl);
                }
            }
            else {
                System.out.println("Unable to execute '" + theQuery + "'!");
            }
        }
        else if (theTable.startsWith("res_")) {
            String            theQuery     = "SELECT * FROM " + theTable;
            PreparedStatement theStatement = theConn.prepareStatement(theQuery);

            if (theStatement.execute()) {
                Collection theColl = this.prepareResStoreInserts(theStatement.getResultSet(), aSlip, theTable);
    
                if (theColl.size() > 0) {
                    aColl.addAll(theColl);
                }
            }
            else {
                System.out.println("Unable to execute '" + theQuery + "'!");
            }
        }
        else {
            String            theQuery     = "SELECT * FROM " + theTable;
            PreparedStatement theStatement = theConn.prepareStatement(theQuery);

            if (theStatement.execute()) {
                Collection theColl = this.prepareInserts(theStatement.getResultSet(), aSlip);
    
                if (theColl.size() > 0) {
                    aColl.addAll(theColl);
                }
            }
            else {
                System.out.println("Unable to execute '" + theQuery + "'!");
            }
        }

        return (aColl);
    }

    /** 
     * Move DO Storage in time.
     * 
     * @param    aResult    The result set with all matching data from storage.
     * @param    aSlip      The time slip to be added.
     * @return   The collection of update statements.
     * @throws   SQLException    If requesting the values fails for a reason.
     */
    private List<String> prepareDOInserts(ResultSet aResult, long aSlip) throws SQLException {
        List<String> theColl = new ArrayList<>();
        long         theTime;
        long         theNewTime;
        String       theResult;

        while (aResult.next()) {
            theTime    = aResult.getLong(4);
            theNewTime = theTime + aSlip;

            theResult = "UPDATE do_storage SET LVAL=" + theNewTime 
                            + " WHERE TYPE='" + aResult.getString(1)
                            + "' AND IDENTIFIER='" + aResult.getString(2)
                            + "' AND ATTR='" + aResult.getString(3)
                            + "';";

            theColl.add(theResult);
        }
        System.out.println("#statements: " + theColl.size());

        return theColl;
    }

    private Collection prepareResStoreInserts(ResultSet aResult, long aSlip, String aTable) throws SQLException {
        Collection theColl = new ArrayList();
        Date       theTime;
        long       theNewTime;
        String     theResult;
        String     theNewDate;

        while (aResult.next()) {
            theTime    = DateUtil.adjustToNoon(aResult.getDate(3));
            theNewTime = theTime.getTime() + aSlip;
            theNewDate = DATE_FORMAT.format(new Date(theNewTime));

            theResult = "UPDATE " + aTable + " SET DAY='" + theNewDate 
                            + "' WHERE SPID='" + aResult.getString(1)
                            + "' AND PERSID='" + aResult.getString(2)
                            + "' AND DAY='" + DATE_FORMAT.format(theTime)
                            + "';";

            theColl.add(theResult);
        }

        System.out.println("#statements: " + theColl.size());

        return theColl;
    }

    private Collection prepareFinInfoInserts(ResultSet aResult, long aSlip) throws SQLException {
        Collection theColl = new ArrayList();
        Date       theTime;
        long       theNewTime;
        String     theResult;
        String     theNewDate;

        while (aResult.next()) {
            theTime    = aResult.getDate(3);
            theNewTime = theTime.getTime() + aSlip;
            theNewDate = DATE_FORMAT.format(new Date(theNewTime));

            theResult = "UPDATE fin_freeze_info SET WHENCE='" + theNewDate 
                            + "' WHERE FREEZEID='" + aResult.getString(1)
                            + "' AND PRJID='" + aResult.getString(2)
                            + "';";

            theColl.add(theResult);
        }

        System.out.println("#statements: " + theColl.size());

        return theColl;
    }

    private Collection prepareFinYearInserts(ResultSet aResult, long aSlip, boolean isFreeze) throws SQLException {
        Collection theColl     = new ArrayList();
        String     theTable    = isFreeze ? "fin_year_freeze" : "fin_year";
        int        theYearDiff = DateUtil.getYearOfDate(new Date(aSlip)) - 1970;
        int        theTime;
        int        theNewTime;
        String     theResult;

        if (theYearDiff != 0) {
            while (aResult.next()) {
                theTime    = aResult.getInt(isFreeze ? 3 : 2);
                theNewTime = theTime + theYearDiff;

                if (isFreeze) {
                    theResult = "UPDATE " + theTable + " SET YEAR=" + theNewTime 
                                + " WHERE FREEZEID='" + aResult.getString(1)
                                + "' AND PRJID='" + aResult.getString(2)
                                + "';";
                }
                else {
                    theResult = "UPDATE " + theTable + " SET YEAR=" + theNewTime 
                                + " WHERE PRJID='" + aResult.getString(1)
                                + "';";
                }
    
                theColl.add(theResult);
            }
            
            System.out.println("#statements: " + theColl.size());
        }

        return theColl;
    }

    private Collection prepareFinStoreInserts(ResultSet aResult, long aSlip, boolean isFreeze) throws SQLException {
        Collection theColl      = new ArrayList();
        String     theTable     = isFreeze ? "fin_store_freeze" : "fin_store";
        Date       theDate      = new Date(aSlip);
        int        theYearDiff  = DateUtil.getYearOfDate(theDate) - 1970;
        int        theMonthDiff = DateUtil.getMonthOfDate(theDate);
        int        theYear;
        int        theMonth;
        String     theResult;

        if ((theYearDiff != 0) || (theMonthDiff != 0)) {
            while (aResult.next()) {
                theYear  = aResult.getInt(6) + theYearDiff;
                theMonth = aResult.getInt(7) + theMonthDiff;

                if (theMonth > 12) {
                    theMonth -= 12;
                    theYear++;
                }

                if (isFreeze) {
                    theResult = "UPDATE " + theTable + " SET YEAR=" + theYear 
                                + ", MONTH=" + theMonth 
                                + " WHERE FREEZEID='" + aResult.getString(1)
                                + "' AND PRJID='" + aResult.getString(2)
                                + "' AND SPID='" + aResult.getString(3)
                                + "' AND KAID='" + aResult.getString(4)
                                + "' AND TYPE=" + aResult.getInt(5)
                                + " AND YEAR=" + aResult.getInt(6)
                                + " AND MONTH=" + aResult.getInt(7)
                                + ";";
                }
                else {
                    theResult = "UPDATE " + theTable + " SET YEAR=" + theYear 
                                    + ", MONTH=" + theMonth 
                                    + " WHERE PRJID='" + aResult.getString(1)
                                    + "' AND SPID='" + aResult.getString(2)
                                    + "' AND KAID='" + aResult.getString(3)
                                    + "' AND DEL=" + aResult.getInt(4)
                                    + " AND TYPE=" + aResult.getInt(5)
                                    + " AND YEAR=" + aResult.getInt(6)
                                    + " AND MONTH=" + aResult.getInt(7)
                                    + ";";
                }

                theColl.add(theResult);
            }
            
            System.out.println("#statements: " + theColl.size());
        }

        return theColl;
    }

    private static final String[] BASE_FIELDS = {"CREATED", "MODIFIED", "REMOVED"};

    /** 
     * Prepare inserts for all other tables.
     * 
     * @param    aResult    The result set with all matching data from the affected table.
     * @param    aSlip      The time slip to be added.
     * @return   The collection of update statements.
     * @throws   SQLException    If requesting the values fails for a reason.
     */
    protected Collection prepareInserts(ResultSet aResult, long aSlip) throws SQLException {
        Collection        theColl     = new ArrayList();
        ResultSetMetaData theMetaData = aResult.getMetaData();
        int               theSize     = theMetaData.getColumnCount();
        String            theType;
        String            theName;
        Collection        theNameCol  = new ArrayList();
        Collection        theTypesCol = new ArrayList();
        Collection        theNumCol   = new ArrayList();
        String            theTable    = theMetaData.getTableName(1);

        for (int thePos = 1; thePos < (theSize + 1); thePos++) {
            theName = theMetaData.getColumnName(thePos);
            theType = theMetaData.getColumnClassName(thePos);

            if (theType.endsWith("Timestamp") || theType.endsWith("Long") || theType.endsWith("BigInteger")) {
                theNameCol.add(theName);
                theTypesCol.add(theType);
				theNumCol.add(Integer.valueOf(thePos));
            }
        }

        if (theNameCol.size() > 0) {
            String[]  theNames = (String[]) theNameCol.toArray(new String[theNameCol.size()]);
            String[]  theTypes = (String[]) theTypesCol.toArray(new String[theTypesCol.size()]);
            Integer[] theCount = (Integer[]) theNumCol.toArray(new Integer[theNumCol.size()]);
            String    theSQL;
            long      theLong;
            Timestamp theDate;
            String    theSpace;
            boolean   filled;

            while (aResult.next()) {
                theSpace = " ";
                theSQL   = "UPDATE " + theTable + " SET";
                filled   = false;

                for (int thePos = 0; thePos < theNames.length; thePos++) {
                    if (theTypes[thePos].endsWith("Long") || theTypes[thePos].endsWith("BigInteger")) {
                        if (Arrays.binarySearch(BASE_FIELDS, theNames[thePos]) > -1) {
                            theLong  = aResult.getLong(theCount[thePos].intValue());

                            if (theLong > 0) {
                                filled   = true;
                                theSQL   = theSQL + theSpace + theNames[thePos] + '=' + (theLong + aSlip);
                                theSpace = ", ";
                            }
                        }
                    }
                    else if (theTypes[thePos].endsWith("Timestamp")) {
                        theDate  = aResult.getTimestamp(theCount[thePos].intValue());

                        if (theDate != null) {
                            filled   = true;
                            theSQL   = theSQL + theSpace + theNames[thePos] + '=' + this.getNewReferenceDate(theDate, aSlip);
                            theSpace = ", ";
                        }
                    }
                }

                theSQL += " WHERE IDENTIFIER='" + aResult.getString(1) + "';";

                if (filled) {
                    theColl.add(theSQL);
                }
            }
        }

        System.out.println("#statements: " + theColl.size());

        return theColl;
    }

    private String getNewReferenceDate(Date aDate, long aSlip) {
        Date theDate = new Date(aDate.getTime() + aSlip);

        return ("\'" + DT_FORMAT.format(theDate) + '\'');
    }

    protected boolean executeInserts(Connection theConn, List<String> theInserts) throws IOException {
        String     theName   = this.getDatabaseName();
        File       theFile   = new File("Update" + theName + ".sql");
        FileWriter theWriter = new FileWriter(theFile);
        int        theCount  = 0;

        theWriter.write("USE " + theName + ";\n\n");

        for (String theSQL: theInserts) {

            theWriter.write(theSQL);
            theWriter.write('\n');
            theCount++;

            if (theCount > 200) {
                theWriter.write("\nCOMMIT;\n\n");
                theCount = 0;
            }
        }

        theWriter.write("\nCOMMIT;\n");

        theWriter.close();

        return (true);
    }

    protected void printResult(ResultSet aResult) throws SQLException {
        ResultSetMetaData theMetaData = aResult.getMetaData();
        int               theSize     = theMetaData.getColumnCount();

        for (int thePos = 1; thePos < theSize; thePos++) {
            System.out.println(theMetaData.getColumnName(thePos) + ": " + aResult.getObject(thePos));
        }
    }

    /** 
     * Return the time difference between new and old reference date.
     * 
     * @return    The slip in milliseconds.
     */
    protected long getTimeSlip() {
        if (this.timeDiff == 0) {
            try {
                this.timeDiff = this.getNewReferenceDate().getTime() - this.getReferenceDate().getTime();
            }
            catch (Exception ex) {
                Logger.error("Unable to calculate timeshift!", ex, this);
            }
        }

        return this.timeDiff;
    }

    /**
     * Return the current reference date on the database.
     * 
     * @return    The requested reference date, never <code>null</code>.
     */
    protected Date getReferenceDate() throws Exception {
        return (this.getDate("20050909 12:00:00"));
    }

    /**
     * Return the new reference date on the database.
     * 
     * @return    The new reference date, never <code>null</code>.
     */
    protected Date getNewReferenceDate() throws Exception {
        // Aufgerufen am 20060410!
        // TODO Differenz zum heutigen Datum berechnen und ausgehend vom heutigen Tag diese 
        // Differenz eintragen.
        return (this.getDate("20071015 12:00:00"));
    }

    protected Date getDate(String aDate) throws ParseException {
        DateFormat theFormat = CalendarUtil.newSimpleDateFormat("yyyyMMdd HH:mm:ss");

        return (theFormat.parse(aDate));
    }

    /** 
     * Return the name of the database to be moved in time.
     * 
     * @return    The name of the database to move all dates in time.
     */
    protected String getDatabaseName() {
        return (this.dbName);
    }

    /** 
     * Return the connection to the database.
     * 
     * @return    The connection to the database.
     * @throws    Exception    If getting the connection fails.
     */
    protected Connection getConnection() throws Exception {
        String theURL = "jdbc:mysql://localhost/" + this.getDatabaseName() + "?useUnicode=true";

        try {
            Class.forName ("org.gjt.mm.mysql.Driver");

            return DriverManager.getConnection(theURL, "sa", "bup");
        }
        catch (Exception ex) {
            System.err.println("getConnection() for '" + theURL + "' failed");

            throw ex;
        } 
    }

    /** 
     * Start this class.
     */
    public static void main(String[] args) {
        new MoveInTime().makeItSo();
    }
}
