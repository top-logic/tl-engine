<%@page import="com.top_logic.basic.StringServices"
%><%@page import="com.top_logic.basic.xml.TagUtil"
%><%@page extends="com.top_logic.util.NoContextJspBase"
%><%@taglib uri="basic" prefix="basic"
%><%@page import="com.top_logic.base.accesscontrol.ApplicationPages"
%><%@page import="com.top_logic.basic.util.ResKey"
%><%@page import="com.top_logic.util.Resources"
%><%
try {
	String logoutPage = ApplicationPages.getInstance().getLogoutPage();
	String logoutURL = StringServices.isEmpty(logoutPage) ? null : request.getContextPath() + logoutPage;
	String loginURL = request.getContextPath() + ApplicationPages.getInstance().getLoginPage();
	Resources res = Resources.getInstance();
	%>
	<basic:html>
		<head>
			<meta name="viewport"
				content="width=device-width, initial-scale=1.0"
			/>
			<% if (logoutURL != null) { %>
				<meta
					content="1; URL=<%=logoutURL %>"
					http-equiv="refresh"
				/>
			<% } %>
			<title>
				TopLogic Logout Screen
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
				<form action="<%=loginURL %>">
					<div class="caption">
						<div>
							<h1>
								<% TagUtil.writeText(out, res.getString(ResKey.legacy("tl.logout.message0"))); %>
							</h1>
						</div>
						<p class="small">
							<% TagUtil.writeText(out, res.getString(ResKey.legacy("tl.logout.message1"))); %>
						</p>
						<button type="submit">
							<% TagUtil.writeText(out, res.getString(ResKey.legacy("tl.logout.message2"))); %>
						</button>
					</div>
				</form>
			</div>
		</body>
	</basic:html>
	<%
} finally {
	// Note: Session invalidation must be done after rendering the page to
	// say goodbye in the users language.
	HttpSession theSession = request.getSession(/*create*/ false);
	if (theSession != null) {
		theSession.invalidate();
	}
}
%>