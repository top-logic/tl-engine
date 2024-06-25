<%@page import="com.top_logic.util.DefaultResourcesModule"
%><%@page import="com.top_logic.basic.ReloadableManager"
%><%@page extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="java.util.Enumeration"
%><%@page import="com.top_logic.basic.Logger"
%><%@page import="com.top_logic.util.Resources"
%><%@page import="com.top_logic.basic.util.ResourcesModule"
%><%@page import="com.top_logic.tool.boundsec.BoundHelper"
%><%@page import="com.top_logic.tool.boundsec.manager.AccessManager"
%><%@page import="com.top_logic.element.boundsec.manager.StorageAccessManager"
%><layout:html>
	<layout:head>
		<title>
			System Flags
		</title>
		<meta
			content="text/html; charset=iso-8859-1"
			http-equiv="Content-Type"
		/>
		<basic:cssLink/>
	</layout:head>

	

	<layout:body>
		<basic:access>
			<%
			// default checked state of reload resources checkbox
			boolean reloadResourcesChecked = false;
			
			if (request.getParameter("SUBMIT") != null) {
				boolean alwaysShowKeys = false;
				boolean logMissingKeys = false;
				boolean logDeprecatedKeys = false;
				boolean disableFallbackToDefLang = false;
				boolean traceMessages = false;
				boolean traceExceptions = false;
				boolean securityAutoUpdate = false;
				boolean reloadResources = false;
				boolean allowChangeDeleteProtection = false;
				boolean allowExecuteDisabledButtons = false;
				String exceptionLevel = request.getParameter("exceptionLevel");
				
				Enumeration<String> theEnum = request.getParameterNames();
				while (theEnum.hasMoreElements()) {
					String theElement  = theEnum.nextElement();
					alwaysShowKeys     = alwaysShowKeys     || (theElement.equals("alwaysShowKeys"));
					logMissingKeys     = logMissingKeys     || (theElement.equals("logMissingKeys"));
					logDeprecatedKeys  = logDeprecatedKeys  || (theElement.equals("logDeprecatedKeys"));
					traceMessages      = traceMessages      || (theElement.equals("traceMessages"));
					traceExceptions    = traceExceptions    || (theElement.equals("traceExceptions"));
					securityAutoUpdate = securityAutoUpdate || (theElement.equals("securityAutoUpdate"));
					reloadResources    = reloadResources    || (theElement.equals("reloadResources"));
					disableFallbackToDefLang    = disableFallbackToDefLang    || (theElement.equals("disableFallbackToDefLang"));
					allowChangeDeleteProtection = allowChangeDeleteProtection || (theElement.equals("allowChangeDeleteProtection"));
					allowExecuteDisabledButtons = allowExecuteDisabledButtons || (theElement.equals("allowExecuteDisabledButtons"));
				}
				
				ResourcesModule res = ResourcesModule.getInstance();
				res.setAlwaysShowKeys(alwaysShowKeys);
				res.setLogMissingKeys(logMissingKeys);
				res.setLogDeprecatedKeys(logDeprecatedKeys);
				res.setDisableFallbackToDefLang(disableFallbackToDefLang);
				Logger.setTraceMessages(traceMessages);
				Logger.setTraceExceptions(traceExceptions);
				
				if ((exceptionLevel != null) && !exceptionLevel.isEmpty()) {
					Logger.setExceptionLevel(exceptionLevel.toUpperCase().trim());
				}
				
				((StorageAccessManager) AccessManager.getInstance()).getSecurityStorage().setAutoUpdate(securityAutoUpdate);
				BoundHelper bh = BoundHelper.getInstance();
				bh.setAllowChangeDeleteProtection(allowChangeDeleteProtection);
				bh.setAllowExecuteDisabledButtons(allowExecuteDisabledButtons);
				
				if (reloadResources) {
					ReloadableManager.getInstance().reload(DefaultResourcesModule.RELOADABLE_KEY);
				}
				reloadResourcesChecked = reloadResources;
			}
			%>
			<h1>
				System Flags
			</h1>
			<p>
				This page allows to toggle some system flags. Changes are only active until next system startup.
			</p>
			<%
			if (request.getParameter("SUBMIT") != null) {
				%>
				<p>
					<%="done".equals(request.getParameter("done")) ? "Done." : "OK."%>
				</p>
				<%
			}
			%>
			<br/>
			<form method="POST">
				<table>
					<tr>
						<td>
							<p>
								<b>
									<basic:tag name="input">
										<basic:attribute name="type"
											value="checkbox"
										/>
										<basic:attribute name="name"
											value="alwaysShowKeys"
										/>
										<basic:attribute name="value"
											value="alwaysShowKeys"
										/>
										<basic:attribute name="checked"
											value="<%=ResourcesModule.getInstance().isAlwaysShowKeys() ? \"checked\" : null%>"
										/>
									</basic:tag>
									&#xA0;alwaysShowKeys
								</b>
								<br/>
								If turned on, instead of the I18N values, the lookup keys are shown in the
								following format:
								<br/>

								<code>
									flag '[' key '(' optional-arguments ')' | optional-fallback ']'
								</code>
								<br/>
								The flag may either be '
								<b>
									<%=ResourcesModule.FLAG_KEY_FOUND%>
								</b>
								' to indicate an existing
								resource, or '
								<b>
									<%=ResourcesModule.FLAG_KEY_NOT_FOUND%>
								</b>
								' to indicate a missing resource.
								<br/>
								Literal text (that is not defined as resource) is printed in double quotes: "some text".
								<br/>
							</p>
							<p>
								<b>
									<basic:tag name="input">
										<basic:attribute name="type"
											value="checkbox"
										/>
										<basic:attribute name="name"
											value="logMissingKeys"
										/>
										<basic:attribute name="value"
											value="logMissingKeys"
										/>
										<basic:attribute name="checked"
											value="<%=ResourcesModule.getInstance().isLogMissingKeys() ? \"checked\" : null%>"
										/>
									</basic:tag>
									&#xA0;logMissingKeys
								</b>
								<br/>
								If turned on, missing keys gets logged to the default logger.
							</p>
							<p>
								<b>
									<basic:tag name="input">
										<basic:attribute name="type"
											value="checkbox"
										/>
										<basic:attribute name="name"
											value="logDeprecatedKeys"
										/>
										<basic:attribute name="value"
											value="logDeprecatedKeys"
										/>
										<basic:attribute name="checked"
											value="<%=ResourcesModule.getInstance().isLogDeprecatedKeys() ? \"checked\" : null%>"
										/>
									</basic:tag>
									&#xA0;logDeprecatedKeys
								</b>
								<br/>
								If turned on, errors are logged, if deprecated keys are successfully resolved.
							</p>
							<p>
								<b>
									<basic:tag name="input">
										<basic:attribute name="type"
											value="checkbox"
										/>
										<basic:attribute name="name"
											value="disableFallbackToDefLang"
										/>
										<basic:attribute name="value"
											value="disableFallbackToDefLang"
										/>
										<basic:attribute name="checked"
											value="<%=ResourcesModule.getInstance().isDisableFallbackToDefLang() ? \"checked\" : null%>"
										/>
									</basic:tag>
									&#xA0;disableFallbackToDefLang
								</b>
								<br/>
								If turned on, resources lookup will not fallback to default language if a key was not found in requested language.
								<br/>
								Note: When changing this flag, resources must be reloaded to take any effects.
							</p>
							<p>
								<b>
									<basic:tag name="input">
										<basic:attribute name="type"
											value="checkbox"
										/>
										<basic:attribute name="name"
											value="traceMessages"
										/>
										<basic:attribute name="value"
											value="traceMessages"
										/>
										<basic:attribute name="checked"
											value="<%=Logger.isTraceMessages() ? \"checked\" : null%>"
										/>
									</basic:tag>
									&#xA0;traceMessages
								</b>
								<br/>
								If turned on, the stack trace will be added to each logger message.
							</p>
							<p>
								<b>
									<basic:tag name="input">
										<basic:attribute name="type"
											value="checkbox"
										/>
										<basic:attribute name="name"
											value="traceExceptions"
										/>
										<basic:attribute name="value"
											value="traceExceptions"
										/>
										<basic:attribute name="checked"
											value="<%=Logger.isTraceExceptions() ? \"checked\" : null%>"
										/>
									</basic:tag>
									&#xA0;traceExceptions
								</b>
								<br/>
								If turned on, the stack trace will be added to each logger exception.
							</p>
							<p>
								<b>
									exceptionLevel:
								</b>
								&#xA0;
								<input name="exceptionLevel"
									size="5"
									type="text"
									value="<%=Logger.getExceptionLevel() %>"
								/>
								<br/>
								Minimum level to write exceptions to the logger.
							</p>
							<p>
								<b>
									<basic:tag name="input">
										<basic:attribute name="type"
											value="checkbox"
										/>
										<basic:attribute name="name"
											value="securityAutoUpdate"
										/>
										<basic:attribute name="value"
											value="securityAutoUpdate"
										/>
										<basic:attribute name="checked"
											value="<%=((StorageAccessManager) AccessManager.getInstance()).getSecurityStorage().isAutoUpdate() ? \"checked\" : null%>"
										/>
									</basic:tag>
									&#xA0;securityAutoUpdate
								</b>
								<br/>
								If turned on, the security storage gets updated automatically after each change in application.
								<br/>
								If turned off, security persistence may be incorrect until next manual reload of the security storage.
							</p>
							<p>
								<b>
									<basic:tag name="input">
										<basic:attribute name="type"
											value="checkbox"
										/>
										<basic:attribute name="name"
											value="allowChangeDeleteProtection"
										/>
										<basic:attribute name="value"
											value="allowChangeDeleteProtection"
										/>
										<basic:attribute name="checked"
											value="<%=BoundHelper.getInstance().isAllowChangeDeleteProtection() ? \"checked\" : null%>"
										/>
									</basic:tag>
									&#xA0;allowChangeDeleteProtection
								</b>
								<br/>
								If turned on, delete protection flags my be edited by the user. This may be useful for example if you want to delete a delete protected meta attribute.
								If turned off, delete protection flags are immutable. This works in all supporting components, like EditMetaAttributes, EditGroups, EditRoles, ...
							</p>
							<p>
								<b>
									<basic:tag name="input">
										<basic:attribute name="type"
											value="checkbox"
										/>
										<basic:attribute name="name"
											value="allowExecuteDisabledButtons"
										/>
										<basic:attribute name="value"
											value="allowExecuteDisabledButtons"
										/>
										<basic:attribute name="checked"
											value="<%=BoundHelper.getInstance().isAllowExecuteDisabledButtons() ? \"checked\" : null%>"
										/>
									</basic:tag>
									&#xA0;allowExecuteDisabledButtons
								</b>
								<br/>
								If turned on, disabled buttons can be pressed anyway. This may be useful for debug purposes.
								To press a disabled button, press and hold the left mouse button over the disabled button for 3 seconds.
							</p>
							<p>
								<br/>
								<b>
									<basic:tag name="input">
										<basic:attribute name="type"
											value="checkbox"
										/>
										<basic:attribute name="name"
											value="reloadResources"
										/>
										<basic:attribute name="value"
											value="reloadResources"
										/>
										<basic:attribute name="checked"
											value="<%=reloadResourcesChecked ? \"checked\" : null%>"
										/>
									</basic:tag>
									&#xA0;Reload resources now
								</b>
								<br/>
								&#xA0;If checked, the resources will be reloaded also while applying the changes.
								<br/>
								<br/>
								<button class="tlButton cButton cmdButton" name="SUBMIT" type="submit">
									<h4 class="tlButtonLabel">Apply</h4>
								</button>
								<input name="done"
									type="hidden"
									value="<%="done".equals(request.getParameter("done")) ? "ok" : "done"%>"
								/>
							</p>
						</td>
					</tr>
				</table>
			</form>
			<br/>
		</basic:access>
	</layout:body>
</layout:html>