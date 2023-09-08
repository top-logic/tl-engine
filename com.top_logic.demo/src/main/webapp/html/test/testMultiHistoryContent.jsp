<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><!DOCTYPE html>
<html>
	<head>
		<title>
			Page <%= request.getParameter("page")%>
		</title>
	</head>
	<body>
		<h1>
			Page <%= request.getParameter("page")%>
		</h1>
	</body>
</html>