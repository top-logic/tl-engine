<%@page import="com.top_logic.basic.util.ResKey"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.event.layout.ConfigureLogEntriesComponent"
%><%@page import="java.util.Iterator"
%><%@page import="com.top_logic.event.layout.ToggleCommand"
%><%@page import="com.top_logic.layout.form.control.DefaultButtonRenderer"

%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="util"   prefix="tl"
%><%@taglib uri="ajaxform" prefix="form"
%><%
ConfigureLogEntriesComponent theComponent = (ConfigureLogEntriesComponent) MainLayout.getComponent(pageContext);
LogEntryConfiguration theConfiguration    = LogEntryConfiguration.getInstance();
Iterator groups                           = theComponent.getAllowedDisplayGroups().iterator();
%><%@page import="com.top_logic.event.logEntry.LogEntryConfiguration"
%><%@page import="com.top_logic.event.logEntry.LogEntryDisplayGroup"
%><%@page import="com.top_logic.event.layout.LogEntryFilterComponent"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<table
				border="1"
				width="100%"
			>
				<%
				int x = 1;
				while(groups.hasNext()){
					LogEntryDisplayGroup theGroup = (LogEntryDisplayGroup) groups.next();
					String theGroupName           = theGroup.getName();
					x=1-x;
					if(x<1){
						String colspan="1";
						if(!groups.hasNext()){
							colspan="2";
						}
						
						%>
						<tr>
							<td colspan="<%=colspan %>">
								<%
							}else{
								%>
							</td>
							<td>
								<%
							}
							%>
							<table width="100%">
								<form:scope name="<%= theGroupName %>">
									<tr>
										<th>
											<tl:label nameConst="<%= theGroup.getResourceKey() %>"/>
										</th>
										<td>
											<form:command name="<%= theGroupName + ToggleCommand.ON %>"
												renderer="<%=DefaultButtonRenderer.INSTANCE%>"
											/>
											<form:command name="<%= theGroupName + ToggleCommand.OFF %>"
												renderer="<%=DefaultButtonRenderer.INSTANCE%>"
											/>
										</td>
									</tr>
									<%
									Iterator elements = theGroup.getEventTypes().iterator();
									while(elements.hasNext()){
										String desc = (String) elements.next();
										ResKey theLabel = theGroup.getResourceKey(desc);
										String theKey   = theComponent.convertToFieldname(theGroup.getKey(desc));
										
										%>
										<tr>
											<td>
												<tl:label nameConst="<%= theLabel %>"/>
											</td>
											<td>
												<form:checkbox name="<%= theKey %>"/>
											</td>
										</tr>
										<%
									}
									%>
								</form:scope>
							</table>
						</td>
						<% if (x > 0 || !groups.hasNext()){%>
						</tr>
					<%} %>
					
					<%
				}
				%>
			</table>
		</form:form>
	</layout:body>
</layout:html>