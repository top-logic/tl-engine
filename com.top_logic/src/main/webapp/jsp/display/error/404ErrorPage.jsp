<%@page isErrorPage="true" session="false"
%><%@page import="com.top_logic.basic.Logger"
%><%@page import="java.text.SimpleDateFormat"
%><%@page import="java.util.Date"
%><%@page import="com.top_logic.layout.ErrorPage"
%><%@taglib uri="basic" prefix="basic"%>
<html>
	<%
	response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	
	String  context    = request.getContextPath();
	String  theReferer = request.getHeader("Referer");
	/* Do not use the JSP-variable "session": If there is currently no session a new session will be created for the request, which is undesired. */
	HttpSession existingSession = request.getSession(false);
	String  sessionId  = existingSession != null ? existingSession.getId() : "no session";
	String  thePage    = (String) request.getAttribute(ErrorPage.JAVAX_SERVLET_ERROR_REQUEST_URI);
	String  theDate    = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
	String 	theClientAddr = request.getRemoteAddr();
	String 	theClientHost = request.getRemoteHost();
	Logger.error("Error 404, page: '" + thePage + "' referer: '" + theReferer + "', session: '" + sessionId + "', remoteAddr: " + theClientAddr + ", remoteHost: " + theClientHost + ", time: " + theDate,  this);
	%>
	<head>
		<title>
			Resource not found
		</title>
		<basic:cssLink href="/jsp/display/error/error.css"/>
	</head>
	<body>
		<h1>
			Requested resource not found
		</h1>
		<p>
			The request was logged with timestamp
			<strong>
				<%= theDate %>
			</strong>
			.
		</p>

		<p>
			If you feel this is an error, please send a the timestamp together
			with a description of your last actions to a system administrator.
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
			Die Anfrage wurde mit dem Zeitstempel
			<strong>
				<%= theDate %>
			</strong>
			protokolliert.
		</p>

		<p>
			Wenn Sie meinen, es handelt sich hierbei um einen Fehler der Anwendung,
			schicken sie bitte den obigen Zeitstempel zusammen mit einer
			Beschreibung Ihrer letzten Aktionen an einen Systemadministrator.
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
</html>