<%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.layout.buttonbar.Icons"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"%>
<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>

<%-- Maintenance template version: 1.2 (1.6) --%>
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
	TITLE = "Rebuild Security";
	DESCRIPTION = "Starts a security rebuild in a new thread.";
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
	%><%@page import="com.top_logic.layout.URLBuilder"
	%><%@page import="java.util.Date"
	%><%@page import="com.top_logic.mig.html.HTMLFormatter"
	%><%@page import="com.top_logic.basic.thread.ThreadContext"
	%><%@page import="com.top_logic.knowledge.security.SecurityStorage"
	%><%@page import="com.top_logic.element.boundsec.manager.StorageAccessManager"
	%><%@page import="com.top_logic.basic.thread.ThreadContextManager"
	%><%@page import="com.top_logic.basic.thread.InContext"
	%><%@page import="com.top_logic.basic.util.NumberUtil"
	%><%@page import="com.top_logic.basic.xml.TagUtil"%>
	
	<%!
	@Override
	protected void doWork(JspWriter out, boolean simulate, HttpServletRequest request) throws Exception {
		print("Starting security rebuild...");
		print(); print();
		
		try {
			new Thread(){
				@Override
				public void run(){
					ThreadContextManager.inSystemInteraction(SecurityStorage.class, new InContext() {
							@Override
							public void inContext() {
								ThreadContext.pushSuperUser();
								StorageAccessManager.getInstance().reload();
								ThreadContext.popSuperUser();
							}
					});
				}
			}.start();
			print("Securtiy rebuild started. This can take a few minutes...");
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
			
			// waiting slider
			if (USE_WAITING_ANI && (request.getParameter("SUBMIT") != null || request.getParameter("SIMULATE") != null)) {
				boolean doSimulate = request.getParameter("SIMULATE") != null;
				%>
				<div id="progressDiv"
					class="fullProgressVisible"
				>
					<table
						height="100%"
						width="100%"
					>
						<tr>
							<td align="center">
								<b>
									<%=doSimulate ? SIM_WAITING_MESSAGE : WAITING_MESSAGE%>
								</b>
								<br/>
								<br/>
								<% if (USE_WAITING_ANI) { %>
									<basic:image
										border="0"
										cssClass="fullProgress"
										icon="<%= ThemeFactory.getTheme().getValue(Icons.SLIDER_IMG) %>"
									/>
									<% } else { %>
									&#xA0;
								<% } %>
							</td>
						</tr>
					</table>
				</div>
				<basic:script>
					self.location.href = "<%=addParameters(component.getComponentURL(displayContext), request).getURL()%>";
				</basic:script>
				<%
			}
			else {
				%>
				<h1>
					<%=TITLE%>
				</h1>
				<p>
					<%=DESCRIPTION%>
				</p>
				<%
			}
			
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
					<basic:script>
						window.setTimeout(function(){self.location.href = "<%=component.getComponentURL(displayContext).getURL()%>"}, 5000);
					</basic:script>
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
								
								<%
								if (!((StorageAccessManager)StorageAccessManager.getInstance()).getSecurityStorage().isRebuilding()) {
									if (RUN_BUTTON != null) {
										%>
										<p>
											&#xA0;
											<input name="SUBMIT"
												type="submit"
												value="<%=RUN_BUTTON%>"
											/>
										</p>
										<%
									}
									if (SIMULATE_BUTTON != null) {
										%>
										<p>
											&#xA0;
											<input name="SIMULATE"
												type="submit"
												value="<%=SIMULATE_BUTTON%>"
											/>
										</p>
										<%
									}
								}
								else {
									String time = HTMLFormatter.getInstance().formatDateTime(new Date());
									%>
									<p>
										Security rebuild in progress. Current time is <%=time %>.
									</p>
									<%
									if (RESTART_LINK != null) {
										%>
										<basic:script>
											window.setTimeout(function(){self.location.href = "<%=component.getComponentURL(displayContext).getURL()%>"}, 5000);
										</basic:script>
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
								%>
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
<%!
private URLBuilder addParameters(URLBuilder url, HttpServletRequest aRequest) {
	boolean runParam = false;
	Map theParameters = aRequest.getParameterMap();
	Iterator it = theParameters.entrySet().iterator();
	while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		String key = (String)entry.getKey();
		String value = (String)ArrayUtil.getFirst((String[])entry.getValue());
		if ("SUBMIT".equals(key)) {
			url.appendParameter("DO_SUBMIT", value);
			runParam = true;
		}
		else if ("SIMULATE".equals(key)) {
			url.appendParameter("DO_SIMULATE", value);
			runParam = true;
		}
		else {
			url.appendParameter((String)entry.getKey(), value);
		}
	}
	if (!runParam) {
		url.appendParameter("DO_SUBMIT", "SUBMIT");
	}
	return url;
}
%>