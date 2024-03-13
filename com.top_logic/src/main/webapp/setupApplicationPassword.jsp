<%@page import="com.top_logic.basic.xml.TagUtil"
%><%@page import="com.top_logic.basic.util.ResKey"
%><%@page import="com.top_logic.base.taglibs.basic.TextTag"
%><%@page import="com.top_logic.layout.basic.AbstractDisplayContext"
%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page import="com.top_logic.knowledge.service.encryption.pbe.ApplicationSetup.Account"
%><%@page import="com.top_logic.knowledge.service.encryption.pbe.ApplicationSetup"
%><%@page session="false"
%><%@taglib uri="basic" prefix="basic"
%><%
AbstractDisplayContext displayContext = DefaultDisplayContext.setupDisplayContext(application, request, response);
try {
	ApplicationSetup model = new ApplicationSetup(application, request, response);
	if (model.phaseFinished()) {
		return;
	}
	%>
	<basic:html>
		<head>
			<basic:cssLink/>
			<link
				href="applicationPassword.css"
				rel="stylesheet"
				type="text/css"
			/>

			<title>
				<basic:text value="<%=model.getApplicationName()%>"/>
				:
				
				<%
				if (model.isSetup()) {
					%>
					<basic:text i18n="tl.setup.initial.title"/>
					<%
				} else {
					%>
					<basic:text i18n="tl.setup.change.title"/>
					<%
				}
				%>
			</title>
		</head>

		<body>
			<h1>
				<basic:text value="<%=model.getApplicationName()%>"/>
				:
				
				<%
				if (model.isSetup()) {
					%>
					<basic:text i18n="tl.setup.initial.title"/>
					<%
				} else {
					%>
					<basic:text i18n="tl.setup.change.title"/>
					<%
				}
				%>
			</h1>

			<div class="values">
				<%
				if (model.errorInvalidKey()) {
					%>
					<div class="error">
						<basic:text i18n="tl.setup.errorInvalidKey"/>
					</div>
					<%
				}
				
				if (model.errorPasswordMissmatch()) {
					%>
					<div class="error">
						<basic:text i18n="tl.setup.errorPasswordMissmatch"/>
					</div>
					<%
				}
				
				if (model.errorInvalidPassword()) {
					%>
					<div class="error">
						<basic:text i18n="tl.setup.errorInvalidPassword"/>
					</div>
					<%
				}
				
				if (model.errorInternal()) {
					%>
					<div class="error">
						<basic:text i18n="tl.setup.errorInternal"/>
					</div>
					<%
				}
				%>
				<div>
					<form
						action="#"
						method="post"
					>
						<%
						if (model.phaseKeyInput()) {
							%>
							<p>
								<div>
									<basic:text i18n="tl.setup.applicationKey"/>
								</div>
								<div>
									<input name="key"
										autocomplete="off"
										type="text"
									/>
								</div>
							</p>

							<div>
								<input
									type="submit"
									value="<%=TextTag.attributeText(ResKey.legacy("tl.setup.buttonNext")) %>"
								/>
							</div>
							<%
						} else {
							%>
							<input name="key"
								type="hidden"
								value="<%=TagUtil.encodeXMLAttribute(model.getApplicationKey()) %>"
							/>
							<%
						}
						%>
						
						<%
						if (model.phasePasswordInput()) {
							if (!model.isSetup()) {
								%>
								<p>
									<div>
										<basic:text i18n="tl.setup.oldPassphrase"/>
									</div>
									<div>
										<input name="oldPassword"
											autocomplete="off"
											type="password"
										/>
									</div>
								</p>
								<%
							}
							%>
							<p>
								<div>
									<%
									if (model.isSetup()) {
										%>
										<basic:text i18n="tl.setup.initialPassphrase"/>
										<%
									} else {
										%>
										<basic:text i18n="tl.setup.newPassphrase"/>
										<%
									}
									%>
								</div>
								<div>
									<input name="newPassword"
										autocomplete="off"
										type="password"
									/>
								</div>
							</p>

							<p>
								<div>
									<basic:text i18n="tl.setup.retypePassphrase"/>
								</div>
								<div>
									<input name="passwordTwice"
										autocomplete="off"
										type="password"
									/>
								</div>
							</p>
							<%
							if (model.isSetup()) {
								%>
								<div>
									<input
										type="submit"
										value="<%=TextTag.attributeText(ResKey.legacy("tl.setup.buttonNext")) %>"
									/>
								</div>
								<%
							} else {
								%>
								<div>
									<input
										type="submit"
										value="<%=TextTag.attributeText(ResKey.legacy("tl.setup.buttonFinishChange"))%>"
									/>
								</div>
								<%
							}
							%>
							
							<%
						} else {
							%>
							<input name="newPassword"
								type="hidden"
								value="<%=TagUtil.encodeXMLAttribute(model.getNewPassword()) %>"
							/>
							<input name="passwordTwice"
								type="hidden"
								value="<%=TagUtil.encodeXMLAttribute(model.getPasswordTwice()) %>"
							/>
							<%
						}
						%>
					</form>
				</div>
			</div>
		</body>
	</basic:html>
	<%
} finally {
	DefaultDisplayContext.teardownDisplayContext(request, displayContext);
}
%>