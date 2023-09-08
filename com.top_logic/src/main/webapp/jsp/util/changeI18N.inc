<%--
Adds name i18n fields

Required:
import:
variables:
--%>
<form:group name="<%= com.top_logic.layout.unit.EditUnitComponent.TRANSLATIONS_GROUP_NAME %>">
	<table class="translationsTable">
		<form:forEach member="group">
			<form:scope name="${group}">
				<form:forEach member="field">
					<tr>
						<td class="label">
							<form:label name="${field}"
								colon="true"
							/>
						</td>
						<td class="content">
							<form:input name="${field}"/>
						</td>
						<td class="error">
							<form:error name="${field}"
								icon="true"
							/>
						</td>
					</tr>
				</form:forEach>
			</form:scope>
		</form:forEach>
	</table>
</form:group>