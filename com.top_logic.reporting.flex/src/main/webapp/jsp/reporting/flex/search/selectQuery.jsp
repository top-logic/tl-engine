<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.reporting.flex.search.SearchResultStoredReportSelector"
%><%@page import="com.top_logic.element.layout.meta.search.SearchFieldSupport"
%><%@page import="com.top_logic.element.layout.meta.search.QueryUtils"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body formBodyClass="">
		<form:form ignoreModel="true">
			<table>
				<colgroup>
					<col width="auto"/>
					<col width="auto"/>
					<col width="10px"/>
					<col width="auto"/>
					<col width="auto"/>
					<col width="10px"/>
					<col width="auto"/>
					<col width="auto"/>
					<col width="10px"/>
					<col width="auto"/>
					<col width="auto"/>
				</colgroup>
				<tr>
					<td class="label"
						style="text-align:left;"
					>
						<form:label name="<%= SearchFieldSupport.STORED_QUERY %>"
							colon="true"
						/>
					</td>
					<td class="content">
						<form:select name="<%= SearchFieldSupport.STORED_QUERY %>"
							width="245px"
						/>
					</td>
					<td>
					</td>
					<td class="label"
						style="text-align:left;"
					>
						<form:label name="<%= SearchResultStoredReportSelector.REPORT_SELECTION_FIELD %>"
							colon="true"
						/>
					</td>
					<td class="content">
						<form:select name="<%= SearchResultStoredReportSelector.REPORT_SELECTION_FIELD %>"
							width="245px"
						/>
					</td>
					<td>
					</td>
					<form:scope name="<%= QueryUtils.FORM_GROUP %>">
						<td class="label">
							<form:label name="<%= QueryUtils.PUBLISH_QUERY_FIELD %>"
								colon="true"
							/>
						</td>
						<td class="content">
							<form:checkbox name="<%= QueryUtils.PUBLISH_QUERY_FIELD %>"/>
						</td>
						<td>
						</td>
						<td class="label">
							<form:label name="<%= QueryUtils.VISIBLE_GROUPS_FIELD %>"
								colon="true"
							/>
						</td>
						<td class="content">
							<form:popup name="<%= QueryUtils.VISIBLE_GROUPS_FIELD %>"/>
						</td>
					</form:scope>
				</tr>
			</table>
		</form:form>
	</layout:body>
</layout:html>