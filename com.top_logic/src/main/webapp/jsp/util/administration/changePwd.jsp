<%@page language="java" session="false" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="basic" prefix="basic"
%><%@page import="com.top_logic.base.accesscontrol.I18NConstants"
%><%@page import="com.top_logic.base.accesscontrol.LoginPageServlet"
%><%@page import="com.top_logic.base.security.password.PasswordManager"
%><%@page import="com.top_logic.base.security.password.PasswordValidator"
%><%@page import="com.top_logic.basic.xml.TagUtil"
%><%@page import="com.top_logic.basic.util.ResKey"
%><%@page import="com.top_logic.basic.StringServices"
%><%@page import="com.top_logic.knowledge.wrap.person.Person"
%><%@page import="com.top_logic.knowledge.wrap.person.PersonManager"
%><%@page import="com.top_logic.layout.ResPrefix"
%><%@page import="com.top_logic.tool.boundsec.commandhandlers.GotoHandler"
%><%@page import="com.top_logic.util.Resources"
%><%
String uid = request.getParameter("username");
Person account = PersonManager.getManager().getPersonByName(uid);
PasswordValidator validator = PasswordManager.getInstance().getPwdValidator();
String loginUrl = "/servlet/login";
String contextPath = request.getContextPath();
ResKey messageKey = (ResKey) request.getAttribute(LoginPageServlet.MESSAGE_ATTR);
Resources res = Resources.getInstance();
String message = messageKey == null ? null : res.getString(messageKey);
String title = StringServices.nonNull(account.getTitle());
String firstName = StringServices.nonNull(account.getFirstName());
String lastName = StringServices.nonNull(account.getLastName());
String fullName = title + " " + firstName + " " + lastName;
boolean pwdExpired = validator.isPasswordExpired(account);

//saving goto params to restore them in formular
String paramComponent = StringServices.nonEmpty(request.getParameter(GotoHandler.COMMAND_PARAM_COMPONENT));
String paramId = StringServices.nonEmpty(request.getParameter(GotoHandler.COMMAND_PARAM_ID));
String paramType = StringServices.nonEmpty(request.getParameter(GotoHandler.COMMAND_PARAM_TYPE));
%>
<basic:html>
	<head>
		<meta name="viewport"
			content="width=device-width, initial-scale=1.0"
		/>
		<title>
			<basic:text key="<%= I18NConstants.PWD_CHANGE_TITLE %>"/>
		</title>
		<style>
			body {
				overflow: hidden;
				font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
				margin: 0;
			}
			
			.container {
				display: flex;
				flex-direction: column;
				justify-content: center;
				height: 100vh;
				margin: 0 auto;
				/* background overlay */
				background: rgba(16, 17, 19, 0.35);
			}
			
			.content {
				margin: 0 auto;
				background: rgba(255, 255, 255, 1);
				padding: 16px;
				/* Shadows/menu/shadow-menu */
				box-shadow: 0px 2px 6px 0px rgba(0, 0, 0, 0.32);
			}
			
			.heading {
				margin-bottom: 1rem;
			}
			
			label {
				display: inline-block;
				width: 160px;
			}
			
			input {
				width: 100%;
				flex: 1;
			}
			
			.input {
				display: flex;
				margin-bottom: 1rem;
			}
			
			.policy {
				min-width: 32rem;
				max-width: 40rem;
				padding: 1rem;
				margin-bottom: 2rem;
				background-color: #F0F1F2;
				color: #505358;
			}
			
			.submit {
				display: flex;
				flex-direction: column;
				align-items: flex-end;
			}
			
			/** Button styling **/
			/* Reset styling*/
			button {
				background-color: transparent;
				border-width: 0;
				font-family: inherit;
				font-size: inherit;
				font-style: inherit;
				font-weight: inherit;
				line-height: inherit;
				padding: 0;
			}
			
			button {
				/* display + box model */
				min-width: 120px;
				height: 2.5rem;
				border-radius: .25rem;
				padding: .5rem 1rem;
				/* color */
				background-color: #0090b8;
				color: white;
				/* text label */
				font-weight: 600;
				letter-spacing: 1px;
			}
			
			button:hover {
				background-color: #0078a3;
			}
			
			/* Text styling */
			h1, h2, h3, h4, h5, h6, p {
				margin: 0;
				margin-bottom: 1rem;
			}
		</style>

		<basic:script>
	function showMessage(){
		var message = <% TagUtil.writeJsString(out, message); %>;
		if (message != null && message != "") {
			alert(message);
		}
	}
	</basic:script>
	</head>

	<body onload="showMessage()">
		<div class="container">
			<div class="content">
				<div class="heading">
					<h1>
						<basic:text key="<%= I18NConstants.PWD_CHANGE_WELCOME__USER.fill(fullName) %>"/>
					</h1>
					<h3>
						<basic:text key="<%= I18NConstants.PWD_CHANGE_PASSWORD_EXPIRED %>"/>
						<br/>
						<basic:text key="<%= I18NConstants.PWD_CHANGE_NEED_UPDATE %>"/>
					</h3>
				</div>
				<div class="policy">
					<h4>
						<basic:text key="<%= I18NConstants.PWD_CHANGE_POLICY %>"/>
					</h4>
					<ul>
						<li>
							<basic:text key="<%= I18NConstants.PWD_CHANGE_MIN_LENGTH__LENGTH.fill(validator.getMinPwdLength()) %>"/>
						</li>
						<% if (validator.getNumberContentCrit() > 0) { %>
							<li>
								<basic:text key="<%= I18NConstants.PWD_CHANGE_MIN_CRITERIA__CNT.fill(validator.getNumberContentCrit()) %>"/>
								<ul>
									<li>
										<basic:text key="<%= I18NConstants.PWD_CHANGE_CRITERIA_UPPER %>"/>
									</li>
									<li>
										<basic:text key="<%= I18NConstants.PWD_CHANGE_CRITERIA_LOWER %>"/>
									</li>
									<li>
										<basic:text key="<%= I18NConstants.PWD_CHANGE_CRITERIA_NUMBER %>"/>
									</li>
									<li>
										<basic:text key="<%= I18NConstants.PWD_CHANGE_CRITERIA_SPECIAL %>"/>
									</li>
								</ul>
							</li>
						<% } %>
					</ul>
					<% if (validator.getExpiryPeriod() > 0) { %>
						<p>
							<basic:text key="<%= I18NConstants.PWD_CHANGE_EXPIRY__DAYS.fill(validator.getExpiryPeriod()) %>"/>
						</p>
					<% } %>
					<% if (validator.getPwdRepeatCycle() > 0) { %>
						<p>
							<basic:text key="<%= I18NConstants.PWD_CHANGE_CYCLE__CNT.fill(validator.getPwdRepeatCycle()) %>"/>
						</p>
					<% } %>
				</div>

				<form name="changePwd"
					action="<%=contextPath%><%=loginUrl%>"
					method="post"
				>
					<%if (!StringServices.isEmpty(paramId)) {%>
						<input name="<%=GotoHandler.COMMAND_PARAM_ID%>"
							type="hidden"
							value="<%=paramId%>"
						/>
					<%}%>
					<%if (!StringServices.isEmpty(paramType)) {%>
						<input name="<%=GotoHandler.COMMAND_PARAM_TYPE%>"
							type="hidden"
							value="<%=paramType%>"
						/>
					<%}%>
					<%if (!StringServices.isEmpty(paramComponent)) {%>
						<input name="<%=GotoHandler.COMMAND_PARAM_COMPONENT%>"
							type="hidden"
							value="<%=paramComponent%>"
						/>
					<%}%>
					<input name="changePwd"
						type="hidden"
						value="true"
					/>
					<input name="username"
						type="hidden"
						value="<%=uid%>"
					/>

					<div class="text-input">
						<div class="input">
							<label for="password">
								<basic:text key="<%= I18NConstants.PWD_CHANGE_CURRENT%>"/>
								:
							</label>
							<basic:tag name="input">
								<basic:attribute name="name"
									value="password"
								/>
								<basic:attribute name="aria-label">
									<basic:text key="<%= I18NConstants.PWD_CHANGE_CURRENT%>"/>
								</basic:attribute>
								<basic:attribute name="type"
									value="password"
								/>
							</basic:tag>
						</div>
						<div class="input">
							<label for="pwd1">
								<basic:text key="<%= I18NConstants.PWD_CHANGE_NEW%>"/>
								:
							</label>
							<basic:tag name="input">
								<basic:attribute name="name"
									value="pwd1"
								/>
								<basic:attribute name="aria-label">
									<basic:text key="<%= I18NConstants.PWD_CHANGE_NEW%>"/>
								</basic:attribute>
								<basic:attribute name="type"
									value="password"
								/>
							</basic:tag>
						</div>
						<div class="input">
							<label for="pwd2">
								<basic:text key="<%= I18NConstants.PWD_CHANGE_CONFIRM%>"/>
								:
							</label>
							<basic:tag name="input">
								<basic:attribute name="name"
									value="pwd2"
								/>
								<basic:attribute name="aria-label">
									<basic:text key="<%= I18NConstants.PWD_CHANGE_CONFIRM%>"/>
								</basic:attribute>
								<basic:attribute name="type"
									value="password"
								/>
							</basic:tag>
						</div>
					</div>

					<div class="submit">
						<button type="submit">
							<basic:text key="<%= I18NConstants.PWD_CHANGE_SUBMIT%>"/>
						</button>
					</div>
				</form>
			</div>
		</div>
	</body>
</basic:html>