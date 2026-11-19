<?xml version="1.0" encoding="ISO-8859-1"?>
<%@page import="com.top_logic.base.accesscontrol.ApplicationPages"
%><%@page import="com.top_logic.base.discovery.AgentDiscoveryServlet"
%><%@taglib uri="basic" prefix="basic"
%><%
String loginPage = ApplicationPages.getInstance().getLoginPage();
String theURL = request.getContextPath() + loginPage;
String catalogURL = request.getContextPath() + AgentDiscoveryServlet.AI_CATALOG_PATH;
String llmsTxtURL = request.getContextPath() + AgentDiscoveryServlet.LLMS_TXT_PATH;
response.addHeader(AgentDiscoveryServlet.LINK_HEADER,
	"<" + catalogURL + ">; rel=\"" + AgentDiscoveryServlet.AI_CATALOG_LINK_RELATION + "\"");
%>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta
			content="0; URL=<%=theURL %>"
			http-equiv="refresh"
		/>
		<link
			href="<%=catalogURL %>"
			rel="<%=AgentDiscoveryServlet.AI_CATALOG_LINK_RELATION %>"
		/>
		<link
			href="<%=llmsTxtURL %>"
			rel="alternate"
			title="llms.txt"
			type="<%=AgentDiscoveryServlet.MARKDOWN_MEDIA_TYPE %>"
		/>
		<basic:cssLink/>
	</head>

	<body>
		<br/>
		<br/>
		<br/>
		<h2 align="center">
			Login
		</h2>
		<p align="center">
			In case you are not redirected click below.
		</p>
		<p align="center">
			<a href="<%=theURL %>">
				Login
			</a>
		</p>
		<p align="center">
			Agents and API clients: this application describes itself in
			<a href="<%=llmsTxtURL %>">llms.txt</a>.
		</p>
	</body>
</html>