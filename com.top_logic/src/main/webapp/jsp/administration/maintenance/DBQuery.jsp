<%@page import="com.top_logic.basic.col.TypedAnnotatable"
%><%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.basic.col.TypedAnnotatable.Property"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"%>
<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>

<%-- Maintenance template version: 1.2 (1.6) --%>
<%!

// JSP page configuration:

// XXX_BUTTON = null: no xxx button
// RUN_BUTTON = null && SIMULATE_BUTTON = null: run automatic
private static final String RUN_BUTTON = "Execute update";
private static final String SIMULATE_BUTTON = "Simulate update";
private static final String QUERY_BUTTON = "Execute query";
private static final String CLEAR_BUTTON = "Clear history";
private static final String REFRESH_BUTTON = null;
private static final String RESTART_LINK = "New query";

private static final Property<String> STATEMENT_PROPERTY = TypedAnnotatable.property(String.class, "adminJSP_DBQuery_Statement");
private static final Property<List<Tuple>> HISTORY_PROPERTY = TypedAnnotatable.propertyList("adminJSP_DBQuery_History");

private static final int HISTORY_LENGTH = 256;

{
	LOG_PRINTS = true;
	LOG_TIME = true;
	USE_WAITING_ANI = false;
	SIM_WAITING_MESSAGE = "Simulating...";
	WAITING_MESSAGE = "Running...";
	TITLE = "Database Query";
	DESCRIPTION =
	"This page runs a database query and shows the result of the query." +
	"<br/>Warning: Not all statements can be simulated / rollbacked, e.g. TRUNCATE TABLE.";
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
	%><%@page import="com.top_logic.basic.sql.SQLH"
	%><%@page import="com.top_logic.knowledge.service.SimpleDBExecutor"
	%><%@page import="com.top_logic.util.db.DBUtil"
	%><%@page import="com.top_logic.basic.xml.TagUtil"
	%><%@page import="com.top_logic.util.TLContext"
	%><%@page import="com.top_logic.basic.col.TupleFactory.Tuple"
	%><%@page import="com.top_logic.basic.col.TupleFactory"
	%><%@page import="com.top_logic.basic.CollectionUtil"
	%><%@page import="java.util.ArrayList"
	%><%@page import="java.util.List"
	%><%@page import="com.top_logic.basic.StringServices"
	%><%@page import="com.top_logic.layout.Control"
	%><%@page import="com.top_logic.util.FormFieldHelper"
	%><%@page import="com.top_logic.layout.form.FormField"
	%><%@page import="com.top_logic.layout.form.ValueListener"
	%><%@page import="com.top_logic.layout.form.model.FormFactory"
	%><%@page import="com.top_logic.layout.form.model.StringField"
	%><%@page import="com.top_logic.layout.form.model.FormContext"
	%><%@page import="com.top_logic.layout.form.template.TextInputFormFieldControlProvider"
	%><%@page import="com.top_logic.basic.util.NumberUtil"
	%><%@page import="com.top_logic.basic.xml.TagWriter"
	%><%@page import="com.top_logic.layout.URLBuilder"%>
	<%!
	@Override
	protected void doWork(JspWriter out, boolean simulate, HttpServletRequest request) throws Exception {
		try {
			boolean query = request.getParameter("QUERY") != null;
			JspWriter writer = getWriter();
			String statement = TLContext.getContext().get(STATEMENT_PROPERTY);
			if (StringServices.isEmpty(statement)) {
				print("No statement specified.");
			}
			else {
				if (HISTORY_LENGTH > 0) {
					List<Tuple> history = TLContext.getContext().mkList(HISTORY_PROPERTY);
					if (history.size() == HISTORY_LENGTH) {
						history.remove(0);
					}
					Tuple tuple = CollectionUtil.getLast(history);
					int counter = tuple == null ? 1 : ((Integer)tuple.get(0)).intValue() + 1;
					history.add(TupleFactory.newTuple(Integer.valueOf(counter), query ? "QUERY" : simulate ? "SIMULATE" : "UPDATE", statement));
				}
				
				long startTime = System.currentTimeMillis();
				if (query) {
					writer.write("<h4>Executing query:</h4>");
					print(statement);
					print(); print();
					String[][] result = DBUtil.executeQueryAsTable(statement);
					printResult(result);
				}
				else {
					writer.write(simulate ? "<h4>Simulating update:</h4>" : "<h4>Executing update:</h4>");
					print(statement);
					print(); print();
					SimpleDBExecutor db = new SimpleDBExecutor();
					db.beginTransaction();
					try {
						int result = db.executeUpdate(statement);
						
						if (simulate) {
							db.rollbackTransaction();
							printColor("blue", "Simulating OK.");
						}
						else {
							db.commitTransaction();
							printInfo("Commit OK.");
						}
						print("Update affected " + result + " rows.");
					}
					finally {
						db.closeTransaction();
					}
				}
				
				print(); print();
				long endTime = System.currentTimeMillis();
				print("Done. Query lasts " + DebugHelper.getTime(endTime - startTime));
			}
		}
		catch (Exception e) {
			printError("Error: Failed to execute statement.");
			printError(e.toString(), e);
			rollback();
		}
	}
	
	private void printResult(String[][] result) throws Exception {
		JspWriter writer = getWriter();
		writer.write("<table class=\"tl-standard-table\" border=\"1\" style=\"white-space: nowrap\">\n");
		
		// write header
		if (result.length > 0) {
			String[] header = result[0];
			writer.write("<tr>\n");
			for (int i = 0, length = header.length; i < length; i++) {
				String value = header[i];
				writer.write("<th>");
				writer.write(TagUtil.encodeXML(StringServices.nonNull(SQLH.quote(value))));
				writer.write("</th>\n");
			}
			writer.write("</tr>\n");
		}
		
		// write content
		for (int i = 1, iLength = result.length; i < iLength; i++) {
			String[] row = result[i];
			writer.write("<tr>\n");
			for (int j = 0, jLength = row.length; j < jLength; j++) {
				String value = row[j];
				writer.write("<td>");
				writer.write(TagUtil.encodeXML(StringServices.nonNull(SQLH.quote(value))));
				writer.write("</td>\n");
			}
			writer.write("</tr>\n");
		}
		
		writer.write("</table>\n");
	}
	%>
	<layout:body>
		<basic:access>
			<%
			LayoutComponent component = MainLayout.getComponent(pageContext);
			DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(pageContext);
			
			writeWaitingSlider(pageContext, "SUBMIT", "SIMULATE", "QUERY");
			
			// clear history
			if (request.getParameter("CLEAR") != null) {
				TLContext.getContext().reset(HISTORY_PROPERTY);
			}
			
			// run work
			if ((RUN_BUTTON == null && SIMULATE_BUTTON == null && QUERY_BUTTON == null) ||
				(USE_WAITING_ANI && (request.getParameter("DO_SUBMIT") != null || request.getParameter("DO_SIMULATE") != null || request.getParameter("DO_QUERY") != null)) ||
				(!USE_WAITING_ANI && (request.getParameter("SUBMIT") != null || request.getParameter("SIMULATE") != null || request.getParameter("QUERY") != null))) {
				
				if (RESTART_LINK != null) {
					%>
					<p>
					    <button class="tlButton cButton cmdButton" onclick="self.location.href = '<%=component.getComponentURL(displayContext).getURL()%>';">
					        <h4 class="tlButtonLabel"><%= RESTART_LINK %></h4>
					    </button>
					</p>
					<%
				}
				
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
			}
			else {
				%>
				<form method="POST">
					<table>
						<tr>
							<td>
								<p>
									<b>
										Statement:
									</b>
								</p>
								<div>
									<%
									// add statement field
									FormContext context = new FormContext(component);
									StringField field = FormFactory.newStringField(STATEMENT_PROPERTY.getName());
									field.setValue(TLContext.getContext().get(STATEMENT_PROPERTY));
									field.addValueListener(new ValueListener() {
											@Override
											public void valueChanged(FormField formField, Object oldValue, Object newValue) {
												TLContext.getContext().set(STATEMENT_PROPERTY, FormFieldHelper.getStringValue(formField));
											}
									});
									context.addMember(field);
									Control control = new TextInputFormFieldControlProvider(5, 128, true).createControl(field, null);
									TagWriter tagWriter = MainLayout.getTagWriter(pageContext);
									control.write(displayContext, tagWriter);
									tagWriter.flushBuffer();
									%>
								</div>

								<div class="cmdButtons">
								    <%
								    if (QUERY_BUTTON != null) {
								        %>
								        <button class="tlButton cButton cmdButton" name="QUERY" type="submit">
								            <h4 class="tlButtonLabel"><%= QUERY_BUTTON %></h4>
								        </button>
								        <%
								    }
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
								</div>
								<%
								if (HISTORY_LENGTH > 0) {
									List<Tuple> history = TLContext.getContext().get(HISTORY_PROPERTY);
									StringBuilder sb = new StringBuilder();
									sb.append("<table class=\"tl-standard-table\" border=\"0\" >\n");
									if (history != null && history.size() > 0) {
										%>
										<br/>
										<h4>
											History:
										</h4>
										<%
										for (int i = history.size() - 1; i >= 0; i--) {
											TupleFactory.Tuple tuple = history.get(i);
											sb.append("<tr>");
											sb.append("<td>").append(tuple.get(0)).append("</td>");
											sb.append("<td>").append(tuple.get(1)).append("</td>");
											sb.append("<td>").append(tuple.get(2)).append("</td>");
											sb.append("</tr>\n");
										}
										sb.append("</table>\n");
										%>
										<%=sb.toString()%>
										
										<%
										if (CLEAR_BUTTON != null) {
											%>
											<div class="cmdButtons">
												<button class="tlButton cButton cmdButton" name="CLEAR" type="submit">
													<h4 class="tlButtonLabel"><%= CLEAR_BUTTON %></h4>
												</button>
											</div>
											<%
										}
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