<%@page import="com.top_logic.layout.basic.AbstractDisplayContext"
%><%@page import="com.top_logic.basic.xml.TagUtil"
%><%@page import="com.top_logic.basic.version.Version"
%><%@page import="com.top_logic.util.DeferredBootUtil"
%><%@page session="false" import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@taglib uri="basic" prefix="basic"
%><%if (!DeferredBootUtil.isBootPending()) {
	response.sendRedirect(request.getContextPath());
	return;
}

AbstractDisplayContext displayContext = DefaultDisplayContext.setupDisplayContext(application, request, response);
try {%>
	<basic:html>
		<head>
			<basic:cssLink/>

			<title>
				System boot pending
			</title>
		</head>

		<body>
			<h1>
				Boot pending
			</h1>

			<p>
				The application <%=TagUtil.encodeXML(Version.getApplicationVersion().getName())%>
				is not yet ready for login. Please
				<a href="<%= request.getRequestURI() %>">
					retry
				</a>
				later.
			</p>
		</body>
	</basic:html>
	<%
} finally {
	DefaultDisplayContext.teardownDisplayContext(request);
}
%>