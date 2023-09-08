<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="java.util.Iterator"
%><%@page import="com.top_logic.knowledge.wrap.person.PersonManager"
%><%@page import="com.top_logic.knowledge.wrap.person.Person"
%><%@page import="com.top_logic.tool.boundsec.wrap.Group"
%><%@page import="java.util.Collection"
%><%@page import="com.top_logic.knowledge.service.KnowledgeBase"
%><%@page import="com.top_logic.knowledge.service.KnowledgeBaseFactory"
%><%@page import="com.top_logic.util.sched.Scheduler"
%><%@page import="com.top_logic.mail.proxy.AbstractMailServerDaemon"
%><layout:html>
	<layout:head/>
	<%
	boolean isNew     = !"makeItSo".equals(request.getParameter("done"));
	
	%>
	<layout:body>
		<basic:access>
			<h1>
				Removal of duplicated representative groups
			</h1>
			<%
			if (isNew) {
				%>
				Click "start" to delete duplicated representative groups.
				<form method="post">
					<input name="done"
						type="hidden"
						value="makeItSo"
					/>
					<table
						summary="Button to reload selected elements"
						width="100%"
					>
						<tr>
							<td>
								<input name="RELOAD"
									type="submit"
									value="Start"
								/>
							</td>
						</tr>
					</table>
				</form>
				<% } else {
				out.print("Please wait...<br /><hr>");
				
				AbstractMailServerDaemon theTask = (AbstractMailServerDaemon) Scheduler.getSchedulerInstance().getTaskByName("MailServerDaemon");
				
				if (theTask != null) {
					theTask.setActivated(true);
					out.print("<br/>Task activated<br/>");
				}
				else {
					out.print("<br/>Task not found<br/>");
				}
			}
			%>
		</basic:access>
	</layout:body>
</layout:html>