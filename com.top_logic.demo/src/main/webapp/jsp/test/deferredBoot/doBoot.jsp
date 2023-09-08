<%@page import="com.top_logic.layout.basic.AbstractDisplayContext"
%><%@page import="com.top_logic.basic.StringServices"
%><%@page import="com.top_logic.util.DeferredBootUtil"
%><%@page session="false" import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@taglib uri="basic" prefix="basic"
%><%
if (!DeferredBootUtil.isBootPending()) {
	response.sendRedirect(request.getContextPath());
	return;
}

if (!StringServices.isEmpty(request.getParameter("boot"))) {
	DeferredBootUtil.boot();
	response.sendRedirect(request.getRequestURI());
	return;
}

AbstractDisplayContext displayContext = DefaultDisplayContext.setupDisplayContext(application, request, response);
try {
	%>
	<basic:html>
		<head>
			<basic:cssLink/>

			<title>
				Boot system
			</title>
		</head>

		<body>
			<h1>
				Boot system
			</h1>

			<form
				action="<%= request.getRequestURI()%>"
				method="post"
			>
				<input name="boot"
					type="submit"
					value="Boot"
				/>
			</form>
		</body>
	</basic:html>
	<%
	} finally {
	DefaultDisplayContext.teardownDisplayContext(request, displayContext);
}
%>