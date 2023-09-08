<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="com.top_logic.reporting.flex.chart.component.MultipleChartComponent"
%><layout:html>
	<layout:head>
		<style type="text/css">
			body {
				padding: 0;
			}
			
			.multiChartContainer {
				position: absolute;
				width: 100%;
				height: 100%
			}
			
			.multiChartRow {
				position: relative;
				height: 50%;
			}
			
			.multiChartColumn {
				position: relative;
				display: inline-block;
				height: 100%;
				width: 50%;
			}
		</style>
		
	</layout:head>
	<layout:body>
		<form:form>
			<form:custom name="<%=MultipleChartComponent.CHARTS%>"/>
		</form:form>
	</layout:body>
</layout:html>