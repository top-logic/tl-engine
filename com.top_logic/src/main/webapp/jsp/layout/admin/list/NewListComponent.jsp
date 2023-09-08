<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.knowledge.wrap.list.FastList"
%><%@page import="com.top_logic.layout.admin.component.EditListComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage
			subtitleKeySuffix="message"
			titleField="<%=FastList.NAME_ATTRIBUTE%>"
			type="<%= FastList.getFastListType() %>"
		>
			<form:inputCell name="<%=FastList.DESC_ATTRIBUTE%>"
				colon="true"
			/>
			<form:inputCell name="<%=EditListComponent.FIELD_CLASSIFICATION_TYPE%>"
				colon="true"
			/>
			<form:inputCell name="<%=EditListComponent.MULTIPLE_FILED_NAME%>"
				colon="true"
			/>
		</form:formPage>
	</layout:body>
</layout:html>