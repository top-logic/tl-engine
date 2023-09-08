<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<basic:text i18n="<%=com.top_logic.util.maintenance.I18NConstants.NO_MAINTENANCE_PAGE_SELECTED.getKey() %>"/>
	</layout:body>
</layout:html>