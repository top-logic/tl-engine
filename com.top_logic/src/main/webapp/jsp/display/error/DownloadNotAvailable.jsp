<%@page extends="com.top_logic.util.TopLogicJspBase"
isErrorPage="true"
%><%@taglib uri="basic" prefix="basic"
%><%@page import="java.io.PrintWriter"
%><%@page import="com.top_logic.basic.version.Version"
%>
<basic:html>
	<head>
		<title>
			Download not available
		</title>
		<link
			href="<%= request.getContextPath() %>/jsp/display/error/error.css"
			rel="stylesheet"
			type="text/css"
		/>
	</head>
	<body>
		<blockquote>
			The file you requested is not longer available.
			<br/>
			Please request download again.
			<br/>
		</blockquote>
		<blockquote>
			Die gewünschte Datei ist nicht länger verfügbar.
			<br/>
			Bitte fordern sie den Download erneut an.
		</blockquote>
	</body>
</basic:html>