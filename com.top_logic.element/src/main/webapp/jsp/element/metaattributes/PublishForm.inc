<%@page import="com.top_logic.element.layout.meta.search.QueryUtils"%>
<form:group name="<%= QueryUtils.FORM_GROUP %>">
	<table>
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
	</table>
</form:group>