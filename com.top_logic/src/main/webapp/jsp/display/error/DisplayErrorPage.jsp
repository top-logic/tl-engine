<%@page import="com.top_logic.base.accesscontrol.ApplicationPages"
%><%@page import="com.top_logic.basic.util.ResKey"
%><%@page language="java" session="true"
extends="com.top_logic.util.TopLogicJspBase"
import ="jakarta.servlet.http.HttpServletResponse,
java.util.Collections,
java.util.Iterator,
com.top_logic.util.Resources,
java.io.PrintWriter,
java.util.Enumeration"
%><%@taglib uri="basic" prefix="basic"
%>
<basic:html>
	<%
	Resources res = Resources.getInstance();
	Iterator theErrors = Collections.EMPTY_LIST.iterator();
	String theCmdId    = null;
	String  context    = request.getContextPath();
	String  startPage  = ApplicationPages.getInstance().getStartPage();
	%>
	<head>
		<title>
			Infodialog: Error occured
		</title>
		<link
			href="<%= context %>/jsp/display/error/error.css"
			rel="stylesheet"
			type="text/css"
		/>
	</head>
	<body>
		<blockquote>
			<%=res.getMessage(ResKey.legacy("error.couldNotExecute"), theCmdId)%>
			<br/>
			<% while (theErrors.hasNext()) {
				%> <%=	res.getString((ResKey)theErrors.next()) %>
				<br/>
			<%	} %>
			<%=	res.getString(ResKey.legacy("error.infoNoSystemFailure"))%>
			<br/>
			<span class="link">
				<a
					href="<%= context %><%= startPage %>"
					target="_top"
				>
					<%=res.getString(ResKey.legacy("generic.button.navigation_back"))%>
				</a>
			</span>
		</blockquote>
		<hr/>
		<%	PrintWriter pout = new PrintWriter(out); %>
		
		<%=res.getString(ResKey.legacy("error.requestParameter"))
		%><%@include file="dumpRequest.inc.jsp" %>
	</body>
</basic:html>