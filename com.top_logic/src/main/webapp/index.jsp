<?xml version="1.0" encoding="ISO-8859-1"?>
<%@page import="com.top_logic.base.accesscontrol.ApplicationPages"
%><%@taglib uri="basic" prefix="basic"
%><%
String loginPage = ApplicationPages.getInstance().getLoginPage();
String theURL = request.getContextPath() + loginPage;
%>
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta
			content="0; URL=<%=theURL %>"
			http-equiv="refresh"
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
	</body>
</html>