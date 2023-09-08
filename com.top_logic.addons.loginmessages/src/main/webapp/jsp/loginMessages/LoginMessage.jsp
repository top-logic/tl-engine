<%@page import="com.top_logic.layout.form.control.IntegerInputControl"
%><%@page import="com.top_logic.addons.loginmessages.model.intf.LoginMessage"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.knowledge.wrap.AbstractWrapper"
%><%@taglib uri="basic"    prefix="basic"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="meta"     prefix="meta"
%><layout:html>
	<layout:head/>
	<layout:body>
		<meta:formPage>
			<meta:group>
				<basic:fieldset titleKeySuffix="contentAttributes">
					<form:vertical>
						<form:descriptionCell wholeLine="true">
							<form:description>
								<meta:label name="<%=LoginMessage.MESSAGE_ATTR %>"
									colon="true"
								/>
							</form:description>
							<form:cell>
								<form:cell>
									<meta:attribute name="<%=LoginMessage.MESSAGE_ATTR %>"/>
								</form:cell>
							</form:cell>
						</form:descriptionCell>
						<form:descriptionContainer>
							<meta:inputCell name="<%=LoginMessage.WINDOW_WIDTH_ATTR %>"
								cssStyle="width:4em"
							/>
							<meta:inputCell name="<%=LoginMessage.WINDOW_HEIGHT_ATTR %>"
								cssStyle="width:4em"
							/>
						</form:descriptionContainer>
					</form:vertical>
				</basic:fieldset>
				<basic:fieldset
					firstColumnWidth="200px"
					titleKeySuffix="controlAttributes"
				>
					<form:vertical>
						<form:cell>
							<form:cell>
								<meta:inputCell name="<%=LoginMessage.NAME_ATTR %>"
									cssStyle="width:100%"
								/>
							</form:cell>
						</form:cell>
						<form:cell>
							<form:cell>
								<meta:inputCell name="<%=LoginMessage.START_DATE_ATTR %>"/>
							</form:cell>
						</form:cell>
						<form:cell>
							<form:cell>
								<meta:inputCell name="<%=LoginMessage.END_DATE_ATTR %>"/>
							</form:cell>
						</form:cell>
						<form:cell>
							<form:cell>
								<form:descriptionCell>
									<form:description>
										<meta:label name="<%=LoginMessage.CONFIRM_DURATION_ATTR %>"
											colon="true"
										/>
									</form:description>
									<meta:custom name="<%=LoginMessage.CONFIRM_DURATION_ATTR %>"
										controlProvider="<%=IntegerInputControl.Provider.INSTANCE%>"
									/>
								</form:descriptionCell>
							</form:cell>
						</form:cell>
						<form:cell>
							<form:cell>
								<meta:inputCell name="<%=LoginMessage.ACTIVE_ATTR %>"/>
							</form:cell>
						</form:cell>
					</form:vertical>
				</basic:fieldset>
			</meta:group>
			<meta:attributes/>
		</meta:formPage>
	</layout:body>
</layout:html>