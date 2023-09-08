<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.reporting.report.util.ReportConstants"
%><%@page import="com.top_logic.element.layout.meta.search.SearchFieldSupport"
%><%@page import="com.top_logic.reporting.layout.flexreporting.component.ReportSelectorComponent"
%><%@page import="com.top_logic.element.layout.meta.search.QueryUtils"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%
ReportSelectorComponent theComp = (ReportSelectorComponent)MainLayout.getComponent(pageContext);
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<table>
				<tr>
					<td class="label"
						style="text-align:left;"
					>
						<form:label name="<%= ReportConstants.REPORT_SELECTION_FIELD %>"
							colon="true"
						/>
					</td>
					<td class="content">
						<form:select name="<%= ReportConstants.REPORT_SELECTION_FIELD %>"
							width="245px"
						/>
					</td>
					<%@include file="/jsp/element/metaattributes/PublishForm_inline.inc" %>
				</tr>
			</table>
		</form:form>
	</layout:body>
</layout:html>