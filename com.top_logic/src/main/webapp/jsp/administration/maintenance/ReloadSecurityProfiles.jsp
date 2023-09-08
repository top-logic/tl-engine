<%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"%>
<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>

<%-- Maintenance template version: 1.1 (1.6) --%>
<%!

// JSP page configuration:

// XXX_BUTTON = null: no xxx button
// RUN_BUTTON = null && SIMULATE_BUTTON = null: run automatic
private static final String RUN_BUTTON = "Reload";
private static final String SIMULATE_BUTTON = null;
private static final String REFRESH_BUTTON = null;
private static final String RESTART_LINK = "Reload page";
{
	LOG_PRINTS = true;
	LOG_TIME = true;
	WAITING_MESSAGE = "Reloading...";
	SIM_WAITING_MESSAGE = "Simulating...";
	USE_WAITING_ANI = true;
	TITLE = "Reload security profiles";
	DESCRIPTION = "This maintenance page reloads the security profiles for all compound security components.";
}
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
	%><%@page import="com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile.RolesProfileHandler"
	%><%@page import="com.top_logic.layout.URLBuilder"
	%><%@page import="com.top_logic.basic.util.NumberUtil"
	%><%@page import="com.top_logic.basic.xml.TagUtil"%>
	
	<%!
	
	// This methods does the actual work:
	private void runWork(LayoutComponent component, JspWriter out, boolean doSimulate, HttpServletRequest aRequest) throws Exception {
		initWriter(out);
		long startTime = System.currentTimeMillis();
		doWork(doSimulate, component);
		print();
		if (!LOG_TIME) print("Finished.");
		else print("Finished. Run needed " + DebugHelper.getTime(System.currentTimeMillis() - startTime));
	}
	
	
	
	private void doWork(boolean simulate, LayoutComponent component) throws Exception {
		print("Running maintenance work...");
		print(); print();
		try {
			MainLayout aMainLayout = component.getMainLayout();
			RolesProfileHandler.reloadSecurityProfiles(aMainLayout);
			commit(simulate);
		}
		catch (Exception e) {
			printError("Error: Failed to reload security profiles.");
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
				<table style="margin: 5px">
					<tr>
						<td>
							<code class="normal">
								<%
								try {
									runWork(component, out, doSimulate, request);
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