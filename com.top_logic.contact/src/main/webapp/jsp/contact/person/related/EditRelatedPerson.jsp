<%@page import="com.top_logic.knowledge.wrap.person.Person"%>
<%@page import="com.top_logic.base.user.UserInterface"%>
<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.contact.layout.person.related.EditRelatedPersonComponent"
%><%@page import="com.top_logic.knowledge.gui.layout.person.EditPersonComponent"
%><%@page import="com.top_logic.layout.form.FormField"
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
					<form:inputCell name="<%=UserInterface.TITLE%>"/>
					<form:inputCell name="<%=UserInterface.NAME%>"/>
					<form:inputCell name="<%=UserInterface.FIRST_NAME%>"/>
				</form:descriptionContainer>

				<form:descriptionContainer>
					<form:inputCell name="<%=UserInterface.USER_NAME%>"/>
					<form:inputCell name="<%=EditRelatedPersonComponent.FIELD_NAME_RELATED_CONTACT%>"/>
				</form:descriptionContainer>
			</form:groupCell>

			<form:groupCell titleKeySuffix="contact">
				<!-- <table summary="Process information" -->
				<form:descriptionContainer>
					<form:inputCell name="<%=UserInterface.PHONE%>"/>
				</form:descriptionContainer>

				<form:descriptionContainer>
					<form:inputCell name="<%=EditPersonComponent.COUNTRY%>"/>
					<form:inputCell name="<%=EditPersonComponent.LANGUAGE%>"/>
					<form:inputCell name="<%=EditPersonComponent.TIME_ZONE%>"/>
				</form:descriptionContainer>

				<form:descriptionContainer>
					<form:inputCell name="<%=UserInterface.EMAIL%>"/>
				</form:descriptionContainer>
			</form:groupCell>

			<form:groupCell titleKeySuffix="securityData">
				<%
				Collection<?> allDataDevices = TLSecurityDeviceManager.getInstance().getConfiguredDataAccessDeviceIDs();
						if(ThreadContext.isAdmin() && !allDataDevices.isEmpty()){
				%>
					<form:inputCell name="<%=Person.DATA_ACCESS_DEVICE_ID%>"/>
					<%
					}
					%>
				
				<%
								Collection<?> allAuthDevices = TLSecurityDeviceManager.getInstance().getConfiguredAuthenticationDeviceIDs();
										if(ThreadContext.isAdmin() && !allAuthDevices.isEmpty()){
								%>
					<form:inputCell name="<%=Person.AUTHENTICATION_DEVICE_ID%>"/>
					<%
				}
				%>
				<form:inputCell name="<%=Person.RESTRICTED_USER%>"
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