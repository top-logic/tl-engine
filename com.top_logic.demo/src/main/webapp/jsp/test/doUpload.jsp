<%@page import="com.top_logic.basic.io.FileUtilities"
%><%@page import="java.io.File"
%><%@page import="java.io.InputStream"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="basic" prefix="basic"
%>
<basic:html>
	<%-- Test Uplaoding of Files (varies with browsers)
	
	Author:	KHA
	--%>
	<head>
		<title>
			File Upload Done
		</title>
	</head>
	<body>
		<%
		int 		length 	= request.getContentLength ();
		String 		type   	= request.getHeader ("Content-Type");
		InputStream	is		= request.getInputStream ();
		File		tmpFile = File.createTempFile("download",".dat");
		FileUtilities.copyFile(is, tmpFile);
		%>
		<pre>
			Copied <%= length %> bytes of type
			<%= type %>
			to File
			<%= tmpFile %>
			<br/>
		</pre>
		<a href="upload.jsp">
			back to Upload
		</a>
	</body>
</basic:html>