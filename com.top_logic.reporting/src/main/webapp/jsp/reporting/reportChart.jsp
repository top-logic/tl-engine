<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.reporting.layout.flexreporting.component.ChartConfigurationComponent"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%
ChartConfigurationComponent theComp = (ChartConfigurationComponent)MainLayout.getComponent(pageContext);
%><%@page import="com.top_logic.reporting.layout.flexreporting.component.ChartConfigurationControlProvider"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<form:custom name="ReportConfiguration"
				controlProvider="<%=new ChartConfigurationControlProvider(theComp.getResPrefix()) %>"
			/>
		</form:form>
	</layout:body>
</layout:html>