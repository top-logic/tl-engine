<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.basic.xml.TagUtil"
%><!DOCTYPE html>
<html>
	<head>
		<title>
			Page <% TagUtil.writeText(out, request.getParameter("page")); %>
		</title>
	</head>
	<body>
		<h1>
			Page <% TagUtil.writeText(out, request.getParameter("page")); %>
		</h1>
	</body>
</html>