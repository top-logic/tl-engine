<%@page import="com.top_logic.layout.structure.OrientationAware.Orientation"
%><%@page trimDirectiveWhitespaces="true"
%><%@page import="com.top_logic.layout.renderers.ButtonComponentButtonRenderer"
%><%@page import="com.top_logic.layout.form.control.ImageLinkButtonRenderer"
%><%@page import="com.top_logic.layout.form.template.BeaconFormFieldControlProvider"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="com.top_logic.layout.form.control.ChoiceControl"
%><%@page import="com.top_logic.layout.form.control.LinkButtonRenderer"
%><%@page import="com.top_logic.layout.form.control.DefaultButtonRenderer"
%><%@page import="com.top_logic.layout.form.control.ImageButtonRenderer"
%><%@page import="com.top_logic.layout.form.control.IconSelectControl"
%><layout:html>
	<layout:head>
		<style type="text/css">
			.greater input {
				background-color: green;
			}
			
			.smaller input {
				background-color: red;
			}
			
			td.demoImportantGroup {
				background-color: #FFDDDD;
			}
		</style>
	</layout:head>
	<layout:body>
		<h1>
			Form Control Test
		</h1>

		<form:form displayWithoutModel="true">
			<form:group name="controls"
				firstColumnWidth="21em"
			>
				<form:hr key="resources"/>

				<ul>
					<li>
						<form:resource key="labelResource"/>
					</li>
					<li>
						<form:resource reskey="demo.global.key.labelResource"/>
					</li>
					<li>
						<form:resource key="imageResource"
							image="theme:ICONS_ALERT"
						/>
					</li>
					<li>
						<form:popup name="mailLinkContent"/>
					</li>
				</ul>

				<form:hr key="integers"/>

				<form:descriptionCell>
					<form:description>
						<form:label name="intControl"/>
						<form:error name="intControl"/>
					</form:description>
					<form:integer name="intControl"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="intControl2"/>
						<form:error name="intControl2"/>
					</form:description>
					<form:integer name="intControl2"/>
				</form:descriptionCell>

				<form:columns count="2">
					<form:descriptionContainer>
						<form:inputCell name="double"/>
					</form:descriptionContainer>

					<form:descriptionContainer>
						<form:inputCell name="doubleFormat"/>
						<form:inputCell name="doubleParser"/>
						<form:inputCell name="doubleFormatAndParser"/>
					</form:descriptionContainer>
				</form:columns>

				<form:inputCell name="doubleValue"/>
				<form:inputCell name="long"/>
				<form:inputCell name="longValue"/>

				<form:hr key="checkboxes"
					info="true"
				/>

				<form:inputCell name="checkboxControl"/>
				<form:inputCell name="tristateControl"/>

				<form:descriptionCell>
					<form:description>
						<form:label name="imageSelect"/>
						<form:error name="imageSelect"/>
					</form:description>
					<form:custom name="imageSelect"
						controlProvider="<%= IconSelectControl.Provider.INSTANCE %>"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="imageSelectReset"/>
						<form:error name="imageSelectReset"/>
					</form:description>
					<form:custom name="imageSelectReset"
						controlProvider="<%= IconSelectControl.Provider.INSTANCE_RESETTABLE %>"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="booleanChoiceControl"/>
						<form:error name="booleanChoiceControl"/>
					</form:description>
					<form:checkbox name="booleanChoiceControl"
						yesNo="true"
					/>
				</form:descriptionCell>

				<form:hr key="textinput"
					image="theme:ICONS_ALERT"
				/>

				<form:inputCell name="textInputControl"/>

				<!-- Another view for the same field model. Both display the same value. -->
				<form:inputCell name="textInputControl"/>

				<form:inputCell name="textInputWithPlaceholder"/>
				<form:inputCell name="textInputWithContextMenu"/>

				<form:groupCell
					personalizationName="structuredText"
					titleText="Structured Text"
				>
					<form:custom name="structuredText"/>
					<form:error name="structuredText"/>
				</form:groupCell>

				<form:groupCell
					personalizationName="codeEditorGroup"
					titleText="XML Editor"
				>
					<form:custom name="codeEditor"/>
					<form:error name="codeEditor"/>
				</form:groupCell>

				<form:group name="i18n">
					<form:inputCell name="i18nString"/>
					<form:inputCell name="i18nStringMultiLine"/>
					<form:inputCell name="i18nHTML"/>
				</form:group>

				<form:descriptionCell>
					<form:description>
						<form:label name="popUpTextInputControl"/>
						<form:error name="popUpTextInputControl"/>
					</form:description>
					<form:custom name="popUpTextInputControl"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="expandableTextInputControl"/>
						<form:error name="expandableTextInputControl"/>
					</form:description>
					<form:custom name="expandableTextInputControl"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="blockTextInputControl"/>
						<form:error name="blockTextInputControl"/>
					</form:description>
					<form:textblock name="blockTextInputControl"/>
				</form:descriptionCell>

				<form:scope name="testLengthConstraint">
					<form:forEach member="f">
						<form:descriptionCell>
							<form:description>
								<form:label name="${f}"/>
								<form:error name="${f}"/>
							</form:description>
							<form:custom name="${f}"/>
						</form:descriptionCell>
					</form:forEach>
				</form:scope>

				<form:inputCell name="testForeachControl"/>

				<form:descriptionCell>
					<form:description>
						<form:label name="infoControl"/>
					</form:description>
					<form:input name="infoControl"/>
					<form:info name="infoControl"
						image="theme:ICONS_DIALOG_INFORMATION"
					/>
				</form:descriptionCell>

				<form:inputCell name="dateInputControl"/>
				<form:inputCell name="timeInputControl"/>

				<form:descriptionCell>
					<form:description>
						<form:label name="openCalendar"/>
					</form:description>
					<form:custom name="openCalendar"/>
					<form:label name="displayDate"
						colon="true"
					/>
					<form:custom name="displayDate"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="dateTimeField"/>
					</form:description>
					<form:custom name="dateTimeField"/>
					<form:error name="dateTimeField"/>
					<form:label name="dateTimeFieldValue"
						colon="true"
					/>
					<form:custom name="dateTimeFieldValue"/>
					<form:error name="dateTimeFieldValue"/>
				</form:descriptionCell>

				<div style="width: 300px; height: 100px; overflow: auto;">
					<form:inputCell name="textInputControl2"
						firstColumnWidth="10em"
					/>
					<form:inputCell name="textInputControl3"
						firstColumnWidth="10em"
					/>
					<form:inputCell name="textInputControl4"
						firstColumnWidth="10em"
					/>
					<form:inputCell name="textInputControl5"
						firstColumnWidth="10em"
					/>
					<form:inputCell name="textInputControl6"
						firstColumnWidth="10em"
					/>
					<form:inputCell name="textInputControl7"
						firstColumnWidth="10em"
					/>
					<form:inputCell name="dateInputControl2"
						firstColumnWidth="10em"
					/>
					<form:inputCell name="textInputControl8"
						firstColumnWidth="10em"
					/>

					<form:descriptionCell firstColumnWidth="10em">
						<form:description>
							<form:label name="popupControl"/>
							<form:error name="popupControl"/>
						</form:description>
						<form:custom name="popupControl"/>
					</form:descriptionCell>

					<form:inputCell name="textInputControl10"
						firstColumnWidth="10em"
					/>
					<form:inputCell name="textInputControl11"
						firstColumnWidth="10em"
					/>
					<form:inputCell name="textInputControl12"
						firstColumnWidth="10em"
					/>
				</div>

				<form:hr key="Buttons"/>

				<form:cell wholeLine="true">
					This section displays buttons in different flavours.
				</form:cell>

				<form:scope name="buttons">
					<form:descriptionCell>
						<form:description>
							<form:resource key="ButtonComponentButtonRenderer"/>
						</form:description>
						<form:command name="ButtonComponentButtonRenderer"
							renderer="<%= ButtonComponentButtonRenderer.INSTANCE %>"
						/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:resource key="DefaultButtonRenderer"/>
						</form:description>
						<form:command name="DefaultButtonRenderer"
							renderer="<%= DefaultButtonRenderer.INSTANCE %>"
						/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:resource key="DefaultButtonRendererNoReason"/>
						</form:description>
						<form:command name="DefaultButtonRendererNoReason"
							renderer="<%= DefaultButtonRenderer.NO_REASON_INSTANCE %>"
						/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:resource key="ImageButtonRenderer"/>
						</form:description>
						<form:command name="ImageButtonRenderer"
							renderer="<%= ImageButtonRenderer.INSTANCE %>"
						/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:resource key="ImageButtonRendererNoReason"/>
						</form:description>
						<form:command name="ImageButtonRendererNoReason"
							renderer="<%= ImageButtonRenderer.NO_REASON_INSTANCE %>"
						/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:resource key="ImageLinkButtonRenderer"/>
						</form:description>
						<form:command name="ImageLinkButtonRenderer"
							renderer="<%= ImageLinkButtonRenderer.INSTANCE %>"
						/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:resource key="ImageLinkButtonRendererNoReason"/>
						</form:description>
						<form:command name="ImageLinkButtonRendererNoReason"
							renderer="<%= ImageLinkButtonRenderer.NO_REASON_INSTANCE%>"
						/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:resource key="LinkButtonRenderer"/>
						</form:description>
						<form:command name="LinkButtonRenderer"
							renderer="<%= LinkButtonRenderer.INSTANCE %>"
						/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:resource key="LinkButtonRendererNoReason"/>
						</form:description>
						<form:command name="LinkButtonRendererNoReason"
							renderer="<%= LinkButtonRenderer.NO_REASON_INSTANCE %>"
						/>
					</form:descriptionCell>
				</form:scope>

				<form:hr key="checkboxes"
					info="true"
				/>

				<form:cell>
					<form:label name="testKeyEventListener"/>
				</form:cell>

				<form:scope name="testKeyEventListener">
					<form:forEach member="m">
						<form:descriptionCell>
							<form:description>
								<form:label name="${m}"/>
								<form:error name="${m}"/>
							</form:description>
							<form:custom name="${m}"/>
						</form:descriptionCell>
					</form:forEach>
				</form:scope>

				<form:descriptionCell>
					<form:description>
						<form:label name="colorChooserControl"/>
						<form:error name="colorChooserControl"/>
					</form:description>
					<form:custom name="colorChooserControl"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="iconChooserControl"/>
						<form:error name="iconChooserControl"/>
					</form:description>
					<form:custom name="iconChooserControl"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="listControl"/>
						<form:error name="listControl"/>
					</form:description>
					<form:custom name="listControl"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="beaconControl-selectField"/>
						<form:error name="beaconControl-selectField"/>
					</form:description>
					<form:beacon name="beaconControl-selectField"
						type="<%=BeaconFormFieldControlProvider.VAL_TYPE_BEACON%>"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="beaconControl-complexField"/>
						<form:error name="beaconControl-complexField"/>
					</form:description>
					<form:beacon name="beaconControl-complexField"
						type="<%=BeaconFormFieldControlProvider.VAL_TYPE_BEACON%>"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="choiceControl"/>
						<form:error name="choiceControl"/>
					</form:description>
					<form:choice name="choiceControl"
						orientation="<%=Orientation.HORIZONTAL %>"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="choiceControlMulti"/>
						<form:error name="choiceControlMulti"/>
					</form:description>
					<form:choice name="choiceControlMulti"
						orientation="<%=Orientation.HORIZONTAL %>"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="choiceControlVertical"/>
						<form:error name="choiceControlVertical"/>
					</form:description>
					<form:choice name="choiceControlVertical"
						orientation="<%=Orientation.VERTICAL %>"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="choiceControlMultiVertical"/>
						<form:error name="choiceControlMultiVertical"/>
					</form:description>
					<form:choice name="choiceControlMultiVertical"
						orientation="<%=Orientation.VERTICAL %>"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="selectControl"/>
						<form:error name="selectControl"/>
					</form:description>
					<form:select name="selectControl"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="selectControlOptionContextMenu"/>
						<form:error name="selectControlOptionContextMenu"/>
					</form:description>
					<form:select name="selectControlOptionContextMenu"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="selectControlAsList"/>
						<form:error name="selectControlAsList"/>
					</form:description>
					<form:select name="selectControlAsList"
						forceDisplayAsList="true"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="selectControlAsListMandatory"/>
						<form:error name="selectControlAsListMandatory"/>
					</form:description>
					<form:select name="selectControlAsListMandatory"
						forceDisplayAsList="true"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:label name="selectControlNotEmptyConstraint"/>
					<form:error name="selectControlNotEmptyConstraint"/>
					<form:description/>
					<form:select name="selectControlNotEmptyConstraint"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="selectControlEmptyOptionList"/>
						<form:error name="selectControlEmptyOptionList"/>
					</form:description>
					<form:select name="selectControlEmptyOptionList"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="fractionSelectControl"/>
						<form:error name="fractionSelectControl"/>
					</form:description>
					<form:custom name="fractionSelectControl"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="coloredSelectControl"/>
						<form:error name="coloredSelectControl"/>
					</form:description>
					<form:custom name="coloredSelectControl"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="selectControlMulti"/>
						<form:error name="selectControlMulti"/>
					</form:description>
					<form:select name="selectControlMulti"
						size="2"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="selectControlMultiMandatory"/>
						<form:error name="selectControlMultiMandatory"/>
					</form:description>
					<form:select name="selectControlMultiMandatory"
						size="2"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="booleanSelectControl"/>
						<form:error name="booleanSelectControl"/>
					</form:description>
					<form:custom name="booleanSelectControl"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="booleanSelectControlUndecided"/>
						<form:error name="booleanSelectControlUndecided"/>
					</form:description>
					<form:custom name="booleanSelectControlUndecided"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="selectControlInvalidSelection"/>
						<form:error name="selectControlInvalidSelection"/>
					</form:description>
					<form:select name="selectControlInvalidSelection"
						size="2"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="selectControlMultiInvalidSelection"/>
						<form:error name="selectControlMultiInvalidSelection"/>
					</form:description>
					<form:select name="selectControlMultiInvalidSelection"
						size="2"
					/>
				</form:descriptionCell>
				
				<form:descriptionCell>
					<form:description>
						<form:label name="dropDown"/>
						<form:error name="dropDown"/>
					</form:description>
					<form:custom name="dropDown"/>
				</form:descriptionCell>
				<form:descriptionCell>
					<form:description>
						<form:label name="dropDownMulti"/>
						<form:error name="dropDownMulti"/>
					</form:description>
					<form:custom name="dropDownMulti"/>
				</form:descriptionCell>

				<form:group name="selectionControlGroup"
					firstColumnWidth="27em"
				>
					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControl"/>
							<form:error name="selectionControl"/>
						</form:description>
						<form:popup name="selectionControl"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlFixed"/>
							<form:error name="selectionControlFixed"/>
						</form:description>
						<form:popup name="selectionControlFixed"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlFlipped"/>
							<form:error name="selectionControlFlipped"/>
						</form:description>
						<form:popup name="selectionControlFlipped"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlDelayedDblClick"/>
							<form:error name="selectionControlDelayedDblClick"/>
						</form:description>
						<form:popup name="selectionControlDelayedDblClick"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlTree"/>
							<form:error name="selectionControlTree"/>
						</form:description>
						<form:popup name="selectionControlTree"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlTreeAllExpanded"/>
							<form:error name="selectionControlTreeAllExpanded"/>
						</form:description>
						<form:popup name="selectionControlTreeAllExpanded"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlTreeCustomLabel"/>
							<form:error name="selectionControlTreeCustomLabel"/>
						</form:description>
						<form:popup name="selectionControlTreeCustomLabel"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlTreeFlipped"/>
							<form:error name="selectionControlTreeFlipped"/>
						</form:description>
						<form:popup name="selectionControlTreeFlipped"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlNoAutoComplete"/>
							<form:error name="selectionControlNoAutoComplete"/>
						</form:description>
						<form:popup name="selectionControlNoAutoComplete"
							autoCompletion="false"
						/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlMulti"/>
							<form:error name="selectionControlMulti"/>
						</form:description>
						<form:popup name="selectionControlMulti"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlMultiFlipped"/>
							<form:error name="selectionControlMultiFlipped"/>
						</form:description>
						<form:popup name="selectionControlMultiFlipped"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlTableSingle"/>
							<form:error name="selectionControlTableSingle"/>
						</form:description>
						<form:popup name="selectionControlTableSingle"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlTableMulti"/>
							<form:error name="selectionControlTableMulti"/>
						</form:description>
						<form:popup name="selectionControlTableMulti"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlTreeTableSingle"/>
							<form:error name="selectionControlTreeTableSingle"/>
						</form:description>
						<form:popup name="selectionControlTreeTableSingle"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlTreeTableMultiWithRootNode"/>
							<form:error name="selectionControlTreeTableMultiWithRootNode"/>
						</form:description>
						<form:popup name="selectionControlTreeTableMultiWithRootNode"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlTreeTableMultiWithoutRootNode"/>
							<form:error name="selectionControlTreeTableMultiWithoutRootNode"/>
						</form:description>
						<form:popup name="selectionControlTreeTableMultiWithoutRootNode"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlTreeTableInfinite"/>
							<form:error name="selectionControlTreeTableInfinite"/>
						</form:description>
						<form:popup name="selectionControlTreeTableInfinite"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlMultiTree"/>
							<form:error name="selectionControlMultiTree"/>
						</form:description>
						<form:popup name="selectionControlMultiTree"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlMultiTreeFlipped"/>
							<form:error name="selectionControlMultiTreeFlipped"/>
						</form:description>
						<form:popup name="selectionControlMultiTreeFlipped"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlMultiTreeUnordered"/>
							<form:error name="selectionControlMultiTreeUnordered"/>
						</form:description>
						<form:popup name="selectionControlMultiTreeUnordered"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlMultiTreeFixed"/>
							<form:error name="selectionControlMultiTreeFixed"/>
						</form:description>
						<form:popup name="selectionControlMultiTreeFixed"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlMultiCustomOrder"/>
							<form:error name="selectionControlMultiCustomOrder"/>
						</form:description>
						<form:popup name="selectionControlMultiCustomOrder"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlLarge"/>
							<form:error name="selectionControlLarge"/>
						</form:description>
						<form:popup name="selectionControlLarge"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlLargeMulti"/>
							<form:error name="selectionControlLargeMulti"/>
						</form:description>
						<form:popup name="selectionControlLargeMulti"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlLargeMultiCustomOrder"/>
							<form:error name="selectionControlLargeMultiCustomOrder"/>
						</form:description>
						<form:popup name="selectionControlLargeMultiCustomOrder"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlFixedMulti"/>
							<form:error name="selectionControlFixedMulti"/>
						</form:description>
						<form:popup name="selectionControlFixedMulti"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlFixedMultiCustomOrder"/>
							<form:error name="selectionControlFixedMultiCustomOrder"/>
						</form:description>
						<form:popup name="selectionControlFixedMultiCustomOrder"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlFixedLargeMulti"/>
							<form:error name="selectionControlFixedLargeMulti"/>
						</form:description>
						<form:popup name="selectionControlFixedLargeMulti"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlFixedLargeMultiCustomOrder"/>
							<form:error name="selectionControlFixedLargeMultiCustomOrder"/>
						</form:description>
						<form:popup name="selectionControlFixedLargeMultiCustomOrder"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlOnlyFixedSelectionSingle"/>
							<form:error name="selectionControlOnlyFixedSelectionSingle"/>
						</form:description>
						<form:popup name="selectionControlOnlyFixedSelectionSingle"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlOnlyFixedSelectionMulti"/>
							<form:error name="selectionControlOnlyFixedSelectionMulti"/>
						</form:description>
						<form:popup name="selectionControlOnlyFixedSelectionMulti"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlOnlyFixedSelectionSingleCustomOrder"/>
							<form:error name="selectionControlOnlyFixedSelectionSingleCustomOrder"/>
						</form:description>
						<form:popup name="selectionControlOnlyFixedSelectionSingleCustomOrder"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlOnlyFixedSelectionMultiCustomOrder"/>
							<form:error name="selectionControlOnlyFixedSelectionMultiCustomOrder"/>
						</form:description>
						<form:popup name="selectionControlOnlyFixedSelectionMultiCustomOrder"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="invalidSelectionSingle"/>
							<form:error name="invalidSelectionSingle"/>
						</form:description>
						<form:popup name="invalidSelectionSingle"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="invalidSelectionSingleLarge"/>
							<form:error name="invalidSelectionSingleLarge"/>
						</form:description>
						<form:popup name="invalidSelectionSingleLarge"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="invalidSelectionMulti"/>
							<form:error name="invalidSelectionMulti"/>
						</form:description>
						<form:popup name="invalidSelectionMulti"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="invalidSelectionMultiLarge"/>
							<form:error name="invalidSelectionMultiLarge"/>
						</form:description>
						<form:popup name="invalidSelectionMultiLarge"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="invalidSelectionSingleFixedSelection"/>
							<form:error name="invalidSelectionSingleFixedSelection"/>
						</form:description>
						<form:popup name="invalidSelectionSingleFixedSelection"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="invalidSelectionSingleFixedSelectionLarge"/>
							<form:error name="invalidSelectionSingleFixedSelectionLarge"/>
						</form:description>
						<form:popup name="invalidSelectionSingleFixedSelectionLarge"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="invalidSelectionSingleFixedOption"/>
							<form:error name="invalidSelectionSingleFixedOption"/>
						</form:description>
						<form:popup name="invalidSelectionSingleFixedOption"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="invalidSelectionSingleFixedOptionLarge"/>
							<form:error name="invalidSelectionSingleFixedOptionLarge"/>
						</form:description>
						<form:popup name="invalidSelectionSingleFixedOptionLarge"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="invalidSelectionMultiFixed"/>
							<form:error name="invalidSelectionMultiFixed"/>
						</form:description>
						<form:popup name="invalidSelectionMultiFixed"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="invalidSelectionMultiFixedLarge"/>
							<form:error name="invalidSelectionMultiFixedLarge"/>
						</form:description>
						<form:popup name="invalidSelectionMultiFixedLarge"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="invalidSelectionMultiFixedCustomOptionOrder"/>
							<form:error name="invalidSelectionMultiFixedCustomOptionOrder"/>
						</form:description>
						<form:popup name="invalidSelectionMultiFixedCustomOptionOrder"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionControlEmptyOptionList"/>
							<form:error name="selectionControlEmptyOptionList"/>
						</form:description>
						<form:popup name="selectionControlEmptyOptionList"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="selectionTextControlMultiFixed"/>
							<form:error name="selectionTextControlMultiFixed"/>
						</form:description>
						<form:popupText name="selectionTextControlMultiFixed"/>
					</form:descriptionCell>
				</form:group>

				<form:descriptionCell>
					<form:description>
						<form:label name="selectOptionControl"/>
					</form:description>
					<form:label name="selectOptionControl"
						index="0"
					/>
					<form:option name="selectOptionControl"
						index="0"
					/>
					<form:label name="selectOptionControl"
						index="1"
					/>
					<form:option name="selectOptionControl"
						index="1"
					/>
					<form:label name="selectOptionControl"
						index="2"
					/>
					<form:option name="selectOptionControl"
						index="2"
					/>
					<form:label name="selectOptionControl"
						index="3"
					/>
					<form:option name="selectOptionControl"
						index="3"
					/>
					<form:error name="selectOptionControl"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="selectOptionControlMulti"/>
					</form:description>
					<form:label name="selectOptionControlMulti"
						index="0"
					/>
					<form:option name="selectOptionControlMulti"
						index="0"
					/>
					<form:label name="selectOptionControlMulti"
						index="1"
					/>
					<form:option name="selectOptionControlMulti"
						index="1"
					/>
					<form:label name="selectOptionControlMulti"
						index="2"
					/>
					<form:option name="selectOptionControlMulti"
						index="2"
					/>
					<form:label name="selectOptionControlMulti"
						index="3"
					/>
					<form:option name="selectOptionControlMulti"
						index="3"
					/>
					<form:error name="selectOptionControlMulti"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="dataField"/>
						<form:error name="dataField"/>
					</form:description>
					<form:dataItem name="dataField"/>
				</form:descriptionCell>

				<form:ifExists name="folderField">
					<form:descriptionCell
						labelAbove="true"
						wholeLine="true"
					>
						<form:description>
							<form:label name="folderField"/>
						</form:description>
						<form:folder name="folderField"/>
					</form:descriptionCell>
				</form:ifExists>

				<form:descriptionCell
					labelAbove="true"
					wholeLine="true"
				>
					<form:description>
						<form:label name="tableListField"/>
					</form:description>
					<form:tablelist name="tableListField"/>
				</form:descriptionCell>

				<form:descriptionCell labelAbove="true">
					<form:description>
						<form:label name="displayImageControl"/>
						Upload of images is expected:
					</form:description>
					<form:custom name="displayImageControl"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="iconControlTest.icons"/>
					</form:description>
					<form:custom name="iconControlTest.icons"/>
					<form:custom name="iconControlTest.icon"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="longRunningListener"/>
						<form:error name="longRunningListener"/>
					</form:description>
					<form:select name="longRunningListener"/>
					<form:custom name="listenerGroup"/>
				</form:descriptionCell>

				<form:cell>
					<form:custom name="openPopupMenu"/>
				</form:cell>
				<form:cell>
					<form:custom name="openInvalidateCompnoentField"/>
				</form:cell>

				<form:descriptionCell>
					<form:description>
						<form:resource key="visibilityOfMenuOpenerTest"/>
					</form:description>
					<form:inputCell name="command1Visibility"/>
					<form:inputCell name="command2Visibility"/>
					<form:custom name="visibilityOfMenuOpenerTest"/>
				</form:descriptionCell>

				<form:cell>
					<form:command name="command1ToggleExecutablityCommand2"
						renderer="<%= LinkButtonRenderer.INSTANCE %>"
					/>
				</form:cell>

				<form:descriptionCell>
					<form:description>
						<form:label name="command2ToggleExecutablityCommand1"
							colon="true"
						/>
					</form:description>
					<form:command name="command2ToggleExecutablityCommand1"
						renderer="<%= ImageButtonRenderer.INSTANCE %>"
					/>
				</form:descriptionCell>

				<form:cell>
					<form:command name="command3ToggleVisibilityOfCommand1And2"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>

				<form:cell>
					<form:command name="command4ToggleExecutablitySetEditable"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>

				<form:cell>
					<form:command name="alertWithLineBreak"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>

				<form:cell>
					<form:command name="failWithMultipleErrors"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>

				<form:cell>
					<form:command name="failWithLegacyTopLogicExceptionNoI18N"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>

				<form:cell>
					<form:command name="failWithTopLogicExceptionInfo"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
					<form:command name="failWithTopLogicExceptionWarning"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
					<form:command name="failWithTopLogicExceptionError"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
					<form:command name="failWithTopLogicExceptionSystemFailure"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>

				<form:cell>
					<form:command name="failWithFatalTopLogicException"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>

				<form:cell>
					<form:command name="failWithRuntimeException"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>

				<form:cell>
					<form:command name="failWithThrowable"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>

				<form:cell>
					<form:command name="messageBoxLongRunningCommand"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>

				<form:cell>
					<form:command name="progressCommand"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>

				<form:cell>
					<form:command name="progressWithFinalizeCommand"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>

				<form:cell>
					<form:command name="progressWithLinearCompletionCommand"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>
				
				<form:cell>
					<form:command name="InfoServiceErrorWithDetails"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
					<form:command name="InfoServiceWarning"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
					<form:command name="InfoServiceInfo"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>
				
				<form:cell>
					<form:input name="DialogWindowWarningMessage"/>
				</form:cell>
				
				<form:cell>
					<form:input name="DialogWindowErrorMessage"/>
				</form:cell>
				
				<form:cell>					
					<form:command name="ThrowErrorDialogWindow"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>
				
				<form:cell>
					<form:button
						command="checkAll"
						renderer="<%= ButtonComponentButtonRenderer.INSTANCE %>"
						reskey="layouts.layoutdemo.TestControlsForm_shared.main.jsp.checkAll"
					/>
				</form:cell>

				<form:cell>
					<form:button
						command="checkAll"
						renderer="<%= ButtonComponentButtonRenderer.INSTANCE %>"
					/>
				</form:cell>
				
				<form:group name="image"
					firstColumnWidth="12em"
				>
					<form:hr key="imageField"/>

					<form:inputCell name="imageWidth"/>
					<form:inputCell name="imageHeight"/>

					<form:descriptionCell>
						<form:description>
							<form:label name="imageDynamic"/>
						</form:description>
						<form:checkbox name="imageDynamic"/>
					</form:descriptionCell>

					<form:cell style="position: relative; height: 150px">
						<form:custom name="imageField"/>
					</form:cell>

					<form:hr key="pictureField"/>

					<form:descriptionCell>
						<form:description>
							<form:label name="pictureInputField"/>
							<form:error name="pictureInputField"/>
						</form:description>
						<form:custom name="pictureInputField"/>
					</form:descriptionCell>

					<form:descriptionCell>
						<form:description>
							<form:label name="pictureField"/>
						</form:description>
						<form:custom name="pictureField"/>
					</form:descriptionCell>

					<form:hr key="galleryField"/>
					<form:cell>
						<form:gallery name="galleryField"
							height="256"
							width="320"
						/>
					</form:cell>
				</form:group>

				<form:cell wholeLine="true">
					<form:tabbar name="deckField"/>
				</form:cell>

				<form:cell wholeLine="true">
					<form:deck name="deckField">
						<form:card name="tab1">
							<form:checkbox name="checkboxControl2"
								yesNo="true"
							/>
						</form:card>
					</form:deck>
				</form:cell>

				<form:cell>
					Dependencies between the following fields:
				</form:cell>

				<form:cell>
					<form:date name="start"/>
					<form:error name="start"/>
					--
					<form:date name="end"/>
					<form:error name="end"/>
				</form:cell>

				<form:cell wholeLine="true">
					<form:tableview name="tableField"/>
				</form:cell>

				<form:cell wholeLine="true">
					<form:tableview name="tableFieldNoColumnSelect"/>
				</form:cell>

				<form:cell wholeLine="true">
					<form:tableview name="tableFieldNoColumnConfig"/>
				</form:cell>

				<form:cell wholeLine="true">
					<form:tableview name="tableFieldEditable"/>
				</form:cell>

				<form:cell wholeLine="true">
					<form:tableview name="tableFieldGlobalSelection"/>
				</form:cell>

				<form:cell wholeLine="true">
					<form:table name="selectionTableField"
						columnNames="string int date double"
						selectable="true"
					/>
				</form:cell>

				<form:cell wholeLine="true">
					<form:table name="selectionTableFieldMandatory"
						columnNames="string int date double"
						selectable="true"
					/>
				</form:cell>
			</form:group>

			<form:group name="CSSClassChangeOnUserInput">
				<form:cell wholeLine="true">
					<ul>
						<form:forEach member="m">
							<li>
								<form:label name="${m}"
									colon="true"
								/>
								<form:custom name="${m}"/>
							</li>
						</form:forEach>
					</ul>
				</form:cell>
			</form:group>

			<form:group name="selectionPartSingle"
				firstColumnWidth="11em"
			>
				<form:cell wholeLine="true">
					<ul>
						<form:forEach member="m">
							<li>
								<form:label name="${m}"
									colon="true"
								/>
								<form:custom name="${m}"/>
							</li>
						</form:forEach>
					</ul>
				</form:cell>
			</form:group>

			<form:group name="selectionPartMultiple"
				firstColumnWidth="11em"
			>
				<form:cell wholeLine="true">
					<ul>
						<form:forEach member="m">
							<li>
								<form:label name="${m}"
									colon="true"
								/>
								<form:custom name="${m}"/>
							</li>
						</form:forEach>
					</ul>
				</form:cell>
			</form:group>

			<form:group name="messages"
				collapsible="true"
				cssClass="demoImportantGroup"
			>
				<form:cell wholeLine="true">
					Note: This group should have a red background due to a custom CSS class.
				</form:cell>

				<form:cell>
					<form:command name="infoMessage"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>
				<form:cell>
					<form:command name="confirmMessage"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>
				<form:cell>
					<form:command name="warnMessage"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>
				<form:cell>
					<form:command name="errorMessage"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>
				<form:cell>
					<form:command name="dynamicDialog"
						renderer="<%= DefaultButtonRenderer.INSTANCE %>"
					/>
				</form:cell>
			</form:group>

			<form:group name="dataFieldGroup"
				collapsible="true"
				firstColumnWidth="36em"
			>
				<form:inputCell name="upload"/>
				<form:inputCell name="uploadForDownload"/>
				<form:inputCell name="uploadAndSetDataToOtherUpload"/>
				<form:inputCell name="justDownloads"/>

				<form:descriptionCell>
					<form:description>
						<form:label name="upload3DownloadInline"/>
						<form:error name="upload3DownloadInline"/>
					</form:description>
					<form:dataItem name="upload3DownloadInline"
						downloadInline="true"
					/>
				</form:descriptionCell>

				<form:inputCell name="upload5IndividualNoValueText"/>

				<form:descriptionCell>
					<form:description>
						<form:label name="toggleReadOnly"/>
					</form:description>
					<form:command name="toggleReadOnly"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="upload6AllowEmptyFile"/>
						<form:error name="upload6AllowEmptyFile"/>
					</form:description>
					<form:dataItem name="upload6AllowEmptyFile"
						forbidEmptyFileUpload="false"
					/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="upload7AllowEmptyFileWithNotEmptyConstraint"/>
						<form:error name="upload7AllowEmptyFileWithNotEmptyConstraint"/>
					</form:description>
					<form:dataItem name="upload7AllowEmptyFileWithNotEmptyConstraint"
						forbidEmptyFileUpload="false"
					/>
				</form:descriptionCell>

				<form:inputCell name="uploadImmutable"/>
				<form:inputCell name="uploadImmutableDisabledDownload"/>
				<form:inputCell name="multiUpload"/>
			</form:group>
			<form:group name="collapsibleNoName"
				collapsible="true"
				legend="false"
			>
				<p>
					This group is collapsible, but does not have a name.
				</p>
			</form:group>
			<form:group name="nonCollapsibleName"
				collapsible="false"
				legend="true"
			>
				<p>
					This group is not collapsible, but does have a name.
				</p>
			</form:group>
			<form:group name="nonCollapsibleNoName"
				collapsible="false"
				legend="false"
			>
				<p>
					This group is not collapsible and does not have a name.
				</p>
			</form:group>

			<form:group name="externalLinks">
				<form:descriptionCell>
					<form:description>
						<form:label name="simpleLink"/>
					</form:description>
					<form:custom name="simpleLink"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="linkWithLabel"/>
					</form:description>
					<form:custom name="linkWithLabel"/>
				</form:descriptionCell>

				<form:descriptionCell>
					<form:description>
						<form:label name="linkWithImage"/>
					</form:description>
					<form:custom name="linkWithImage"/>
				</form:descriptionCell>
			</form:group>

			<form:group name="configuration">
				<form:cell wholeLine="true">
					<form:resource key="switchStateDescription"/>
				</form:cell>

				<form:descriptionCell>
					<form:description>
						<form:label name="switchStateField"/>
					</form:description>
					<form:input name="switchStateField"/>
				</form:descriptionCell>
			</form:group>
		</form:form>
	</layout:body>
</layout:html>