<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.element.layout.meta.search.QueryUtils"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform"   prefix="form"
%><layout:html>
	<layout:head>
	</layout:head>
	<layout:body>
		<form:form>
			<table class="frm">
				<tr>
					<th colspan="2">
						<form:resource key="title"/>
					</th>
				</tr>
				<tr>
					<td colspan="2">
						&#xA0;
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<form:resource key="message"/>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						&#xA0;
					</td>
				</tr>
				<tr>
					<td class="label">
						<form:label name="name"/>
						:
					</td>
					<td class="content">
						<form:input name="name"
							columns="30"
						/>
						<form:error name="name"/>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						&#xA0;
					</td>
				</tr>
				<form:scope name="<%= QueryUtils.FORM_GROUP %>">
					<tr>
						<td class="label">
							<form:label name="<%= QueryUtils.PUBLISH_QUERY_FIELD %>"/>
						</td>
						<td class="content">
							<form:checkbox name="<%= QueryUtils.PUBLISH_QUERY_FIELD %>"/>
						</td>
					</tr>
					<tr>
						<td class="label">
							<form:label name="<%= QueryUtils.VISIBLE_GROUPS_FIELD %>"/>
						</td>
						<td class="content">
							<form:popup name="<%= QueryUtils.VISIBLE_GROUPS_FIELD %>"
								style="width:175px;"
							/>
							<form:error name="<%= QueryUtils.VISIBLE_GROUPS_FIELD %>"
								icon="true"
							/>
						</td>
					</tr>
				</form:scope>
			</table>
		</form:form>
	</layout:body>
</layout:html>