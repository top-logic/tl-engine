<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><!DOCTYPE html>
<%@page import="com.top_logic.layout.servlet.CacheControl"
%><%@taglib uri="basic" prefix="basic"
%><%
CacheControl.setNoCache(response);
%>
<html>
	<head>
		<title>
			Server Response Site
		</title>
		<basic:script>
			function loaded(){
				var acTime = getActualTime();
				var save = true;
				parent.setServerTime(acTime, save);
				
			}
			
			function getActualTime(){
				var time = document.getElementById("time");
				return time.innerHTML;
			}
			
			//function log(message) {
				//	document.getElementById("console").appendChild(document.createTextNode(new Date() + ": " + message + "\r\n"));
			//}
		</basic:script>
	</head>
	<body onload="loaded()">
		<span id="time">
			<%
			out.print(System.currentTimeMillis());
			%>
		</span>

		<!--
			<h2>Console output</h2>
			<pre id="console" style="border-style: solid; border-width: 1px; border-color: red;"></pre>
		-->
	</body>
</html>