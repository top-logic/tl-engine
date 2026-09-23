<%@page extends="com.top_logic.util.NoContextJspBase" session="false"
%><%@page import="com.top_logic.base.accesscontrol.ApplicationPages"
%><%@page import="com.top_logic.base.accesscontrol.ExternalAuthenticationServlet"
%><%@page import="com.top_logic.base.accesscontrol.I18NConstants"
%><%@page import="com.top_logic.basic.StringServices"
%><%@page import="com.top_logic.basic.xml.TagUtil"
%><%@page import="com.top_logic.util.Resources"
%><%@taglib uri="basic" prefix="basic"
%><%
String loginName = StringServices.nonEmpty(request.getParameter(ExternalAuthenticationServlet.LOGIN_NAME_PARAM));
String loginURL = request.getContextPath() + ApplicationPages.getInstance().getLoginPage();
Resources res = Resources.getInstance();
String title = res.getString(I18NConstants.UNKNOWN_ACCOUNT_TITLE);
String message = loginName == null
? res.getString(I18NConstants.UNKNOWN_ACCOUNT_MESSAGE)
: res.getString(I18NConstants.UNKNOWN_ACCOUNT_MESSAGE__LOGIN_NAME.fill(loginName));
%>
<basic:html>
	<head>
		<meta name="viewport"
			content="width=device-width, initial-scale=1.0"
		/>
		<title>
			<% TagUtil.writeText(out, title); %>
		</title>

		<link
			href="<%= request.getContextPath() %>/style/standalone-page.css"
			rel="stylesheet"
		/>
	</head>

	<body>
		<div class="container">
			<div class="logo">
				<basic:image
					altKey="<%= com.top_logic.layout.I18NConstants.APPLICATION_TITLE %>"
					icon="<%= com.top_logic.layout.Icons.APP_LOGO %>"
				/>
			</div>
			<form action="<%= loginURL %>">
				<div class="caption">
					<div>
						<h1>
							<% TagUtil.writeText(out, title); %>
						</h1>
					</div>
					<p class="small">
						<% TagUtil.writeText(out, message); %>
					</p>
					<button type="submit">
						<% TagUtil.writeText(out, res.getString(I18NConstants.UNKNOWN_ACCOUNT_LOGIN)); %>
					</button>
				</div>
			</form>
		</div>
	</body>
</basic:html>