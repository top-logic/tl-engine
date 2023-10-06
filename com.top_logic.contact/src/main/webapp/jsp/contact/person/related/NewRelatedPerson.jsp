<%@page import="com.top_logic.model.util.TLModelUtil"
%><%@page import="com.top_logic.knowledge.wrap.person.Person"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.base.security.attributes.PersonAttributes,
java.util.*,
com.top_logic.contact.layout.person.related.NewRelatedPersonComponent,
com.top_logic.base.security.device.TLSecurityDeviceManager"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:formPage
			titleKeySuffix="title"
			type="<%= Person.getPersonType() %>"
		>
			<!-- summary="New Person Form" -->
			<form:inputCell name="<%=PersonAttributes.USER_NAME%>"
				colon="true"
			/>
			<form:inputCell name="<%=PersonAttributes.GIVEN_NAME%>"
				colon="true"
			/>
			<form:inputCell name="<%=PersonAttributes.SUR_NAME%>"
				colon="true"
			/>
			<form:inputCell name="<%=PersonAttributes.TITLE%>"
				colon="true"
			/>
			<form:inputCell name="<%=PersonAttributes.RESTRICTED_USER%>"
				colon="true"
			/>

			<form:ifExists name="<%= NewRelatedPersonComponent.FIELD_NAME_RELATED_CONTACT %>">
				<form:descriptionCell>
					<form:description>
						<form:label name="<%= NewRelatedPersonComponent.FIELD_NAME_RELATED_CONTACT %>"
							colon="true"
						/>
						&#xA0;
						<form:error name="<%= NewRelatedPersonComponent.FIELD_NAME_RELATED_CONTACT %>"
							icon="true"
						/>
					</form:description>
					<form:popup name="<%= NewRelatedPersonComponent.FIELD_NAME_RELATED_CONTACT %>"/>
				</form:descriptionCell>
			</form:ifExists>
		</form:formPage>
	</layout:body>
</layout:html>