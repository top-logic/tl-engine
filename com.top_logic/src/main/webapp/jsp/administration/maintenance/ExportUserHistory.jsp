<%@page import="com.top_logic.basic.io.binary.BinaryDataFactory"
%><%@page import="com.top_logic.layout.form.PlainKeyResources"
%><%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"%>
<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>

<%-- Maintenance template version: 1.1 (1.6) --%>
<%!
// JSP page configuration:

// XXX_BUTTON = null: no xxx button
// RUN_BUTTON = null && SIMULATE_BUTTON = null: run automatic
private static final String RUN_BUTTON = "Run";
private static final String SIMULATE_BUTTON = null;
private static final String REFRESH_BUTTON = null;
private static final String RESTART_LINK = "Reload page";
{
	LOG_PRINTS = true;
	LOG_TIME = true;
	WAITING_MESSAGE = "Running...";
	SIM_WAITING_MESSAGE = "Simulating...";
	USE_WAITING_ANI = true;
	TITLE = "Create user history";
	DESCRIPTION = "Shows latest logins of current accounts.";
}

private static char separator = ';';
private static char delim='\n';

%>

<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>
<layout:html>
	<layout:head>
		<title>
			<%=TITLE%>
		</title>
		<meta
			content="text/html; charset=iso-8859-1"
			http-equiv="Content-Type"
		/>
		<basic:cssLink/>
		<basic:script>
			services.ajax.ignoreTLAttributes = true;
		</basic:script>
	</layout:head>
	<%-- Insert import statements here. --%>
	<%@page import="java.util.Map"
	%><%@page import="java.util.Iterator"
	%><%@page import="com.top_logic.basic.Logger"
	%><%@page import="com.top_logic.basic.ArrayUtil"
	%><%@page import="com.top_logic.basic.DebugHelper"
	%><%@page import="com.top_logic.mig.html.layout.MainLayout"
	%><%@page import="com.top_logic.mig.html.layout.LayoutComponent"
	%><%@page import="com.top_logic.layout.DisplayContext"
	%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
	%><%@page import="com.top_logic.knowledge.service.KnowledgeBaseFactory"
	%><%@page import="com.top_logic.knowledge.service.KnowledgeBase"
	%><%@page import="com.top_logic.gui.ThemeFactory"
	%><%@page import="com.top_logic.basic.xml.TagUtil"
	%><%@page import="java.sql.SQLException"
	%><%@page import="java.sql.ResultSet"
	%><%@page import="com.top_logic.util.db.ResultExtractor"
	%><%@page import="com.top_logic.basic.col.MapUtil"
	%><%@page import="com.top_logic.util.db.DBUtil"
	%><%@page import="com.top_logic.knowledge.wrap.person.PersonManager"
	%><%@page import="java.util.HashMap"
	%><%@page import="com.top_logic.layout.provider.MetaLabelProvider"
	%><%@page import="com.top_logic.knowledge.wrap.person.Person"
	%><%@page import="com.top_logic.basic.DateUtil"
	%><%@page import="com.top_logic.knowledge.monitor.UserSession"
	%><%@page import="java.util.Date"
	%><%@page import="com.top_logic.layout.form.model.FormFactory"
	%><%@page import="java.util.List"
	%><%@page import="java.util.Collection"
	%><%@page import="com.top_logic.util.Resources"
	%><%@page import="java.util.ArrayList"
	%><%@page import="java.io.File"
	%><%@page import="java.io.FileOutputStream"
	%><%@page import="java.io.StringWriter"
	%><%@page import="java.io.FileWriter"
	%><%@page import="com.top_logic.basic.io.FileUtilities"
	%><%@page import="com.top_logic.layout.form.model.DataField"
	%><%@page import="com.top_logic.knowledge.gui.layout.upload.DefaultDataItem"
	%><%@page import="com.top_logic.layout.form.model.FormContext"
	%><%@page import="com.top_logic.basic.io.binary.FileBasedBinaryData"
	%><%@page import="com.top_logic.layout.form.template.DefaultFormFieldControlProvider"
	%><%@page import="com.top_logic.basic.xml.TagWriter"
	%><%@page import="java.util.StringTokenizer"
	%><%@page import="com.top_logic.basic.StringServices"
	%><%@page import="com.top_logic.knowledge.wrap.WrapperFactory"
	%><%@page import="com.top_logic.layout.URLBuilder"
	%><%@page import="com.top_logic.basic.util.NumberUtil"%>
	
	<%!private String createLine(Person person, Date lastLoginDate) {
		return encodeValue(person.getName()) + separator + encodeValue(person.getUser().getFirstName()) + separator
		+ encodeValue(person.getUser().getName()) + separator
		+ encodeValue(person.getUser().getEMail()) + separator
		+ encodeValue(person.getAuthenticationDeviceID()) + separator
		+ MetaLabelProvider.INSTANCE.getLabel(lastLoginDate) + delim;
	}
	
	private String encodeValue(String value) {
		if (value == null) return StringServices.EMPTY_STRING;
		return value.replace(separator, '_').replace(delim, ' ');
	}
	
	@Override
	protected void doWork(JspWriter out, boolean simulate, HttpServletRequest request) throws Exception {
		print("Running maintenance work...");
		print(); print();
		int checked = 0;
		
		try {
			List<String> theResult = new ArrayList<>();
			theResult.add("Login-ID" + separator + "Vorname" + separator + "Nachname" + separator + "E-Mail" + separator
			 + "Authentifizierungsressource" + separator + "Datum letzter Login" + delim); //add header line
			
			Map<String, Date> latestLogins = DBUtil.executeQuery("SELECT NAME, MAX(LOGIN) FROM USER_SESSION GROUP BY NAME",
				new ResultExtractor<Map<String, Date>>() {
					@Override
					public Map<String, Date> extractResult(ResultSet result) throws SQLException {
						Map<String, Date> resultMap = new HashMap<>();
						while (result.next()) {
							resultMap.put(result.getString(1), result.getDate(2));
						}
						return resultMap;
					}
			});
			
			Iterator<Person>accounts = Person.all().iterator();
			while (accounts.hasNext()) {
				Person person = accounts.next();
				checked++;
				Date lastLogin = latestLogins.get(person.getName());
				if (lastLogin != null) {
					theResult.add(createLine(person, lastLogin));
				}
			}
			
			File tempFile = File.createTempFile("latestSessions", "csv");
			FileWriter fw = new FileWriter(tempFile);
			Iterator<String> lines = theResult.iterator();
			while(lines.hasNext()){
				fw.append(lines.next());
			}
			fw.flush();
			fw.close();
			
			final FormContext fc = new FormContext("form", PlainKeyResources.INSTANCE);
			DataField theDataField  = FormFactory.newDataField("file");
			theDataField.setValue(new DefaultDataItem("Benutzerhistorie.csv", BinaryDataFactory.createBinaryData(tempFile), "application/csv"));
			theDataField.setImmutable(true);
			fc.addMember(theDataField);
			
			TagWriter tout = new TagWriter(out);
			new DefaultFormFieldControlProvider().createControl(fc,null).write(DefaultDisplayContext.getDisplayContext(), tout);
			tout.flushBuffer();
			
			print(); print();
			printInfo("Checked " + checked + " accounts.");
			printInfo("Finished. Download the generated File.");
			print(); print();
			printInfo("Note:");
			printInfo("Excel should open the file correctly as table with separate columns.");
			printInfo("If it doesn't, you have to manually import the csv file:");
			printInfo("Choose \"Data -> From file\". Select semicolon as separator.");
		}
		catch (Exception e) {
			printError("Error while processing maintenance work.");
			printError(e.toString(), e);
			rollback();
		}
	}%>
	<layout:body>
		<basic:access>
			<%
			LayoutComponent component = MainLayout.getComponent(pageContext);
			DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(pageContext);
			
			writeWaitingSlider(pageContext, "SUBMIT", "SIMULATE");
			
			// run work
			if ((RUN_BUTTON == null && SIMULATE_BUTTON == null) ||
				(USE_WAITING_ANI && (request.getParameter("DO_SUBMIT") != null || request.getParameter("DO_SIMULATE") != null)) ||
				(!USE_WAITING_ANI && (request.getParameter("SUBMIT") != null || request.getParameter("SIMULATE") != null))) {
				
				boolean doSimulate = request.getParameter(USE_WAITING_ANI ? "DO_SIMULATE" : "SIMULATE") != null;
				if (doSimulate) {
					out.write("<br/><b>Simulating...</b><br/><br/><br/>\n");
				}
				%>
				<table>
					<tr>
						<td>
							<code class="normal">
								<%
								try {
									runWork(out, doSimulate, request);
								}
								catch (Throwable t) {
									printError("Error: An error occured while running the JSP page:");
									printError(t.toString());
									rollback();
								}
								%>
							</code>
						</td>
					</tr>
				</table>
				<%
				if (REFRESH_BUTTON != null) {
					%>
					<br/>
					<form method="POST">
						<table>
							<tr>
								<td>
									<p>
										<button class="tlButton cButton cmdButton"
									            name="<%=doSimulate ? "SIMULATE" : "SUBMIT"%>"
									            type="submit">
									        <span class="tlButtonLabel"><%= REFRESH_BUTTON %></span>
									    </button>
									</p>
								</td>
							</tr>
						</table>
					</form>
					<%
				}
				if (RESTART_LINK != null) {
					%>
					<p>
					    <button class="tlButton cButton cmdButton" onclick="self.location.href = '<%=component.getComponentURL(displayContext).getURL()%>';">
					        <h4 class="tlButtonLabel"><%= RESTART_LINK %></h4>
					    </button>
					</p>
					<%
				}
			}
			else {
				%>
				<form method="POST">
					<table>
						<tr>
							<td>
								<%-- Add here custom inputs or fields. --%>
								<p>
									<%
									if (RUN_BUTTON != null) {
										%>
										<button class="tlButton cButton cmdButton" name="SUBMIT" type="submit">
								            <h4 class="tlButtonLabel"><%= RUN_BUTTON %></h4>
								        </button>
										<%
									}
									if (SIMULATE_BUTTON != null) {
										%>
										<button class="tlButton cButton cmdButton" name="SIMULATE" type="submit">
								            <h4 class="tlButtonLabel"><%= SIMULATE_BUTTON %></h4>
								        </button>
										<%
									}
									%>
								</p>
							</td>
						</tr>
					</table>
				</form>
				<%
			}
			%>
			<br/>
		</basic:access>
	</layout:body>
</layout:html>