<%@page import="com.top_logic.basic.util.ResourcesModule"
%><%@page import="java.util.Locale"
%><%@page import="com.top_logic.model.util.TLModelUtil"
%><%@page import="com.top_logic.model.util.TLModelNamingConvention"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.admin.component.EditGroupComponent"
%><%@page import="com.top_logic.knowledge.wrap.person.PersonAccessor"
%><%@page import="com.top_logic.tool.boundsec.wrap.Group"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage
			titleField="<%=EditGroupComponent.FORM_FIELD_NAME %>"
			type="<%= Group.getGroupType() %>"
		>
			<form:groupCell titleKeySuffix="basic">
				<form:cell wholeLine="true">
					<form:inputCell name="<%=EditGroupComponent.FORM_FIELD_SYSTEM %>"
						firstColumnWidth="13em"
					/>
				</form:cell>
				<%
				for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
					String theNameFiled = EditGroupComponent.getLanguageFieldName(locale);
					String theDescFiled = EditGroupComponent.getDescriptionFieldName(locale);
					%>
					<form:descriptionContainer>
						<form:inputCell name="<%=theNameFiled %>"
							firstColumnWidth="13em"
						/>
						<form:inputCell name="<%=theDescFiled %>"
							firstColumnWidth="13em"
						/>
					</form:descriptionContainer>
					<%
				}
				%>
			</form:groupCell>

			<form:groupCell titleKeySuffix="user">
				<form:cell wholeLine="true">
					<form:table name="<%=EditGroupComponent.FORM_FIELD_MEMBERS %>"
						columnNames="firstName lastName name"
						initialSortColumn="1"
					/>
					<form:error name="members"/>
				</form:cell>
			</form:groupCell>
		</form:formPage>
	</layout:body>
</layout:html>