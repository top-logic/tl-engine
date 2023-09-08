<%@page import="com.top_logic.layout.compare.DateCompareSelector"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		<style type="text/css">
			table.frm {
				width: 0px;
			}
		</style>
	</layout:head>
	<layout:body>
		<form:form ignoreModel="true">
			<form:horizontal>
				<form:inputCell name="<%=DateCompareSelector.VERSION_FIELD  %>"/>
				<form:inputCell name="<%=DateCompareSelector.COMPARE_VERSION_FIELD %>"/>
			</form:horizontal>
		</form:form>
	</layout:body>
</layout:html>