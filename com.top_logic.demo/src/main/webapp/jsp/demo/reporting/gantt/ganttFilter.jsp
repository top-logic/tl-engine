<%@page import="com.top_logic.layout.structure.OrientationAware.Orientation"
%><%@page import="com.top_logic.layout.form.template.SelectionControlProvider"
%><%@page import="com.top_logic.reporting.report.util.ReportUtilities"
%><%@page import="com.top_logic.layout.form.control.ImageButtonRenderer"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.reporting.chart.gantt.component.GanttChartFilterComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:filterForm>
			<form:groupCell
				border="false"
				titleKeySuffix="filterLegend"
			>
				<form:cell>
					<form:vertical>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_START_DATE%>"/>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_END_DATE%>"/>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_DEPTH%>"
							controlProvider="<%=com.top_logic.layout.form.control.IntegerInputControl.Provider.INSTANCE %>"
						/>
					</form:vertical>
				</form:cell>

				<form:cell>
					<form:vertical>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_SHOW_ROOT%>"/>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_SHOW_PARENT_ELEMENTS%>"/>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_SHOW_FORECAST%>"/>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_SHOW_REPORT_LINES%>"/>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_SHOW_DEPENDENCIES%>"/>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_DISABLE_FINISHED_ELEMENTS%>"/>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_HIDE_FINISHED_ELEMENTS%>"/>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_HIDE_NODE_DATE_RANGES%>"/>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_HIDE_START_END_LABEL%>"/>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_ADD_COLLISION_AVOIDING_ROWS%>"/>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_SHOW_ADDITIONAL_COLUMNS%>"/>
					</form:vertical>
				</form:cell>

				<form:cell>
					<form:vertical>
						<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_ADDITIONAL_COLUMNS%>"
							controlProvider="<%=SelectionControlProvider.SELECTION_INSTANCE %>"
						/>
					</form:vertical>
				</form:cell>
			</form:groupCell>

			<form:groupCell
				border="false"
				titleKeySuffix="scalingLegend"
			>
				<form:vertical>
					<form:cell>
						<form:choice name="<%=GanttChartFilterComponent.PROPERTY_SCALING_OPTION%>"
							labelsLeft="true"
							orientation="<%=Orientation.VERTICAL %>"
						/>
					</form:cell>

					<form:inputCell name="<%=GanttChartFilterComponent.PROPERTY_SCALING_GRANULARITY%>"/>
				</form:vertical>
			</form:groupCell>
		</form:filterForm>
	</layout:body>
</layout:html>