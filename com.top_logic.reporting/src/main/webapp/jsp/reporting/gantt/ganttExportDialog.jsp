<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.reporting.chart.gantt.component.GanttExportDialog"
%><%@taglib uri="layout"     prefix="layout"
%><%@taglib uri="ajaxform"   prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage
			ignoreModel="true"
			image="theme:ICONS_DOWNLOAD60"
			titleKeySuffix="chooseFormat"
		>
			<form:vertical>
				<form:inputCell name="<%=GanttExportDialog.SCALING_TYPE%>"/>
				<form:inputCell name="<%=GanttExportDialog.CHOOSE_FORMAT_FIELD%>"/>
			</form:vertical>
		</form:formPage>
	</layout:body>
</layout:html>