<%@page import="com.top_logic.basic.util.ResourcesModule"
%><%@page import="com.top_logic.knowledge.wrap.list.FastList"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.admin.component.EditListComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%
EditListComponent theComponent = (EditListComponent) MainLayout.getComponent(pageContext);
String noModelSuffix = theComponent.isSingleListMode() ? "error" : "noModel";
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage
			noModelKeySuffix="<%=noModelSuffix %>"
			type="<%= FastList.getFastListType() %>"
		>
			<form:columns count="2">
				<form:inputCell name="<%=EditListComponent.FIELD_LIST_NAME %>"/>
				<form:inputCell name="<%=EditListComponent.FIELD_LIST_MULTI %>"/>
				<form:inputCell name="<%=EditListComponent.FIELD_LIST_DESCRIPTION %>"/>
				<form:inputCell name="<%=EditListComponent.FIELD_LIST_ORDERED %>"/>
				<form:separator/>
			</form:columns>

			<form:columns count="1">
				<%
				for (String localeName : ResourcesModule.getInstance().getSupportedLocaleNames()) {
					String theName = EditListComponent.FIELD_PREFIX + EditListComponent.FIELD_I18N_PREFIX + localeName;
					%>
					<form:inputCell name="<%=theName %>"
						firstColumnWidth="15em"
					/>
					<%
				}
				%>
			</form:columns>

			<form:tablelist name="<%=EditListComponent.FIELD_TABLELIST%>"
				rowObjectCreator="<%=theComponent%>"
				rowObjectRemover="<%=theComponent%>"
			/>
			<form:error name="<%=EditListComponent.FIELD_TABLELIST%>"/>
			<br/>
		</form:formPage>
	</layout:body>
</layout:html>