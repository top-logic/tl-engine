<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.contact.layout.person.related.EditRelatedPersonComponent"
%><%@page import="com.top_logic.knowledge.gui.layout.person.EditPersonComponent"
%><%@page import="com.top_logic.layout.form.FormField"
%><%@page import="com.top_logic.base.security.attributes.PersonAttributes"
%><%@page import="com.top_logic.base.security.device.TLSecurityDeviceManager"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="java.util.Collection"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.basic.thread.ThreadContext"%>
<layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage>
			<form:groupCell titleKeySuffix="basic">
				<form:descriptionContainer>
					<form:inputCell name="<%=PersonAttributes.TITLE%>"/>
					<form:inputCell name="<%=PersonAttributes.GIVEN_NAME%>"/>
					<form:inputCell name="<%=PersonAttributes.SUR_NAME%>"/>
				</form:descriptionContainer>

				<form:descriptionContainer>
					<form:inputCell name="<%=PersonAttributes.USER_NAME%>"/>
					<form:inputCell name="<%=EditRelatedPersonComponent.FIELD_NAME_RELATED_CONTACT%>"/>
					<form:inputCell name="<%=PersonAttributes.CUSTOMER%>"/>
				</form:descriptionContainer>
			</form:groupCell>

			<form:groupCell titleKeySuffix="contact">
				<!-- <table summary="Process information" -->
				<form:descriptionContainer>
					<form:inputCell name="<%=PersonAttributes.MOBILE_NR%>"/>
					<form:inputCell name="<%=PersonAttributes.INTERNAL_NR%>"/>
					<form:inputCell name="<%=PersonAttributes.EXTERNAL_NR%>"/>
					<form:inputCell name="<%=PersonAttributes.PRIVATE_NR%>"/>
				</form:descriptionContainer>

				<form:descriptionContainer>
					<form:inputCell name="<%=PersonAttributes.ORG_UNIT%>"/>
					<form:inputCell name="<%=EditPersonComponent.COUNTRY%>"/>
					<form:inputCell name="<%=EditPersonComponent.LANGUAGE%>"/>
					<form:inputCell name="<%=EditPersonComponent.TIME_ZONE%>"/>
				</form:descriptionContainer>

				<form:descriptionContainer>
					<form:inputCell name="<%=PersonAttributes.MAIL_NAME%>"/>
					<form:inputCell name="<%=PersonAttributes.EXTERNAL_MAIL%>"/>
				</form:descriptionContainer>
			</form:groupCell>

			<form:groupCell titleKeySuffix="securityData">
				<%
				Collection<?> allDataDevices = TLSecurityDeviceManager.getInstance().getConfiguredDataAccessDeviceIDs();
				if(ThreadContext.isSuperUser() && !allDataDevices.isEmpty()){
					%>
					<form:inputCell name="<%=PersonAttributes.DATA_ACCESS_DEVICE_ID%>"/>
					<%
				}
				%>
				
				<%
				Collection<?> allAuthDevices = TLSecurityDeviceManager.getInstance().getConfiguredAuthenticationDeviceIDs();
				if(ThreadContext.isSuperUser() && !allAuthDevices.isEmpty()){
					%>
					<form:inputCell name="<%=PersonAttributes.AUTHENTICATION_DEVICE_ID%>"/>
					<%
				}
				%>
				<form:inputCell name="<%=PersonAttributes.RESTRICTED_USER%>"
					labelFirst="false"
				/>
				<form:inputCell name="<%=EditPersonComponent.SUPER_USER_FIELD%>"
					labelFirst="false"
				/>
				<form:ifExists name="<%= EditPersonComponent.GROUPS_FIELD_NAME %>">
					<form:inputCell name="<%= EditPersonComponent.GROUPS_FIELD_NAME %>"/>
				</form:ifExists>
			</form:groupCell>

			<form:groupCell titleKeySuffix="configuration">
				<form:inputCell name="<%=EditPersonComponent.THEME_SELECTOR%>"/>
			</form:groupCell>
		</form:formPage>
	</layout:body>
</layout:html>