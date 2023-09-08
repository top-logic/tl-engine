<%@taglib uri="basic" prefix="basic"
%><%@page extends="com.top_logic.util.TopLogicJspBase"
import="java.sql.*,
com.top_logic.basic.StringServices"
%><%!
protected String getClean (String aString) {
	return ((aString == null) ? "" : aString);
}
%>
<basic:html>
	<head>
		<title>
			SQL
		</title>
		<meta
			content="text/html; charset=iso-8859-1"
			http-equiv="Content-Type"
		/>
	</head>
	<body
		bgcolor="#FF0000"
		text="#000000"
	>
		<basic:access>
			<h2>
				SQL Interface for direct database access
			</h2>
			<p>
				<i>
					<font size="-1">
						Go back to
						<a href="index.jsp">
							Overview page
						</a>
					</font>
				</i>
			</p>
			<%
			String driver = this.getClean (request.getParameter ("driver"));
			String url    = this.getClean (request.getParameter ("url"));
			String user   = this.getClean (request.getParameter ("user"));
			String passwd = this.getClean (request.getParameter ("passwd"));
			String sql    = this.getClean (request.getParameter ("sql"));
			%>
			<p>
				Please specify the parameters below and send a query to the database.
			</p>
			<form method="POST">
				<table summary="Enter JDBC Parameters here">
					<tr>
						<td align="right">
							Driver:
						</td>
						<td>
							<input name="driver"
								size="50"
								value="<%= driver %>"
							/>
						</td>
					</tr>
					<tr>
						<td align="right">
							URL:
						</td>
						<td>
							<input name="url"
								size="50"
								value="<%= url %>"
							/>
						</td>
					</tr>
					<tr>
						<td align="right">
							User:
						</td>
						<td>
							<input name="user"
								size="50"
								value="<%= user %>"
							/>
						</td>
					</tr>
					<tr>
						<td align="right">
							Password:
						</td>
						<td>
							<input name="passwd"
								size="50"
								type="password"
								value="<%= passwd %>"
							/>
						</td>
					</tr>
				</table>
				<hr/>
				<p>
					Please specify the query for the database:
				</p>
				<p>
					<textarea name="sql"
						cols="80"
						rows="10"
					>
						<%= sql %>
					</textarea>
				</p>
				<table summary="Send SQL Query or Update">
					<tr>
						<td width="50%">
							<input name="query"
								type="submit"
								value="Send query"
							/>
						</td>
						<td width="50%">
							<input name="update"
								type="submit"
								value="Send update"
							/>
						</td>
					</tr>
				</table>
			</form>
			<%
			if (!StringServices.isEmpty (driver) && !StringServices.isEmpty (url)) {
				Class.forName (driver);
				Connection con = DriverManager.getConnection (url, user, passwd);
				Statement  stm = con.createStatement ();
				
				if (request.getParameter ("query") != null) {
					ResultSet         res   = stm.executeQuery (sql);
					ResultSetMetaData meta  = res.getMetaData ();
					int               count = meta.getColumnCount ();
					%>
					<p>
						Executing query:
						<b>
							<%=sql %>
						</b>
					</p>
					<table summary="Result of Query">
						<tr>
							<%
							for (int thePos = 1; thePos <= count; thePos++) {
								%>
								<th>
									<%=meta.getColumnName (thePos) %>
								</th>
								<%
							}
							%>
						</tr>
						<%
						boolean odd = true;
						
						while (res.next ()) {
							odd = !odd;
							%>
							<tr bgcolor="<%=odd ? "#cfcfcf" : "white" %>">
								<%
								for (int thePos = 1; thePos <= count; thePos++) {
									%>
									<td nowrap="nowrap">
										<%=res.getObject (thePos) %>
									</td>
									<%
								}
								%>
							</tr>
							<%
						}
						%>
					</table>
					<%
					res.close ();
				}
				else if (request.getParameter("update") != null) {
					%>
					<p>
						Result of update: <%=stm.executeUpdate (sql) %>
					</p>
					<%
				}
				
				stm.close ();
				con.close ();
			}
			%>
		</basic:access>
	</body>
</basic:html>