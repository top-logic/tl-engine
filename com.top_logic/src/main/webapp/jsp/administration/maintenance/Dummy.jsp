<%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"%>
<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>

<%-- Maintenance template version: 1.7 --%>
<%!
// JSP page configuration:

// XXX_BUTTON = null: no xxx button
// RUN_BUTTON = null && SIMULATE_BUTTON = null: run automatic
private static final String RUN_BUTTON = "Run";
private static final String SIMULATE_BUTTON = "Simulate";
private static final String REFRESH_BUTTON = null;
private static final String RESTART_LINK = "Reload page";
{
	LOG_PRINTS = true;
	LOG_TIME = true;
	WAITING_MESSAGE = "Running...";
	SIM_WAITING_MESSAGE = "Simulating...";
	USE_WAITING_ANI = true;
	TITLE = "Dummy";
	DESCRIPTION = "This is a template for maintenance JSP pages.";
}
%>

<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>
<layout:html>
	<layout:head>
		<title>
			<%=TITLE%>
		</title>
		<meta
			content="text/html; charset=UTF-8"
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
	%><%@page import="com.top_logic.layout.URLBuilder"%>
	
	<%!
	// This methods does the actual work:
	@Override
	protected void doWork(JspWriter out, boolean simulate, HttpServletRequest request) throws Exception {
		print("Running maintenance work...");
		print(); print();
		int checked = 0, fixed = 0;
		
		try {
			// Here the very important work is done:
			
			printInfo("Hier könnte Ihr Code stehen!");
			printInfo("Interessiert?", new Exception("Stacktrace"));
			print();
			printWarning("Sie müssen sich aber schnell dafür entscheiden.");
			printColor("skyblue", "Das ganze gibts auch in Farbe.");
			printColor("palevioletred", "Und bunt!", new Exception("Stacktrace"));
			print();
			printUnquoted("Es kann auch <code>gestyled</code> und <b>gequotet</b> " + quote("(<i>dies ist nicht kursiv</i>)") + " werden.");
			printColor("blue", "Tolle Zeichen ( < > & ) können direkt gequotet werden.");
			printLog("Auch Loggerausgaben sind möglich...");
			print();
			try {
				print("Fortschritt: " + percent(0, 3) + "%");
				Thread.sleep(1000);
				print("Fortschritt: " + percent(1, 3) + "%");
				Thread.sleep(1000);
				print("Fortschritt: " + percent(2, 3) + "%");
				Thread.sleep(1000);
				print("Fortschritt: " + percent(3, 3) + "%");
			}
			catch (InterruptedException e) {
				printWarning("Nicht mal in Ruhe schlafen kann man hier...", e);
				printLog("... Sogar mit Stacktraces.", e);
			}
			
			
			print(); print();
			print("Done. Checked " + checked + " and fixed " + fixed + " objects. Committing...");
			commit(simulate);
		}
		catch (Exception e) {
			printError("Error while processing maintenance work.");
			printError(e.toString(), e);
			rollback();
		}
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