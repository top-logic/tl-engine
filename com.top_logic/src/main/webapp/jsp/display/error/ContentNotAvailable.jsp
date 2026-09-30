<%@page
extends="com.top_logic.util.TopLogicJspBase"
isErrorPage="true"
%><%@page import="com.top_logic.layout.ErrorPage"
%><%@taglib uri="basic" prefix="basic"
%>
<basic:html>
	<%
	String  requestedPage = (String) request.getAttribute(ErrorPage.JAKARTA_SERVLET_ERROR_REQUEST_URI);
	String context = request.getContextPath();
	%>
	<head>
		<title>
			Content not longer available
		</title>
		<link
			href="<%= context %>/jsp/display/error/error.css"
			rel="stylesheet"
			type="text/css"
		/>
	</head>
	<body>
		<h1>
			Requested resource not found
		</h1>
		<p>
			The content of URL '<%= requestedPage %>' you requested is not longer available.
		</p>
		<p>
			<a
				href="<%= context %>/jsp/main/LogoutPage.jsp"
				target="_top"
			>
				Logout
			</a>
		</p>

		<h1>
			Seite nicht gefunden
		</h1>
		<p>
			Der angeforderte Inhalt der URL '<%= requestedPage %>' ist nicht länger verfügbar.
		</p>
		<p>
			<a
				href="<%= context %>/jsp/main/LogoutPage.jsp"
				target="_top"
			>
				Abmelden
			</a>
		</p>
	</body>
</basic:html>