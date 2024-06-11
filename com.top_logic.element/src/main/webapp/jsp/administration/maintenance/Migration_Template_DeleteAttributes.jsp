<%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.layout.buttonbar.Icons"
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
	TITLE = "Migration: Delete old elements and attributes";
	DESCRIPTION = "This page deletes attributes and other stuff not required anymore.";
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
	%><%@page import="com.top_logic.element.core.util.MaintenanceUtil"
	%><%@page import="java.util.Collection"
	%><%@page import="com.top_logic.tool.boundsec.wrap.BoundedRole"
	%><%@page import="com.top_logic.tool.boundsec.gui.EditRoleComponent.DeleteRoleCommand"
	%><%@page import="com.top_logic.layout.admin.component.EditGroupComponent.DeleteGroupCommandHandler"
	%><%@page import="com.top_logic.tool.boundsec.CommandHandlerFactory"
	%><%@page import="com.top_logic.tool.boundsec.wrap.Group"
	%><%@page import="com.top_logic.knowledge.objects.KnowledgeObject"
	%><%@page import="com.top_logic.basic.io.EmptyInputStream"
	%><%@page import="com.top_logic.layout.URLBuilder"
	%><%@page import="com.top_logic.basic.xml.TagUtil"
	%><%@page import="com.top_logic.basic.util.NumberUtil"%>
	
	<%!
	@Override
	protected void doWork(JspWriter out, boolean simulate, HttpServletRequest request) throws Exception {
		print("Deleting attributes not required anymore...");
		print();
		boolean done = false;
		
		try {
			
			done |= deleteMetaAttribute("My.MetaElement", "MyAttribute");
			commit(simulate);
			
			done |= deleteMetaElement("My.MetaElement");
			commit(simulate);
			
			done |= deleteList("MyList");
			commit(simulate);
			
			done |= deleteListElement("MyList", "MyListElement");
			commit(simulate);
			
			done |= deleteGroup("MyGroup");
			commit(simulate);
			
			done |= deleteRole("MyRole");
			commit(simulate);
			
			
			print(); print();
			print(done ? "Deleting complete." : "Check done. Nothing was to do.");
		}
		catch (Exception e) {
			printError("Error: Failed to delete attributes.");
			printError(e.toString(), e);
			rollback();
		}
	}
	
	private boolean deleteMetaAttribute(String me, String ma) throws Exception {
		print(); print("Deleting MetaAttribute '" + ma + "' from MetaElement '" + me + "'...");
		try {
			return processResult(MaintenanceUtil.deleteMetaAttributeComplete(me, ma));
		}
		catch (Exception e) {
			printError("Failed: " + e.toString(), e); print();
			rollback();
			return false;
		}
	}
	
	private boolean deleteMetaElement(String me) throws Exception {
		print(); print(); print("Deleting MetaElement '" + me + "'...");
		try {
			return processResult(MaintenanceUtil.deleteMetaElementComplete(me));
		}
		catch (Exception e) {
			printError("Failed: " + e.toString(), e); print();
			rollback();
			return false;
		}
	}
	
	private boolean deleteList(String listName) throws Exception {
		print(); print(); print("Deleting list '" + listName + "'...");
		try {
			return processResult(MaintenanceUtil.deleteList(listName));
		}
		catch (Exception e) {
			printError("Failed: " + e.toString(), e); print();
			rollback();
			return false;
		}
	}
	
	private boolean deleteListElement(String listName, String elementName) throws Exception {
		print(); print(); print("Deleting list element '" + elementName + "' from list '" + listName + "'...");
		try {
			return processResult(MaintenanceUtil.deleteListElement(listName, elementName));
		}
		catch (Exception e) {
			printError("Failed: " + e.toString(), e); print();
			rollback();
			return false;
		}
	}
	
	private boolean deleteGroup(String groupName) throws Exception {
		print(); print(); print("Deleting group '" + groupName + "'...");
		try {
			Group group = Group.getGroupByName(groupName);
			if (group == null) return processResult(false);
			group.tDelete();
			return processResult(true);
		}
		catch (Exception e) {
			printError("Failed: " + e.toString(), e); print();
			rollback();
			return false;
		}
	}
	
	private boolean deleteRole(String roleName) throws Exception {
		print(); print(); print("Deleting role '" + roleName + "'...");
		try {
			BoundedRole role = BoundedRole.getRoleByName(roleName);
			if (role == null) return processResult(false);
			role.tDelete();
			return processResult(true);
		}
		catch (Exception e) {
			printError("Failed: " + e.toString(), e); print();
			rollback();
			return false;
		}
	}
	
	private boolean processResult(boolean result) throws Exception {
		print(result ? "Done." : "Already done; nothing was to do."); print();
		return result;
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
				<table">
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