<%@page language="java" session="true"
extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.mig.html.layout.*"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="com.top_logic.knowledge.wrap.person.PersonManager"
%><%@page import="java.util.List"
%><%@page import="com.top_logic.knowledge.service.KBUtils"
%><%@page import="com.top_logic.knowledge.wrap.person.Person"
%><%@page import="java.util.ArrayList"
%><%@page import="com.top_logic.layout.basic.CommandModel"
%><%@page import="com.top_logic.tool.boundsec.HandlerResult"
%><%@page import="com.top_logic.layout.DisplayContext"
%><%@page import="com.top_logic.tool.boundsec.commandhandlers.GotoHandler"
%><%@page import="com.top_logic.layout.basic.ComponentCommandModel"
%><%@page import="java.util.HashMap"
%><%@page import="java.util.Map"
%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page import="java.io.Writer"
%><%@page import="java.io.IOException"
%><%@page import="com.top_logic.basic.xml.TagWriter"
%><layout:html>
	<layout:head/>
	<%
	LayoutComponent theComp   = MainLayout.getComponent(pageContext);
	JspWriter writer = pageContext.getOut();
	DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(pageContext);
	String theURL = theComp.getComponentURL(displayContext).getURL();
	PersonManager mgr = PersonManager.getManager();
	List restrictedUser = mgr.getAllAliveRestrictedPersons();
	List allUser = mgr.getAllAlivePersons();
	%>
	
	<%!private void writePersonList(PageContext pageContext, List persons) throws Exception {
		DisplayContext context = DefaultDisplayContext.getDisplayContext(pageContext);
		TagWriter out = MainLayout.getTagWriter(pageContext);
		for (int index = 0, size = persons.size(); index < size; index++) {
			Person current = (Person) persons.get(index);
			out.beginTag("li");
			GotoHandler.writeGotoStart(context, out, current);
			out.writeText(current.getLastName() + ", " + current.getFirstName() + " (" + Person.userOrNull(current).getUserName() + ")");
			GotoHandler.writeGotoEnd(out, current);
			out.endTag("li");
		}
		out.flushBuffer();
	}%>
	<layout:body>
		<basic:access>
			<h1>
				Information about info user!
			</h1>
			<div>
				<p>
					There are currently <%= restrictedUser.size()%> restricted user and <%= allUser.size() - restrictedUser.size() %> full user.
				</p>
				All info user:
				<br/>
				<ul>
					<%
					writePersonList(pageContext, restrictedUser);
					%>
				</ul>
				All non info user:
				<br/>
				<ul>
					<%
					List user = new ArrayList(allUser);
					user.removeAll(restrictedUser);
					writePersonList(pageContext, user);
					%>
				</ul>
			</div>
		</basic:access>
	</layout:body>
</layout:html>