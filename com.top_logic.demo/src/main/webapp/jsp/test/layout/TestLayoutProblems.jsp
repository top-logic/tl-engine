<%@page import="com.top_logic.layout.form.template.BeaconFormFieldControlProvider"
%><%@page import="com.top_logic.layout.IdentityAccessor"
%><%@page import="com.top_logic.demo.layout.form.demo.TestLayoutProblems"
%><%@page import="com.top_logic.layout.form.control.IconSelectControl"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="com.top_logic.layout.form.control.LinkButtonRenderer"
%><%@page import="com.top_logic.layout.form.control.DefaultButtonRenderer"
%><%@page import="com.top_logic.layout.form.control.ImageButtonRenderer"
%><%@page import="com.top_logic.layout.form.control.ButtonRenderer"
%><layout:html>
	<layout:head>
		<style type="text/css">
			table.border, table.border td, table.border th {
				border:1px black solid;
			}
			
			.ticket3109 {
				background-color: green;
			}
		</style>
	</layout:head>
	<layout:body>
		<h1>
			Test Layout Problems
		</h1>

		<form:form>
			<form:group name="Ticket_16921">
				<form:forEach member="inner">
					<form:group name="${inner}">
						<form:inputCell name="input"/>
					</form:group>
				</form:forEach>
			</form:group>

			<form:group name="Ticket_12066">
				<form:cell>
					<form:resource key="message1"/>
				</form:cell>
				<form:group name="toggleGroup">
					<form:label name="input"/>
					<form:input name="input"/>
				</form:group>
				<form:group name="nonToggleGroup">
					<form:label name="input"/>
					<form:input name="input"/>
				</form:group>
				<form:cell>
					<form:command name="toggleButton"
						renderer="<%= DefaultButtonRenderer.INSTANCE%>"
					/>
					<form:command name="updateButton"
						renderer="<%= DefaultButtonRenderer.INSTANCE%>"
					/>
				</form:cell>
			</form:group>

			<form:group name="Ticket_6828">
				<div>
					<form:resource key="message1"/>
				</div>
				<div>
					<form:select name="selectWithConstraint1"/>
					<form:error name="selectWithConstraint1"/>
					<form:popup name="selectWithConstraint2"/>
					<form:error name="selectWithConstraint2"/>
				</div>
				<div>
					<form:resource key="message2"/>
				</div>
				<div>
					<form:popup name="selectWithConstraint3"/>
					<form:error name="selectWithConstraint3"/>
				</div>
			</form:group>

			<form:group name="Ticket_4829">
				<div>
					<form:resource key="message"/>
				</div>
				<div>
					<form:custom name="fieldWithLengthConstraint"/>
					<form:error name="fieldWithLengthConstraint"/>
				</div>
			</form:group>

			<form:group name="Ticket_5310">
				<div>
					<form:command name="command"/>
				</div>
				<div>
					<form:custom name="tree"/>
				</div>
			</form:group>

			<form:group name="Ticket_2823">
				<table>
					<tr>
						<td>
							<form:command name="setOptions"/>
							<br/>
							<form:command name="setValue"/>
						</td>
					</tr>
					<tr>
						<td>
							<form:table name="select"
								accessor="<%= IdentityAccessor.INSTANCE %>"
								columnNames="col1"
								rowMove="true"
							/>
						</td>
					</tr>
				</table>
			</form:group>

			<form:group name="Ticket_4594">
				<table>
					<tr>
						<td>
							<form:select name="select"/>
						</td>
					</tr>
					<tr>
						<td>
							<form:popup name="selectPopup"/>
						</td>
					</tr>
					<tr>
						<td>
							<form:input name="textInput"/>
						</td>
					</tr>
					<tr>
						<td>
							<form:integer name="integerInput"/>
						</td>
					</tr>
					<tr>
						<td>
							<form:option name="selectOption"
								option="a"
							/>
						</td>
					</tr>
					<tr>
						<td>
							<form:choice name="choice"/>
						</td>
					</tr>
					<tr>
						<td>
							<form:beacon name="beacon"
								type="<%=BeaconFormFieldControlProvider.VAL_TYPE_BEACON%>"
							/>
						</td>
					</tr>
					<tr>
						<td>
							<form:checkbox name="checkbox"/>
						</td>
					</tr>
					<tr>
						<td>
							<form:checkbox name="booleanChoice"
								yesNo="true"
							/>
						</td>
					</tr>
					<tr>
						<td>
							<form:custom name="imageSelect"
								controlProvider="<%= IconSelectControl.Provider.INSTANCE %>"
							/>
						</td>
					</tr>
				</table>
			</form:group>

			<form:group name="Ticket_4175">
				<form:popup name="largeContent"/>
			</form:group>

			<form:group name="Ticket_4213">
				<form:input name="time"/>
				<form:error name="time"/>
				<form:command name="activateForward"/>
			</form:group>

			<form:group name="Ticket_2602">
				<form:command name="disabledCommand"
					renderer="<%= ButtonRenderer.newButtonRenderer(false) %>"
				/>
				<form:select name="reasons"/>
			</form:group>

			<form:group name="Ticket_3725">
				<form:group name="innerCTX1">
					<form:label name="dataField"/>
					<form:dataItem name="dataField"/>
				</form:group>
				<form:group name="innerCTX2">
					<form:label name="dataField"/>
					<form:dataItem name="dataField"/>
				</form:group>
			</form:group>

			<form:group name="Ticket_3687">
				<table>
					<tr>
						<td>
							<form:label name="disabledField"/>
							:
						</td>
						<td>
							<form:select name="disabledField"/>
						</td>
					</tr>
					<tr>
						<td>
							<form:label name="changingField"/>
							:
						</td>
						<td>
							<form:popup name="changingField"/>
						</td>
					</tr>
				</table>
			</form:group>

			<form:group name="Ticket_2372">
				<table>
					<tr>
						<td>
							<form:label name="executeTwiceImage"/>
							:
						</td>
						<td>
							<form:command name="executeTwiceImage"
								renderer="<%= ImageButtonRenderer.INSTANCE%>"
							/>
						</td>
					</tr>
					<tr>
						<td>
							<form:label name="executeTwiceLink"/>
							:
						</td>
						<td>
							<form:command name="executeTwiceLink"
								renderer="<%= LinkButtonRenderer.INSTANCE%>"
							/>
						</td>
					</tr>
					<tr>
						<td>
							<form:label name="executeTwiceButtonImage"/>
							:
						</td>
						<td>
							<form:command name="executeTwiceButtonImage"
								renderer="<%= ButtonRenderer.INSTANCE%>"
							/>
						</td>
					</tr>
					<tr>
						<td>
							<form:label name="executeTwiceButtonLink"/>
							:
						</td>
						<td>
							<form:command name="executeTwiceButtonLink"
								renderer="<%= ButtonRenderer.INSTANCE%>"
							/>
						</td>
					</tr>
				</table>
				<form:command name="toggle"
					renderer="<%= DefaultButtonRenderer.INSTANCE%>"
				/>
			</form:group>

			<form:group name="Ticket_2437">
				<form:input name="executedCount"/>
				<form:command name="increase"/>
			</form:group>

			<form:group name="Ticket_2470">
				<form:tableview name="table"/>
				<form:command name="increase"/>
			</form:group>

			<form:group name="Ticket_2781">
				<table>
					<tr>
						<td>
							<form:label name="noApply"/>
						</td>
						<td>
							<form:checkbox name="noApply"/>
						</td>
					</tr>
					<tr>
						<td>
							<form:label name="applyTemporarily"/>
						</td>
						<td>
							<form:checkbox name="applyTemporarily"/>
						</td>
					</tr>
					<tr>
						<td>
							<form:label name="applyWithErrors"/>
						</td>
						<td>
							<form:checkbox name="applyWithErrors"/>
						</td>
					</tr>
					<tr>
						<td>
							<form:label name="noDiscard"/>
						</td>
						<td>
							<form:checkbox name="noDiscard"/>
						</td>
					</tr>
					<tr>
						<td>
							<form:label name="discardWithErrors"/>
						</td>
						<td>
							<form:checkbox name="discardWithErrors"/>
						</td>
					</tr>
				</table>
				<form:input name="number"/>
				<form:command name="increase"/>
			</form:group>

			<form:group name="Ticket_2850">
				<div>
					<ul>
						<li>
							<form:command name="toggleTableVisibility"/>
						</li>
						<li>
							<form:command name="toggleTheadVisibility"/>
						</li>
						<li>
							<form:command name="toggleThVisibility"/>
						</li>
						<li>
							<form:command name="toggleTbodyVisibility"/>
						</li>
						<li>
							<form:command name="toggleTrVisibility"/>
						</li>
						<li>
							<form:command name="toggleTdVisibility"/>
						</li>
					</ul>
				</div>
				<form:custom name="table"/>
			</form:group>

			<form:group name="Ticket_3109">
				<div>
					<form:input name="emptyInput"/>
				</div>
				<form:command name="toggleImmutable"/>
			</form:group>

			<form:group name="Ticket_3132">
				<div>
					<form:label name="nameField"/>
					:
					<form:input name="nameField"/>
				</div>
				<div>
					<form:label name="changedField"/>
					:
					<form:checkbox name="changedField"/>
				</div>
				<div>
					<form:command name="removeCommand"/>
					&#xA0;
					<form:command name="addCommand"/>
				</div>
				<form:custom name="changedMembersContainer"/>
				<div>
					<form:label name="changedMembers"/>
					:
					<form:input name="changedMembers"/>
				</div>
			</form:group>

			<form:group name="Ticket_3438">
				<div>
					<form:label name="style"/>
					:
					<form:input name="style"/>
				</div>
				<div>
					<form:label name="text"/>
					:
					<form:input name="text"/>
				</div>
				<div>
					<form:label name="select"/>
					:
					<form:select name="select"/>
				</div>
			</form:group>

			<form:group name="Ticket_17893">
				<div>
					<form:resource key="message"/>
				</div>
				<div>
					<form:label name="source"/>
					:
					<form:input name="source"
						columns="100"
						multiLine="true"
						rows="5"
					/>
				</div>
				<div>
					<form:label name="target"/>
					:
					<form:input name="target"
						columns="100"
						multiLine="true"
						rows="5"
					/>
				</div>
			</form:group>

			<form:group name="Ticket_18991">
				<form:resource key="message"/>
				<form:table name="polymorphicField"
					columnNames="type value"
				/>
			</form:group>

			<form:group name="Ticket_18271">
				<div>
					<form:label name="valueAsTooltip"/>
				</div>
				<div>
					<form:input name="valueAsTooltip"
						columns="80"
						multiLine="true"
						rows="10"
					/>
					<form:custom name="tooltipField"/>
				</div>
				<div>
					<form:label name="valueAsError"/>
				</div>
				<div>
					<form:input name="valueAsError"
						columns="80"
						multiLine="true"
						rows="10"
					/>
					<form:error name="valueAsError"/>
				</div>
				<div>
					<form:label name="explicitError"/>
				</div>
				<div>
					<form:input name="explicitError"
						columns="80"
						multiLine="true"
						rows="10"
					/>
					<form:error name="explicitError"/>
				</div>
				<div>
					<form:command name="setValueAsError"/>
				</div>
			</form:group>

			<form:group name="Ticket_18938">
				<form:resource key="message"/>
				<div>
					<form:select name="vetoField"/>
				</div>
			</form:group>

			<form:group name="Ticket_19467">
				<form:resource key="blockingMessage"/>
				<div>
					<form:label name="blockingField"
						colon="true"
					/>
					<form:input name="blockingField"/>
				</div>
				<form:resource key="nonBlockingMessage"/>
				<div>
					<form:label name="nonBlockingField"
						colon="true"
					/>
					<form:input name="nonBlockingField"/>
				</div>
			</form:group>

			<form:group name="Ticket_24173">
				<form:resource key="message"/>
				<form:command name="renewControlField"/>
			</form:group>
			<form:group name="Ticket_25739">
				<form:resource key="message"/>
				<form:inputCell name="waitingTime"/>
				<form:inputCell name="table"/>
			</form:group>
		</form:form>
	</layout:body>
</layout:html>