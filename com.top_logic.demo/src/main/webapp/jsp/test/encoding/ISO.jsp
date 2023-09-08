<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page language="java" contentType="text/html; charset=ISO-8859-1"%>
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
		<jsp:include page="<%= \"UTF-8.inc.jsp\" %>"/>
	</body>
</html>