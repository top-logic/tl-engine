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
				style="margin-bottom: 10px;"
				width="100%"
			/>

			<form:ifExists name="<%=ChartSelector.CONTENT_FIELD%>">
				<form:input name="<%=ChartSelector.CONTENT_FIELD%>"
					columns="30"
					multiLine="true"
					rows="30"
				/>
			</form:ifExists>
		</form:form>
	</layout:body>
</layout:html>