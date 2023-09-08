<form:scope name="<%= com.top_logic.element.layout.meta.search.QueryUtils.FORM_GROUP %>">
	<form:inputCell name="<%= com.top_logic.element.layout.meta.search.QueryUtils.PUBLISH_QUERY_FIELD %>"/>
	<form:descriptionCell>
		<form:description>
			<form:label name="<%= com.top_logic.element.layout.meta.search.QueryUtils.VISIBLE_GROUPS_FIELD %>"/>
		</form:description>
		<form:popup name="<%= com.top_logic.element.layout.meta.search.QueryUtils.VISIBLE_GROUPS_FIELD %>"/>
	</form:descriptionCell>
</form:scope>