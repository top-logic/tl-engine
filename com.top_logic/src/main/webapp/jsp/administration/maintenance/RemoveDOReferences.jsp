<%@page import="com.top_logic.dob.attr.MOAttributeImpl"
%><%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="com.top_logic.knowledge.service.KnowledgeBaseFactory"
%><%@page import="com.top_logic.knowledge.service.KnowledgeBase"
%><%@page import="com.top_logic.basic.xml.TagUtil"%>

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
	TITLE = "RemoveDOReferences";
	DESCRIPTION =
	"This page removes old references to DO storage of all objects. This is necessary " +
	"as result of the refactoring described by ticket #266, implemented on 06.01.2009.";
}
%>

<%!
// This methods does the actual work:
@Override
protected void doWork(JspWriter out, boolean simulate, HttpServletRequest request) throws Exception {
	final String PR_ATTRIBUTE = "physicalResource";
	print("Removing old references to DO storage...");
	int checked = 0, removed = 0;
	
	try {
		KnowledgeBase theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
		SimpleDBExecutor db = new SimpleDBExecutor();
		
		MORepository theRepository = theKB.getMORepository();
		Iterator it = theRepository.getMetaObjectNames().iterator();
		while (it.hasNext()) {
			String typeName = (String)it.next();
			MOStructure theMO = (MOStructure)theRepository.getMetaObject(typeName);
			if (!theMO.hasAttribute(PR_ATTRIBUTE)) continue;
			if (theMO instanceof MOClass && ((MOClass)theMO).isAbstract()) continue;
			
			int processed = 0, fixed = 0;
			print("Processing type '" + quote(typeName) + "'...");
			Collection theObjects = theKB.getAllKnowledgeObjects(typeName);
			
			if (theMO instanceof DBTableMetaObject) {
				DBTableMetaObject theDBMO = (DBTableMetaObject)theMO;
				String attrName = ((MOAttributeImpl)theDBMO.getAttributeOrNull(PR_ATTRIBUTE)).getDBName();
				String statement = "UPDATE " + theDBMO.getDBName() + " SET " + attrName + " = NULL WHERE " + attrName + " LIKE 'do://%'";
				db.beginTransaction((CommitHandler)theKB);
				fixed = db.executeUpdate(statement);
				processed = theObjects.size();
			}
			else {
				Iterator itObj = theObjects.iterator();
				while (itObj.hasNext()) {
					KnowledgeObject theObject = (KnowledgeObject)itObj.next();
					Object theValue = theObject.getAttributeValue(PR_ATTRIBUTE);
					if (theValue instanceof String && ((String)theValue).startsWith("do://")) {
						theObject.setAttributeValue(PR_ATTRIBUTE, null);
						fixed++;
					}
					processed++;
				}
			}
			checked++;
			removed += fixed;
			print("Processed " + processed + " objects and removed " + fixed + " references.");
			commit(simulate);
		}
		print("Done. Processed " + checked + " object types and removed " + removed + " references.");
		print("");
	}
	catch (Exception e) {
		printError("Error: Failed to removing references to DO storage." + e.toString(), e);
		rollback();
	}
	print("");
}
%><%@page import="java.util.Iterator"
%><%@page import="java.util.Collection"
%><%@page import="com.top_logic.knowledge.service.SimpleDBExecutor"
%><%@page import="com.top_logic.knowledge.objects.KnowledgeObject"
%><%@page import="com.top_logic.dob.meta.MORepository"
%><%@page import="com.top_logic.dob.MetaObject"
%><%@page import="com.top_logic.dob.meta.MOClass"
%><%@page import="com.top_logic.dob.sql.DBTableMetaObject"
%><%@page import="com.top_logic.knowledge.service.CommitHandler"
%><%@page import="com.top_logic.dob.meta.MOStructure"
%><layout:html>
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

	<layout:body>
		<basic:access>
			<h1>
				<%=TITLE%>
			</h1>
			<p>
				<%=DESCRIPTION%>
			</p>
			<%
			if ((RUN_BUTTON == null && SIMULATE_BUTTON == null) || request.getParameter("SUBMIT") != null || request.getParameter("SIMULATE") != null) {
				boolean doSimulate = request.getParameter("SIMULATE") != null;
				if (doSimulate) {
					out.write("<br/><b>Simulating...</b><br/><br/><br/>\n");
				}
				%>
				<table style="margin: 5px">
					<tr>
						<td>
							<code class="normal">
								<%
								runWork(out, doSimulate, request);
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
							href="javascript:document.location = document.location;"
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