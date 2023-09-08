<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.contact.layout.person.related.EditRelatedPersonComponent"
%><%@page import="com.top_logic.knowledge.gui.layout.person.EditPersonComponent"
%><%@page import="com.top_logic.layout.form.FormField"
%><%@page import="com.top_logic.base.security.attributes.PersonAttributes"
%><%@page import="com.top_logic.base.security.device.TLSecurityDeviceManager"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="basic" prefix="basic"
%><%@page import="java.util.Collection"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.basic.thread.ThreadContext"
%><%@page import="com.top_logic.contact.layout.person.related.EditCurrentPersonComponent"%>
<layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage>
			<basic:fieldset titleKeySuffix="basic">
				<form:descriptionContainer>
					<form:inputCell name="<%= PersonAttributes.TITLE %>"/>
					<form:inputCell name="<%= PersonAttributes.GIVEN_NAME %>"/>
					<form:inputCell name="<%= PersonAttributes.SUR_NAME %>"/>
				</form:descriptionContainer>

				<form:descriptionContainer>
					<form:inputCell name="<%= PersonAttributes.USER_NAME %>"/>

					<form:descriptionCell>
						<form:description>
							<form:label name="<%=EditRelatedPersonComponent.FIELD_NAME_RELATED_CONTACT%>"/>
							<form:error name="<%=EditRelatedPersonComponent.FIELD_NAME_RELATED_CONTACT%>"/>
						</form:description>
						<form:popup name="<%=EditRelatedPersonComponent.FIELD_NAME_RELATED_CONTACT%>"/>
					</form:descriptionCell>

					<form:inputCell name="<%= PersonAttributes.CUSTOMER %>"/>
				</form:descriptionContainer>
			</basic:fieldset>

			<basic:fieldset titleKeySuffix="contact">
				<form:descriptionContainer>
					<form:inputCell name="<%= PersonAttributes.MOBILE_NR %>"/>
					<form:inputCell name="<%= PersonAttributes.INTERNAL_NR %>"/>
					<form:inputCell name="<%= PersonAttributes.EXTERNAL_NR %>"/>
					<form:inputCell name="<%= PersonAttributes.PRIVATE_NR %>"/>
				</form:descriptionContainer>

				<form:descriptionContainer>
					<!-- "description" is used for org-unit -->
					<form:descriptionCell>
						<form:description>
							<form:label name="<%=PersonAttributes.ORG_UNIT%>"/>
							<form:error name="<%=PersonAttributes.ORG_UNIT%>"
								icon="true"
							/>
						</form:description>

						<form:input name="<%=PersonAttributes.ORG_UNIT%>"
							columns="20"
							tabindex="16"
						/>
					</form:descriptionCell>
					<form:inputCell name="<%= EditPersonComponent.COUNTRY %>"/>
					<form:inputCell name="<%= EditPersonComponent.LANGUAGE %>"/>
					<form:inputCell name="<%= EditPersonComponent.TIME_ZONE %>"/>
				</form:descriptionContainer>

				<form:inputCell name="<%= PersonAttributes.MAIL_NAME %>"/>
				<form:inputCell name="<%= PersonAttributes.EXTERNAL_MAIL %>"/>
			</basic:fieldset>
			<form:groupCell titleKeySuffix="securityData">
				<%
				Collection<?> allDataDevices = TLSecurityDeviceManager.getInstance().getConfiguredDataAccessDeviceIDs();
				if(ThreadContext.isSuperUser() && !allDataDevices.isEmpty()){
					%>
					<form:inputCell name="<%= PersonAttributes.DATA_ACCESS_DEVICE_ID %>"/>
					<%
				}
				%>
				
				<%
				Collection<?> allAuthDevices = TLSecurityDeviceManager.getInstance().getConfiguredAuthenticationDeviceIDs();
				if(ThreadContext.isSuperUser() && !allAuthDevices.isEmpty()){
					%>
					<form:inputCell name="<%= PersonAttributes.AUTHENTICATION_DEVICE_ID %>"/>
					<%
				}
				%>
				<form:inputCell name="<%= PersonAttributes.RESTRICTED_USER %>"/>
				<form:ifExists name="<%= EditPersonComponent.GROUPS_FIELD_NAME %>">
					<form:inputCell name="<%= EditPersonComponent.GROUPS_FIELD_NAME %>"/>
				</form:ifExists>
			</form:groupCell>
			<basic:fieldset titleKeySuffix="configuration">
				<form:ifExists name="<%= EditCurrentPersonComponent.FIELD_START_PAGE_AUTOMATISM %>">
					<form:inputCell name="<%= EditCurrentPersonComponent.FIELD_START_PAGE_AUTOMATISM %>"/>
				</form:ifExists>
				<form:ifExists name="<%= EditCurrentPersonComponent.PERSONAL_CONFIGURATION_START_PAGE %>">
					<form:inputCell name="<%= EditCurrentPersonComponent.PERSONAL_CONFIGURATION_START_PAGE %>"/>
				</form:ifExists>
				<form:inputCell name="<%= EditPersonComponent.THEME_SELECTOR %>"/>
				<form:inputCell name="<%= EditCurrentPersonComponent.PERSONAL_CONFIGURATION_AUTO_TRANSLATE %>"/>
			</basic:fieldset>
		</form:formPage>
	</layout:body>
</layout:html>