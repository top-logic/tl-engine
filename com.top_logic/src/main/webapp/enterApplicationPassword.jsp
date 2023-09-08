<%@page import="com.top_logic.base.taglibs.basic.TextTag"
%><%@page import="com.top_logic.layout.basic.AbstractDisplayContext"
%><%@page import="com.top_logic.basic.Logger"
%><%@page import="com.top_logic.util.BootFailure"
%><%@page import="com.top_logic.basic.module.ModuleException"
%><%@page import="com.top_logic.basic.version.Version"
%><%@page session="false" import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page import="com.top_logic.util.DeferredBootUtil"
%><%@page import="com.top_logic.knowledge.service.encryption.pbe.ApplicationPasswordUtil"
%><%@page import="com.top_logic.knowledge.service.encryption.pbe.InvalidPasswordException"
%><%@page import="com.top_logic.basic.StringServices"
%><%@taglib uri="basic" prefix="basic"
%><%if (!DeferredBootUtil.isBootPending()) {
	response.sendRedirect(request.getContextPath());
	return;
}

boolean invalidPassword = false;
boolean internalError = false;
String password = request.getParameter("password");
if (!StringServices.isEmpty(password)) {
	try {
		ApplicationPasswordUtil.usePassword(password);
		
		response.sendRedirect(request.getContextPath());
		return;
	} catch (InvalidPasswordException ex) {
		Logger.info("Invalid password.", ApplicationPasswordUtil.class);
		invalidPassword = true;
	} catch (ModuleException ex) {
		Logger.error("Module error: " + ex.getMessage(), ex, ApplicationPasswordUtil.class);
		internalError = true;
	} catch (BootFailure ex) {
		Logger.error("Boot error: " + ex.getMessage(), ex, ApplicationPasswordUtil.class);
		internalError = true;
	}
}

AbstractDisplayContext displayContext = DefaultDisplayContext.setupDisplayContext(application, request, response);
try {%>
	<basic:html>
		<head>
			<basic:cssLink/>
			<link
				href="applicationPassword.css"
				rel="stylesheet"
				type="text/css"
			/>

			<title>
				<basic:text value="<%=Version.getApplicationVersion().getName()%>"/>
				:
				<basic:text i18n="tl.setup.start.title"/>
			</title>
		</head>

		<body>
			<h1>
				<basic:text value="<%=Version.getApplicationVersion().getName()%>"/>
				:
				<basic:text i18n="tl.setup.start.title"/>
			</h1>

			<div class="values">
				<% if(invalidPassword) { %>
					<div class="error">
						<basic:text i18n="tl.setup.errorWrongPassword"/>
					</div>
				<% } %>
				
				<% if(internalError) { %>
					<div class="error">
						<basic:text i18n="tl.setup.errorInternal"/>
					</div>
				<% } %>
				<div>
					<form
						action="#"
						method="post"
					>
						<p>
							<div>
								<basic:text i18n="tl.setup.applicationPassphrase"/>
							</div>
							<div>
								<input name="password"
									autocomplete="off"
									type="password"
								/>
							</div>
						</p>

						<div>
							<input
								type="submit"
								value="<%=TextTag.attributeText(com.top_logic.util.I18NConstants.BUTTON_STARTUP) %>"
							/>
						</div>
					</form>
				</div>
				<br/>
				<div style="text-align:right">
					<a href="setupApplicationPassword.jsp">
						<basic:text i18n="tl.setup.buttonPassphraseChange"/>
					</a>
				</div>
			</div>
		</body>
	</basic:html>
	<%
} finally {
	DefaultDisplayContext.teardownDisplayContext(request, displayContext);
}
%>