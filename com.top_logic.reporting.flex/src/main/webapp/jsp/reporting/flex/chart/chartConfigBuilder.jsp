<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.reporting.flex.chart.component.ChartConfigComponent"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:filterForm>
			<form:custom name="<%=ChartConfigComponent.CHART_FIELD%>"/>
		</form:filterForm>
	</layout:body>
</layout:html>