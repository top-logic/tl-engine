<%@page import="com.top_logic.layout.form.control.DefaultButtonRenderer"
%><%@page import="com.top_logic.layout.form.control.ButtonRenderer"
%><%@page import="com.top_logic.layout.form.control.ImageButtonRenderer"
%><%@page import="com.top_logic.demo.layout.form.demo.TestBoxesComponent"
%><%@page import="com.top_logic.layout.form.boxes.reactive_tag.GroupCellTag"
%><%@page import="com.top_logic.gui.ThemeFactory"
%><%@page import="com.top_logic.util.Resources"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		

		<form:form>
			Testing input elements in box layout.
			<form:columns count="2">
				<form:groupCell name="<%=TestBoxesComponent.GROUP1_GROUP %>"
					columns="2"
					labelAbove="false"
				>
					<form:inputCell name="<%=TestBoxesComponent.STRING_FIELD %>"/>
					<form:inputCell name="<%=TestBoxesComponent.BOOLEAN_FIELD %>"
						labelFirst="false"
					/>

					<form:inputCell name="<%=TestBoxesComponent.DATE_FIELD %>"/>

					<form:cell>
						<form:command name="<%=TestBoxesComponent.TOGGLE_BUTTON %>"
							renderer="<%= DefaultButtonRenderer.INSTANCE %>"
						/>
					</form:cell>
				</form:groupCell>

				<form:groupCell
					columns="2"
					labelAbove="true"
					personalizationName="bar"
				>
					<form:cellTitle raw="true">
						<span class="dfToolbar">
							<form:scope name="<%=TestBoxesComponent.GROUP2_GROUP %>">
								<form:command name="<%=TestBoxesComponent.RESET_BUTTON %>"
									renderer="<%=ImageButtonRenderer.INSTANCE%>"
								/>
							</form:scope>
						</span>

						<span class="<%=GroupCellTag.TITLE_TEXT_CSS_CLASS %>">
							<form:label name="<%=TestBoxesComponent.GROUP2_GROUP%>"
								colon="false"
							/>
						</span>
					</form:cellTitle>

					<form:scope name="<%=TestBoxesComponent.GROUP2_GROUP %>">
						<form:descriptionCell>
							<form:description>
								<form:label name="<%=TestBoxesComponent.TEXT_FIELD %>"
									colon="true"
								/>
							</form:description>

							<form:input name="<%=TestBoxesComponent.TEXT_FIELD %>"
								multiLine="true"
								rows="4"
							/>
						</form:descriptionCell>

						<form:inputCell name="<%=TestBoxesComponent.DATA_FIELD %>"/>
					</form:scope>
				</form:groupCell>
			</form:columns>

			<form:groupCell
				columns="1"
				personalizationName="independent"
				titleText="Independent contents in column layout"
			>
				<form:columns count="3">
					<form:cell>
						First cell independent of the outer table layout.
					</form:cell>
					<form:cell>
						Second cell independent of the outer table layout.
					</form:cell>
					<form:cell>
						Third cell independent of the outer table layout.
					</form:cell>
					<form:cell>
						Fourth cell independent of the outer table layout.
					</form:cell>
					<form:cell>
						Fifth cell independent of the outer table layout.
					</form:cell>
				</form:columns>
			</form:groupCell>

			<form:groupCell
				personalizationName="dependent"
				titleText="Back in table layout"
			>
				<form:columns count="2">
					<form:cell
						cssClass="rf_label"
						width="10em"
					>
						Left cell like a label:
					</form:cell>
					<form:cell>
						Right cell with massive contents...
					</form:cell>
				</form:columns>
			</form:groupCell>
		</form:form>
	</layout:body>
</layout:html>