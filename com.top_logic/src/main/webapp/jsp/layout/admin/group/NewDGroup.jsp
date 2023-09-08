<%@page import="java.util.Locale"
%><%@page import="com.top_logic.basic.util.ResourcesModule"
%><%@page import="com.top_logic.model.util.TLModelUtil"
%><%@page import="com.top_logic.tool.boundsec.wrap.Group"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="com.top_logic.util.Resources"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.mig.html.layout.LayoutComponent"
%><%@page import="com.top_logic.layout.admin.component.EditGroupComponent"
%><%@page import="com.top_logic.layout.admin.component.EditGroupComponent.NewGroupComponent"
%><%
NewGroupComponent theComponent  = (NewGroupComponent) MainLayout.getComponent(pageContext);
LayoutComponent theParent = theComponent.getDialogParent();
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage
			titleField="<%=EditGroupComponent.FORM_FIELD_NAME %>"
			type="<%= Group.getGroupType() %>"
		>
			<form:inputCell name="<%=EditGroupComponent.FORM_FIELD_SYSTEM %>"
				colon="true"
			/>
			<%
			for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
				String theNameFiled = EditGroupComponent.getLanguageFieldName(locale);
				%>
				<form:inputCell name="<%=theNameFiled %>"
					colon="true"
				/>
				<%
			}
			%>
		</form:formPage>
	</layout:body>
</layout:html>