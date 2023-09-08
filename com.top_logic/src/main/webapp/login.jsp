<%@page import="com.top_logic.util.license.LicenseTool"
%><%@page import="com.top_logic.base.accesscontrol.ApplicationPages"
%><%@page import="com.top_logic.basic.util.ResKey"
%><%@page extends="com.top_logic.util.NoContextJspBase"
%><%@page import="com.top_logic.layout.servlet.CacheControl"
%><%@page import="com.top_logic.layout.basic.AbstractDisplayContext"
%><%@page import="com.top_logic.layout.DisplayContext"
%><%@page import="com.top_logic.util.DeferredBootUtil"
%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page import="com.top_logic.base.administration.MaintenanceWindowManager"
%><%@page session="false"
%><%@page import="java.io.File,
com.top_logic.basic.StringServices,
com.top_logic.basic.xml.TagUtil,
com.top_logic.util.Resources"
%><%@page import="com.top_logic.mig.html.UserAgent"
%><%@taglib uri="basic" prefix="basic"
%><%if (DeferredBootUtil.redirectOnPendingBoot(request, response)) {
	return;
}

String contextPath = request.getContextPath();
String      theMessage      = request.getParameter("message");
boolean     hasMessage      = (theMessage != null);
Resources res = Resources.getInstance();

CacheControl.setNoCache(response);

if (!hasMessage) {
	theMessage = (String) request.getAttribute("errorMessage");
	hasMessage = (theMessage != null);
}
else {
	theMessage = res.getString(ResKey.legacy(theMessage));
}

if (StringServices.isEmpty(theMessage)) {
	theMessage = "";
}

String theLoginMessage = TagUtil.encodeXML(res.getString(ResKey.legacy("tl.login.message")));
String theDisabled = null;

String appName = res.getString(com.top_logic.layout.I18NConstants.APPLICATION_TITLE);
String appSubtitle = (com.top_logic.layout.Icons.HIDE_APP_TITLE_ON_LOGIN_PAGE.get()) ? null : appName;
%>
<basic:html>
	<!-- Note: IE does not accept comments before the top-level element in XHTML. -->

	<head>
		<title>
			<% TagUtil.writeText(out, appName); %>
		</title>
		<meta name="viewport"
			content="width=device-width, initial-scale=1, shrink-to-fit=no"
		/>
		<meta
			content="IE=edge"
			http-equiv="X-UA-Compatible"
		/>
		<basic:js/>
		<basic:bal/>
		<link
			href="<% TagUtil.writeText(out, contextPath + "/style/login.css"); %>"
			rel="stylesheet"
		/>
		<%@include file="doOnload.inc" %>
		<basic:script>			
			function doLogin(button) {
				// Disable login button by changing its function. Disabling the button would stop processing the
				// login after click.
				button.onclick = function() {return false; };
				
				var message = '<%=theLoginMessage%>';
				var element = document.getElementById("messageField");
				if (!element.firstChild) {
					element.appendChild(document.createTextNode(message));
				}
				else {
					element.firstChild.data = message;
				}
				
				// Continue with default form processing. Otherwise, the "Save this login information?" dialog of
				// the browser is also skipped.
				return true;
			}
			
			var DO_NOT_CLOSE = 'doNotClose=true';
			
			function openInRootFrame(name) {
				if (this == top) {
					return true;
				}
				if (name == null) {
					name = "invisible_frame"; // default name
				}
				// check, if we need to scan for the correct frame
				var fullURL     = parent.document.URL;
				var theQuery = fullURL.substring(fullURL.indexOf('?'), fullURL.length);
				if (theQuery.indexOf(DO_NOT_CLOSE) < 0) {
					return;
				}
				if (this == top) {
					// top frame is ok, but we need to check if we have an openend window to close
					if (confirm('<%=res.getString(ResKey.legacy("tl.login.noConnection"))%>\n<%=res.getString(ResKey.legacy("tl.login.closingWindow"))%>')) {
						self.close();
					}
					else {
						self.location = "<%=contextPath%>/jsp/pos/view/Empty.jsp";
					}
				}
				else {
					theRootFrame = findFrame(name);
					
					if (theRootFrame != null) {
						// we found the pos root frame
						theRootFrame.parent.location.href = self.location+'?'+DO_NOT_CLOSE;
					}
					else {
						// normal login/logout
						// do nothing
					}
				}
			}
			
			// ignore errors on page
			function handleError() {
				return true;
			}
		</basic:script>
		<basic:favicon/>
	</head>

	<body onload="doOnload();">
		<form name="LoginPage"
			action="<%=contextPath + ApplicationPages.getInstance().getLoginServletPath()%>"
			method="post"
		>
			<%
			com.top_logic.base.accesscontrol.LoginPageServlet.jspForwardParams(request, out);
			%>
			<div class="container">
				<!-- Message field top -->
				<div class="messageContainer">
					<%
					if(MaintenanceWindowManager.getInstance().getMaintenanceModeState() == MaintenanceWindowManager.IN_MAINTENANCE_MODE) {
						%>
						<div class="messageField">
							<% TagUtil.writeText(out, res.getString(ResKey.legacy("main.admin.maintenanceWindow.component.isInMaintenanceWindow"))); %>
						</div>
						<%
					} else if(MaintenanceWindowManager.getInstance().getMaintenanceModeState() == MaintenanceWindowManager.ABOUT_TO_ENTER_MAINTENANCE_MODE) {
						%>
						<div class="messageField">
							<% TagUtil.writeText(out, res.getString(ResKey.legacy("main.admin.maintenanceWindow.component.isAboutToEnterMaintenanceWindow"))); %>
						</div>
						<%
					}
					%>
				</div>

				<!-- Logo -->
				<div class="logodiv">
					<span class="logo">
						<basic:image
							altKey="<%= com.top_logic.layout.I18NConstants.APPLICATION_TITLE %>"
							icon="<%= com.top_logic.layout.Icons.APP_LOGO %>"
						/>
					</span>
					<% if (appSubtitle != null) { %>
						<span class="version">
							<% TagUtil.writeText(out, appSubtitle);%>
						</span>
					<% } %>
				</div>

				<!-- Login -->
				<div class="loginContainer">
					<div class="loginCentered">
						<div class="logindiv">
							<label>
								<%=res.getString(ResKey.legacy("tl.login.username"))%>
							</label>
							<basic:tag name="input">
								<basic:attribute name="type"
									value="text"
								/>
								<basic:attribute name="id"
									value="username"
								/>
								<basic:attribute name="name"
									value="username"
								/>
								<basic:attribute name="disabled"
									value="<%=theDisabled%>"
								/>
							</basic:tag>
							<label>
								<% TagUtil.writeText(out, res.getString(ResKey.legacy("tl.login.password"))); %>
							</label>
							<basic:tag name="input">
								<basic:attribute name="type"
									value="password"
								/>
								<basic:attribute name="id"
									value="password"
								/>
								<basic:attribute name="name"
									value="password"
								/>
								<basic:attribute name="disabled"
									value="<%=theDisabled%>"
								/>
							</basic:tag>
							<basic:tag name="input">
								<basic:attribute name="type"
									value="submit"
								/>
								<basic:attribute name="value"
									value="<%= res.getString(ResKey.legacy(\"tl.login.alt\"))%>"
								/>
								<basic:attribute name="onclick"
									value="return doLogin(this);"
								/>
								<basic:attribute name="disabled"
									value="<%=theDisabled%>"
								/>
							</basic:tag>
							<div class="tlLink">
								<a class="tlLink"
									href="http://www.top-logic.com"
									target="_blank"
								>
									Powered by
									<em>
										<% TagUtil.writeText(out, LicenseTool.productType()); %>
									</em>
								</a>
							</div>
						</div>
						<%@include file="/login.banner.inc" %>
					</div>
				</div>

				<!-- Message -->
				<div id="messageField"
					class="messageField label"
				>
					<%
					TagUtil.writeText(out, theMessage);
					%>
				</div>
			</div>
		</form>
	</body>
</basic:html>