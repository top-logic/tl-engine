<%@page import="com.top_logic.base.accesscontrol.ApplicationPages"
%><%@page import="com.top_logic.layout.ErrorPage"
%><%@page import="com.top_logic.mig.html.layout.LayoutComponent"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page language="java" isErrorPage="true" session="true"
extends="com.top_logic.util.TopLogicJspBase"
import ="jakarta.servlet.http.HttpServletResponse,
jakarta.servlet.RequestDispatcher,
java.util.Enumeration,
java.util.Date,
java.text.SimpleDateFormat,
java.io.PrintWriter,
com.top_logic.basic.Logger,com.top_logic.basic.version.Version"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<%--
	This page cares for any uncaught Throwable
	--%>
	<%
	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	
	SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	String timestamp = timestampFormat.format(new Date());
	
	String  uri    = (String) request.getAttribute(ErrorPage.JAVAX_SERVLET_ERROR_REQUEST_URI);
	if (exception == null) {
		exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
	}
	
	String message;
	if (exception != null) {
		message = exception.getMessage();
		if (message == null) {
			message = exception.getClass().getName();
		}
	} else {
		message = "No exception";
	}
	
	String componentName;
	LayoutComponent component = MainLayout.getComponent(pageContext);
	if (component != null) {
		componentName = component.getName().qualifiedName();
	} else {
		componentName = "No component";
	}
	
	Logger.error("Internal error: '" + message + "', timestamp: '" + timestamp + "', component: " + componentName + " uri: '" + uri + "'.", exception, this);
	// In case we die of a class cast or null pointer this was at least logged :-(
		String  context    = request.getContextPath();
		Version theVers    = (Version) application.getAttribute(Version.class.getName());
		String  startPage  = ApplicationPages.getInstance().getStartPage();
		%>
		<layout:head>
			<title>
				Internal error
			</title>
			<link
				href="<%= context %>/jsp/display/error/error.css"
				rel="stylesheet"
				type="text/css"
			/>
		</layout:head>
		<layout:body>
			<h1>
				Internal error
			</h1>
			<p>
				The error was logged with timestamp
				<strong>
					<%= timestamp %>
				</strong>
				.
				Please send a screenshot of this page to a system administrator to
				report the error.
			</p>
			<p>
				Continue with
				<a
					href="<%= context %><%= startPage %>"
					target="_top"
				>
					Reload Application
				</a>
				or
				<a
					href="<%= context %>/jsp/main/LogoutPage.jsp"
					target="_top"
				>
					Logout
				</a>
				.
			</p>

			<h1>
				Interner Fehler
			</h1>
			<p>
				Der Fehler wurde mit dem Zeitstempel
				<strong>
					<%= timestamp %>
				</strong>
				protokolliert.
				Bitte senden Sie einen Screenshot an den Systemadministrator um den
				Fehler zu melden.
			</p>

			<p>
				Weiter mit
				<a
					href="<%= context %><%= startPage %>"
					target="_top"
				>
					Seite neu laden
				</a>
				oder
				<a
					href="<%= context %>/jsp/main/LogoutPage.jsp"
					target="_top"
				>
					Ausloggen
				</a>
				.
			</p>
		</layout:body>
	</layout:html>