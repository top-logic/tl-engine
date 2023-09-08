<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="basic" prefix="basic"
%>
<basic:html>
	<%--	Test Uploading of Files (varies with browsers)
	
	Author:	KHA
	--%>
	<head>
		<title>
			Test File Upload
		</title>
	</head>
	<body>
		<form
			action="doUpload.jsp"
			enctype="multipart/form-data"
			method="post"
		>
			<input name="file"
				type="file"
			/>
			<br/>
			<input type="submit"/>
		</form>
	</body>
</basic:html>