<%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"%>
<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>

<%-- Maintenance template version: 1.6 --%>
<%!
// JSP page configuration:

// XXX_BUTTON = null: no xxx button
// RUN_BUTTON = null && SIMULATE_BUTTON = null: run automatic
private static final String RUN_BUTTON = "Test";
private static final String SIMULATE_BUTTON = null;
private static final String REFRESH_BUTTON = null;
private static final String RESTART_LINK = "Reload page";
{
	LOG_PRINTS = true;
	LOG_TIME = true;
	WAITING_MESSAGE = "Running...";
	SIM_WAITING_MESSAGE = "Simulating...";
	USE_WAITING_ANI = false;
	TITLE = "Test Person Data Access Service";
	DESCRIPTION = "Allows a Test of the configured persondata access devices.";
}
private static final boolean VERBOSE = true;
private static final String REQ_PARAM_DEVICE_ID="deviceID";
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
	%><%@page import="com.top_logic.basic.util.NumberUtil"
	%><%@page import="com.top_logic.basic.xml.TagUtil"
	%><%@page import="com.top_logic.layout.URLBuilder"
	%><%@page import="com.top_logic.dob.DataObject"
	%><%@page import="java.util.List"
	%><%@page import="com.top_logic.base.security.device.interfaces.PersonDataAccessDevice"
	%><%@page import="com.top_logic.base.security.device.TLSecurityDeviceManager"
	%><%@page import="javax.naming.directory.Attribute"
	%><%@page import="com.top_logic.base.security.device.ldap.LDAPDataObject"
	%><%@page import="java.util.Arrays"%>
	
	<%!
	// This methods does the actual work:
	@Override
	protected void doWork(JspWriter out, boolean simulate, HttpServletRequest request) throws Exception {
		print("Checking data access device...");
		print(); print();
		int checked = 0;
		
		String deviceID = request.getParameter(REQ_PARAM_DEVICE_ID);
		print("Selected data access device: " + deviceID);
		
		try {
			PersonDataAccessDevice userDevice = TLSecurityDeviceManager.getInstance().getDataAccessDevice(deviceID);
			if (userDevice != null) {
				List<DataObject> entries = userDevice.getAllUserData();
				Iterator<DataObject> it = entries.iterator();
				
				print("Entries found: " + entries.size());
				print(); print();
				
				out.write("<table border=\"0\">\n");
				out.write("<tr><td><h4>Attribute</h4></td><td><h4>Value</h4></td></tr>\n");
				while (it.hasNext()) {
					checked++;
					out.write("<tr><td colspan=\"2\"><h5>Entry " + checked + ":</h5></td></tr>\n");
					try {
						DataObject entry = it.next();
						String[] attrNames = entry.getAttributeNames();
						Arrays.sort(attrNames);
						for (int i = 0; i < attrNames.length; i++) {
							String attrName = attrNames[i];
							out.write("<tr>");
							out.write("<td>" + quote(attrName) + "</td>");
							try {
								String value = getValue(entry, attrName);
								out.write("<td>" + quote(value) + "</td>");
							}
							catch (Exception e) {
								out.write("<tr><td style=\"color:red\">Error reading value: " + e.toString() + "</td></tr>");
								if (LOG_PRINTS) Logger.error(e.toString(), e, this);
							}
							out.write("</tr>\n");
						}
						if (it.hasNext()) {
							out.write("<tr><td colspan=\"2\">&nbsp;</td></tr><tr><td colspan=\"2\">&nbsp;</td></tr>\n");
						}
					}
					catch (Exception e){
						out.write("<tr><td colspan=\"2\" style=\"color:red\">Unable to read attributes for entry. " + e.toString() + "</td></tr>");
						if (LOG_PRINTS) Logger.error(e.toString(), e, this);
					}
				}
				out.write("</table>\n");
			}
			else{
				printError("No user device (ressource) found for selected person data access device." + deviceID);
			}
			
			print(); print();
			print("Done. Found " + checked + " user entries in selected data access device.");
		}
		catch (Exception e) {
			printError("Error while checking data access device.");
			printError(e.toString(), e);
		}
	}
	
	private String getValue(DataObject entry, String attrName) throws Exception {
		StringBuffer sb = new StringBuffer();
		if (entry instanceof LDAPDataObject) {
			Attribute attr = ((LDAPDataObject)entry).getJNDIAttributes().get(attrName);
			for (int i = 0, length = attr.size(); i < length; i++) {
				if (i > 0) {
					sb.append(" ");
				}
				sb.append(attr.get(i));
			}
		}
		else {
			sb.append(entry.getAttributeValue(attrName));
		}
		return sb.toString();
	}
	
	%>
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
				<table style="margin: 5px">
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
										&#xA0;
										<input name="<%=doSimulate ? "SIMULATE" : "SUBMIT"%>"
											type="submit"
											value="<%=REFRESH_BUTTON%>"
										/>
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
						<a
							href="javascript:self.location.href = '<%=component.getComponentURL(displayContext).getURL()%>';"
							style="color:darkblue"
						>
							&#xA0;<%=RESTART_LINK%>
						</a>
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
									Data access device:
									<select name="<%=REQ_PARAM_DEVICE_ID%>">
										<%
										Iterator<String> devIDs = TLSecurityDeviceManager.getInstance().getConfiguredDataAccessDeviceIDs().iterator();
										while(devIDs.hasNext()){
											String anID = devIDs.next();
											%>
											<option>
												<%=anID%>
											</option>
											<%
										}
										%>
									</select>
								</p>

								<p>
									<%
									if (RUN_BUTTON != null) {
										%>
										&#xA0;
										<input name="SUBMIT"
											type="submit"
											value="<%=RUN_BUTTON%>"
										/>
										<%
									}
									if (SIMULATE_BUTTON != null) {
										%>
										&#xA0;
										<input name="SIMULATE"
											type="submit"
											value="<%=SIMULATE_BUTTON%>"
										/>
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