<%@page import="com.top_logic.basic.util.ResKey"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.event.layout.LogEntryFilterComponent"
%><%@page import="com.top_logic.util.Resources"
%><%@page import="com.top_logic.gui.ThemeFactory"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%
Resources theRes = Resources.getInstance();
String theLabel = theRes.getString(ResKey.legacy("tl.command.refresh"));
String theImagePath = "theme:MISC_RELOAD_SMALL";
%><layout:html>
	<layout:head>
		<style>
			.no-error {
				width: 0;
				height: 0;
			}
			.leftField {
				margin-right: 0.5em
			}
			
			.rightField {
				margin-left: 0.5em
			}
		</style>
	</layout:head>
	<layout:body>
		<form:form>
			<div class="filterGroupOuterDiv"
				style="text-align: center;"
			>
				<table style="display: inline-table">
					<tr>
						<td>
							<span class="leftField">
								<form:date name="<%= LogEntryFilterComponent.START_DATE %>"/>
								<form:error name="<%= LogEntryFilterComponent.START_DATE %>"
									icon="true"
								/>
							</span>
						</td>
						<td valign="middle">
							-
						</td>
						<td>
							<span class="rightField">
								<form:date name="<%= LogEntryFilterComponent.END_DATE %>"/>
								<form:error name="<%= LogEntryFilterComponent.END_DATE %>"
									icon="true"
								/>
							</span>
						</td>
						<td>
							<form:button
								command="<%= LogEntryFilterComponent.UpdateLogEntriesCommandHandler.COMMAND_ID %>"
								disabledImage="<%= theImagePath %>"
								image="<%= theImagePath %>"
								reskey="tl.command.refresh"
							/>
						</td>
					</tr>
				</table>
			</div>
		</form:form>
	</layout:body>
</layout:html>