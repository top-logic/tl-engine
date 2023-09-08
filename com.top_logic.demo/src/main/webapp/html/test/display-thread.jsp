<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="basic" prefix="basic"
%><%@page import="com.top_logic.layout.servlet.CacheControl"%>
<%
CacheControl.setNoCache(response);
%>
<html>
	<head>
		<title>
			Display thread
		</title>
	</head>
	<body onload="parent.stopDisplay()">
		<p>
			Display thread
		</p>

		<p>
			Running<%
			
			for (int n = 0; n < 50; n++) {
				%>.
				<basic:script>
					parent.nextDisplay();
				</basic:script>
				<%
				
				out.flush();
				Thread.sleep(50);
			}
			
			%>
		</p>
	</body>
</html>