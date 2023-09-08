<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page language="java" contentType="text/html; charset=UTF-8"%>
<html>
	<head>
		<title>
			Hällo Wörld!
		</title>
	</head>
	<body>
		Normal: Hällo Wörld!
		<br/>
		Java: <%="Hällo Wörld"%>
		<jsp:include page="<%= \"ISO.inc.jsp\" %>"/>
	</body>
</html>