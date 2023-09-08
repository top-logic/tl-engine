<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><!DOCTYPE html>
<%@page import="com.top_logic.layout.servlet.CacheControl"
%><%@taglib uri="basic" prefix="basic"
%><%@page import="java.util.Random"%>
<%
CacheControl.setNoCache(response);
%>
<html>
	<head>
		<%
		boolean isBig = "big".equalsIgnoreCase(request.getParameter("mode"));
		int LOOP = (isBig) ? 5 : 1000;
		long starttime = System.currentTimeMillis();
		Random rand = new Random();
		
		%>
		<title>
			Server Response Site
		</title>

		<basic:script>
			function loaded(){
				
				
				parent.setServerTime(<%=LOOP%>);
				
			}
		</basic:script>
	</head>
	<body onload="loaded()">
		<p>
			<%
			String theImg = (isBig) ? "big.jpg" : "titleLeft.png";
			for(int i =0; i < LOOP ; i++){
				String imageFile0 =  theImg + "?" + rand.nextInt();
				%>
				<img
					alt="Pic"
					src="<%= imageFile0 %>"
				/>
				<%
			}
			%>
		</p>

		<!--
			<h2>Console output</h2>
			<pre id="console" style="border-style: solid; border-width: 1px; border-color: red;"></pre>
		-->
	</body>
</html>