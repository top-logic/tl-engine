<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.*"
%><%@page import="com.top_logic.knowledge.wrap.person.PersonManager"
%><%@page import="com.top_logic.util.TLContext"
%><%@page import="com.top_logic.basic.StringServices"
%><%@page import="java.sql.DriverManager"
%><%@page import="java.sql.Connection"
%><%@page import="java.sql.ResultSet"
%><%@page import="java.sql.ResultSetMetaData"
%><%@page import="com.top_logic.basic.sql.SQLQuery"
%><%@page import="com.top_logic.basic.Logger"
%><%@page import="com.top_logic.dob.DataObject"
%><%@page import="com.top_logic.dob.ex.NoSuchAttributeException"
%><%@page import="com.top_logic.dob.xml.DOXMLWriter"
%><%@page import="com.top_logic.basic.xml.TagUtil"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<%!
	
	private static String DRIVER_NAME = "";
	/** The service to connect to SAP. */
	private static Connection CONNECT;
	
	/** SAP access data */
	private static String driver   = "com.mysql.cj.jdbc.MysqlConnectionPoolDataSource";
	private static String url      = "jdbc:mysql://localhost:3306/...";
	private static String table    = "";
	private static String user     = "";
	private static String password = "";
	
	public void testGetDataObject(String aTable, JspWriter aWriter) throws Exception {
		aWriter.write("<b>Try to connect to DB...</b><br />");
		
		Connection        theConn   = this.getConnection();
		SQLQuery          theQuery  = new SQLQuery(theConn, "SELECT * FROM " + aTable + ";");
		ResultSet         theResult = theQuery.getResultSet();
		ResultSetMetaData theMeta   = theResult.getMetaData();
		int               theCount  = theMeta.getColumnCount();
		int               thePos    = 1;
		
		while (theResult.next()) {
			aWriter.write("<b>Result #" + (thePos++) + "</b>:<br />");
			
			this.writeSQLResult(theResult, theMeta, theCount, aWriter);
			
			aWriter.write("<br />");
		}
		
		aWriter.write("<b>Finished...</b><br />");
	}
	
	public void writeSQLResult(ResultSet aResult, ResultSetMetaData aMeta, int aCount, JspWriter aWriter) throws Exception {
		int    theCount;
		Object theValue;
		
		for (int thePos = 0; thePos < aCount; thePos++) {
			theCount = thePos + 1;
			theValue = aResult.getObject(theCount);
			
			aWriter.write(aMeta.getColumnName(theCount) + ": ");
			aWriter.write((theValue == null) ? "[null]" : theValue.toString());
			aWriter.write("<br />");
		}
	}
	
	public Connection getConnection() throws Exception {
		if ((CONNECT == null) || !DRIVER_NAME.equals(driver)) {
			if (!StringServices.isEmpty(driver) && !DRIVER_NAME.equals(driver)) {
				Class.forName(driver);
			}
			
			CONNECT = DriverManager.getConnection(url, user, password);
		}
		
		return (CONNECT);
	}
	
	/**
	* Print the content of the given data object as debug info to system out.
	*
	* @param    anObject    The data object to be printed, may be <code>null</code>.
	*/
	public void printDataObject(DataObject anObject, JspWriter aWriter) throws NoSuchAttributeException {
		TagUtil.writeText(aWriter, new DOXMLWriter().convertDO2XML(anObject));
	}
	
	%><%
	boolean         isNew     = !"makeItSo".equals(request.getParameter("done"));
	
	LayoutComponent theComp   = MainLayout.getComponent(pageContext);
	
	if (!isNew) {
		driver    = request.getParameter("driver");
		url       = request.getParameter("url");
		table     = request.getParameter("table");
		user      = request.getParameter("user");
		password  = request.getParameter("password");
	}
	%>
	<layout:body>
		<basic:access>
			<h1>
				JDBC Connection Test
			</h1>
			<p>
				Please click on "Make It So" to test the JDBC connection with the given parameters.
			</p>
			<form name="select"
				action="<%=theComp.getComponentURL(pageContext).getURL() %>"
				method="post"
			>
				<input name="done"
					type="hidden"
					value="makeItSo"
				/>
				<table
					align="center"
					width="100%"
				>
					<tr>
						<td colspan="2">
							<hr/>
						</td>
					</tr>
					<tr>
						<td class="label">
							JDBC Driver:
						</td>
						<td class="content">
							<input name="driver"
								size="50"
								type="text"
								value="<%=driver %>"
							/>
						</td>
					</tr>

					<tr>
						<td class="label">
							JDBC URL:
						</td>
						<td class="content">
							<input name="url"
								size="50"
								type="text"
								value="<%=url %>"
							/>
						</td>
					</tr>

					<tr>
						<td class="label">
							Table:
						</td>
						<td class="content">
							<input name="table"
								size="50"
								type="text"
								value="<%=table %>"
							/>
						</td>
					</tr>

					<tr>
						<td class="label">
							User:
						</td>
						<td class="content">
							<input name="user"
								size="50"
								type="text"
								value="<%= user %>"
							/>
						</td>
					</tr>

					<tr>
						<td class="label">
							Password:
						</td>
						<td class="content">
							<input name="password"
								size="50"
								type="password"
								value="<%= password %>"
							/>
						</td>
					</tr>

					<tr>
						<td colspan="2">
							<hr/>
						</td>
					</tr>

					<tr>
						<td
							align="center"
							colspan="2"
						>
							<input name="submit"
								type="submit"
								value="Make It So"
							/>
						</td>
					</tr>
				</table>
			</form>
			<br/>
			<%
			if (!isNew) {
				try {
					this.testGetDataObject(table, out);
				}
				catch (Exception ex) {
					Logger.error("JDBC connection test failed: ", ex, this);
					out.write("JDBC connection test failed: " + ex);
				}
			}
			%>
		</basic:access>
	</layout:body>
</layout:html>