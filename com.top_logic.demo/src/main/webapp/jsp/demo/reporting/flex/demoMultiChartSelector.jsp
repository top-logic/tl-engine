<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="com.top_logic.demo.chart.flex.ChartSelector"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<form:select name="<%=ChartSelector.CHARTS_FIELD%>"
				style="height: 100%; position: absolute; top: 0px; left: 0px; box-sizing: border-box;"
				width="100%"
			/>
		</form:form>
	</layout:body>
</layout:html>