<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.basic.util.ResKey"
%><%@page import="com.top_logic.base.accesscontrol.ApplicationPages"
%><%@page import="com.top_logic.layout.ContentHandlersRegistry"
%><%@page import="com.top_logic.basic.version.Version"
%><%@page import="com.top_logic.util.Resources"
%><%@page import="com.top_logic.mig.html.UserAgent"
%><%@taglib uri="basic" prefix="basic"
%><%
String theContext = request.getContextPath();
boolean allowSubsession = ContentHandlersRegistry.allowSubsession(pageContext);
%>
<basic:html>
	<head>
		<title>
			<basic:text key="<%= com.top_logic.layout.I18NConstants.APPLICATION_TITLE %>"/>
		</title>
		<basic:js/>
		<basic:favicon/>
		<style type="text/css">
			* {
				box-sizing: border-box;
				margin: 0;
				padding: 0;
			}
			
			body {
				font-family: Arial, sans-serif;
				background-color: #FFFFFF;
				display: flex;
				justify-content: center;
				align-items: center;
				min-height: 100vh;
				padding: 20px;
			}
			
			.container {
				display: flex;
				flex-direction: column;
				align-items: center;
				max-width: 400px;
				width: 100%;
			}
			
			.app-logo {
				width: 280px;
				height: 80px;
				background-image: url(<%=theContext%>/images/appLogo.svg);
				background-repeat: no-repeat;
				background-size: contain;
				margin-bottom: 20px;
			}
			
			.login-box {
				background-color: #f0f1f2;
				border: 4px solid #2968C8;
				border-radius: 24px;
				padding: 40px 30px;
				width: 100%;
				max-width: 320px;
				text-align: center;
				box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
			}
			
			.message {
				font-size: 14px;
				font-weight: bold;
				color: #6F6F6F;
				margin-bottom: 20px;
				text-align: center;
			}
			
			.button-container {
				display: flex;
				justify-content: center;
				gap: 10px;
				margin-top: 20px;
			}
			
			.cta-button {
				background-color: #2968C8;
				color: white;
				padding: 12px 24px;
				border: none;
				border-radius: 8px;
				cursor: pointer;
				font-weight: bold;
				font-size: 12px;
				display: inline-flex;
				align-items: center;
				justify-content: center;
				text-decoration: none;
				position: relative;
			}
			
			.cta-button:hover {
				background-color: #205cb4;
			}
		</style>
	</head>

	<body class="<%=UserAgent.getUserAgent(request).getCSSClasses("")%>">
		<div class="container">
			<div class="app-logo">
			</div>
			<div class="login-box">
				<div class="message">
					<basic:text key="<%= (allowSubsession ? com.top_logic.layout.I18NConstants.SUBSESSION_CREATE__NAME : com.top_logic.layout.I18NConstants.SUBSESSION_DENY__NAME).fill(Version.getApplicationVersion().getName()) %>"/>
				</div>
				<div class="button-container">
					<a class="cta-button"
						href="#"
						onclick="window.close(); return false;"
					>
						<basic:text key="<%= com.top_logic.layout.I18NConstants.SUBSESSION_CANCEL %>"/>
					</a>
					<% if (allowSubsession) { %>
						<a class="cta-button"
							href="<%=theContext + ApplicationPages.getInstance().getStartPage() %>"
						>
							<basic:text key="<%= com.top_logic.layout.I18NConstants.SUBSESSION_LOAD %>"/>
						</a>
					<% } %>
				</div>
			</div>
		</div>
	</body>
</basic:html>