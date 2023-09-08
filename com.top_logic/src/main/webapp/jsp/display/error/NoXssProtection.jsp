<%@page import="com.top_logic.base.accesscontrol.LoginPageServlet"
%><%@page import="com.top_logic.knowledge.gui.layout.HttpSecureHeaderFilter"
%><%@taglib uri="basic" prefix="basic"
%>
<html>
	<head>
		<title>
			No XSS protection enabled
		</title>
		<basic:cssLink/>
	</head>
	<body>
		<div class="frmBody">
			<h1>
				Application misconfigured
			</h1>

			<p>
				The application is not hardened against XSS attacks by setting at least the
				<code>
					X-XSS-Protection
				</code>
				header.
			</p>

			<p>
				Enabling protection can be done by configuring the filter
				<code>
					<%=HttpSecureHeaderFilter.class.getName()%>
				</code>
				in the application's
				<code>
					web.xml
				</code>
				.
			</p>

			<p>
				If you choose to add the security-relevant headers by configuring an
				upstream Apache server, you may disable this check in the init
				parameters of the
				<code>
					<%=LoginPageServlet.class.getName()%>
				</code>
				, using parameter
				<code>
					<%=LoginPageServlet.DISABLE_SECURE_HEADER_CHECK %>
				</code>
				.
			</p>
		</div>
	</body>
</html>