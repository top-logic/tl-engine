<%@page import="com.top_logic.layout.form.control.DefaultButtonRenderer"
%><%@page import="com.top_logic.layout.form.control.ButtonRenderer"
%><%@page import="com.top_logic.layout.form.control.ImageButtonRenderer"
%><%@page import="com.top_logic.demo.layout.form.demo.TestBoxesComponentReactive"
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
			<form:cell>
				This page is to test the reactive behavior of forms. The boxes above each form includes the actual number of columns given by the MediaQueryControl and the number of columns the respective elements should be embedded. So e.g. 1 column means the element is rendered over 100% of the parent element and 3 columns means for 33.3%.
			</form:cell>

			<form:cell>
				<h3>
					Default
				</h3>
			</form:cell>

			<form:cell>
				If there is no group or column layout around the elements, the layout will consists of 1 column.
			</form:cell>
			<%-- Fields without a group --%>
			<form:cell>
				<h4>
					Fields without a group
				</h4>
			</form:cell>
			<form:cell cssClass="rf_infoBox">
				<form:cell cssClass="show-columns">
					Actual number of columns
					<i>
						n
					</i>
					:
				</form:cell>

				<form:cell>
					<ul>
						<li>
							Textfields: 1 column
						</li>
						<li>
							Textarea: 1 column
						</li>
					</ul>
				</form:cell>
			</form:cell>

			<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
			<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
			<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
			<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD4 %>"/>
			<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD5 %>"/>
			<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD6 %>"/>

			<form:descriptionCell>
				<form:description>
					<form:label name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
					<form:error name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
				</form:description>
				<form:custom name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
			</form:descriptionCell>
			<%-- Default group --%>
			<form:cell>
				<h4>
					Default group
				</h4>
			</form:cell>

			<form:cell cssClass="rf_infoBox">
				<form:cell cssClass="show-columns">
					Actual number of columns
					<i>
						n
					</i>
					:
				</form:cell>

				<form:cell>
					<ul>
						<li>
							Outer Gruppe: 1 column
						</li>
						<li>
							Inner Gruppe: 1 column
						</li>
						<li>
							Textfields: n columns
						</li>
						<li>
							Textarea: 1 column
						</li>
					</ul>
				</form:cell>
			</form:cell>

			<form:groupCell name="<%=TestBoxesComponentReactive.GROUP1_GROUP %>"
				titleText="Outer group"
			>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD4 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD5 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD6 %>"/>

				<form:descriptionCell>
					<form:description>
						<form:label name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
						<form:error name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
					</form:description>
					<form:custom name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
				</form:descriptionCell>
				<%-- inner group --%>
				<form:groupCell name="<%=TestBoxesComponentReactive.GROUP2_GROUP %>"
					titleText="Inner group"
				>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
				</form:groupCell>
			</form:groupCell>
			<%-- Group colums: 1 --%>
			<form:cell>
				<h4>
					1-column group
				</h4>
			</form:cell>
			<form:cell cssClass="rf_infoBox">
				<form:cell cssClass="show-columns">
					Actual number of columns
					<i>
						n
					</i>
					:
				</form:cell>

				<form:cell>
					<ul>
						<li>
							Outer Gruppe: 1 column
						</li>
						<li>
							Inner Gruppe: 1 column
						</li>
						<li>
							Textfields: 1 column
						</li>
						<li>
							Textarea: 1 column
						</li>
					</ul>
				</form:cell>
			</form:cell>

			<form:groupCell name="<%=TestBoxesComponentReactive.GROUP3_GROUP %>"
				columns="1"
				titleText="Outer group with 1 column inside"
			>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD4 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD5 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD6 %>"/>

				<form:descriptionCell>
					<form:description>
						<form:label name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
						<form:error name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
					</form:description>
					<form:custom name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
				</form:descriptionCell>
				<%-- inner group --%>
				<form:groupCell name="<%=TestBoxesComponentReactive.GROUP4_GROUP %>"
					titleText="Inner group"
				>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
				</form:groupCell>
			</form:groupCell>
			<%-- Group colums: 2 --%>
			<form:cell>
				<h4>
					2-column group
				</h4>
			</form:cell>

			<form:cell cssClass="rf_infoBox">
				<form:cell cssClass="show-columns">
					Actual number of columns
					<i>
						n
					</i>
					:
				</form:cell>

				<form:cell>
					<ul>
						<li>
							Outer Gruppe: 1 column
						</li>
						<li>
							Inner Gruppe: 1 column
						</li>
						<li>
							Textfields: 2 columns (at n &#8805; 3), n columns (at n &lt; 3)
						</li>
						<li>
							Textarea: 1 column
						</li>
					</ul>
				</form:cell>
			</form:cell>

			<form:groupCell name="<%=TestBoxesComponentReactive.GROUP5_GROUP %>"
				columns="2"
				titleText="Outer group with 2 columns inside"
			>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD4 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD5 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD6 %>"/>

				<form:descriptionCell>
					<form:description>
						<form:label name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
						<form:error name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
					</form:description>
					<form:custom name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
				</form:descriptionCell>
				<%-- inner group --%>
				<form:groupCell name="<%=TestBoxesComponentReactive.GROUP6_GROUP %>"
					titleText="Inner group"
				>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
				</form:groupCell>
			</form:groupCell>
			<%-- Group colums: 3 --%>
			<form:cell>
				<h4>
					3-column group
				</h4>
			</form:cell>

			<form:cell cssClass="rf_infoBox">
				<form:cell cssClass="show-columns">
					Actual number of columns
					<i>
						n
					</i>
					:
				</form:cell>

				<form:cell>
					<ul>
						<li>
							Outer Gruppe: 1 column
						</li>
						<li>
							Inner Gruppe: 1 column
						</li>
						<li>
							Textfields: n columns
						</li>
						<li>
							Textarea: 1 column
						</li>
					</ul>
				</form:cell>
			</form:cell>

			<form:groupCell name="<%=TestBoxesComponentReactive.GROUP7_GROUP %>"
				columns="3"
				titleText="Outer group with 3 columns inside"
			>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD4 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD5 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD6 %>"/>

				<form:descriptionCell>
					<form:description>
						<form:label name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
						<form:error name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
					</form:description>
					<form:custom name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
				</form:descriptionCell>
				<%-- inner group --%>
				<form:groupCell name="<%=TestBoxesComponentReactive.GROUP8_GROUP %>"
					titleText="Inner group"
				>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
				</form:groupCell>
			</form:groupCell>
			<%-- Group colums: 4 --%>
			<form:cell>
				<h4>
					4-column group
				</h4>
			</form:cell>

			<form:cell cssClass="rf_infoBox">
				<form:cell cssClass="show-columns">
					Actual number of columns
					<i>
						n
					</i>
					:
				</form:cell>

				<form:cell>
					<ul>
						<li>
							Outer Gruppe: 1 column
						</li>
						<li>
							Inner Gruppe: 1 column
						</li>
						<li>
							Textfields: 4 columns (at n &#8805; 3), n columns (at n &lt; 3)
						</li>
						<li>
							Textarea: 1 column
						</li>
					</ul>
				</form:cell>
			</form:cell>

			<form:groupCell name="<%=TestBoxesComponentReactive.GROUP9_GROUP %>"
				columns="4"
				titleText="Outer group with 4 columns inside"
			>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD4 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD5 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD6 %>"/>

				<form:descriptionCell>
					<form:description>
						<form:label name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
						<form:error name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
					</form:description>
					<form:custom name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
				</form:descriptionCell>
				<%-- inner group --%>
				<form:groupCell name="<%=TestBoxesComponentReactive.GROUP10_GROUP %>"
					titleText="Inner group"
				>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
				</form:groupCell>
			</form:groupCell>
			<%-- Group colums: 5 --%>
			<form:cell>
				<h4>
					5-column group
				</h4>
			</form:cell>

			<form:cell cssClass="rf_infoBox">
				<form:cell cssClass="show-columns">
					Actual number of columns
					<i>
						n
					</i>
					:
				</form:cell>

				<form:cell>
					<ul>
						<li>
							Outer Gruppe: 1 column
						</li>
						<li>
							Inner Gruppe: 1 column
						</li>
						<li>
							Textfields: 5 columns (at n &#8805; 3), n columns (at n &lt; 3)
						</li>
						<li>
							Textarea: 1 column
						</li>
					</ul>
				</form:cell>
			</form:cell>

			<form:groupCell name="<%=TestBoxesComponentReactive.GROUP11_GROUP %>"
				columns="5"
				titleText="Outer group with 5 columns inside"
			>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD4 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD5 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD6 %>"/>

				<form:descriptionCell>
					<form:description>
						<form:label name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
						<form:error name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
					</form:description>
					<form:custom name="<%=TestBoxesComponentReactive.TEXT_FIELD1 %>"/>
				</form:descriptionCell>
				<%-- inner group --%>
				<form:groupCell name="<%=TestBoxesComponentReactive.GROUP12_GROUP %>"
					titleText="Inner group"
				>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
				</form:groupCell>
			</form:groupCell>

			<form:cell>
				<h3>
					Columns layout
				</h3>
			</form:cell>

			<form:cell>
				<h4>
					3 columns layout
				</h4>
			</form:cell>

			<form:cell cssClass="rf_infoBox">
				<form:cell cssClass="show-columns">
					Actual number of columns
					<i>
						n
					</i>
					:
				</form:cell>

				<form:cell>
					<ul>
						<li>
							Outer Gruppe: n column
						</li>
						<li>
							Inner Gruppe: 1 column
						</li>
						<li>
							Textfields: n columns (outside the group), 1 column (inside the group)
						</li>
						<li>
							Textarea: 1 column
						</li>
					</ul>
				</form:cell>
			</form:cell>

			<form:columns count="3">
				<%-- Fields without a group --%>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD4 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD5 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD6 %>"/>

				<form:descriptionCell>
					<form:description>
						<form:label name="<%=TestBoxesComponentReactive.TEXT_FIELD2 %>"/>
						<form:error name="<%=TestBoxesComponentReactive.TEXT_FIELD2 %>"/>
					</form:description>
					<form:custom name="<%=TestBoxesComponentReactive.TEXT_FIELD2 %>"/>
				</form:descriptionCell>
			</form:columns>

			<form:columns count="3">
				<%-- outer group --%>
				<form:groupCell name="<%=TestBoxesComponentReactive.GROUP13_GROUP %>"
					titleText="Outer group inside of a columns layout"
				>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD4 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD5 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD6 %>"/>

					<form:descriptionCell>
						<form:description>
							<form:label name="<%=TestBoxesComponentReactive.TEXT_FIELD2 %>"/>
							<form:error name="<%=TestBoxesComponentReactive.TEXT_FIELD2 %>"/>
						</form:description>
						<form:custom name="<%=TestBoxesComponentReactive.TEXT_FIELD2 %>"/>
					</form:descriptionCell>
					<%-- inner group --%>
					<form:groupCell name="<%=TestBoxesComponentReactive.GROUP14_GROUP %>"
						titleText="Inner group"
					>
						<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
						<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
						<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
					</form:groupCell>
				</form:groupCell>
			</form:columns>

			<form:cell>
				<h4>
					3 columns layout with a group with wholeLine-attribute
				</h4>
			</form:cell>

			<form:cell cssClass="rf_infoBox">
				<form:cell cssClass="show-columns">
					Actual number of columns
					<i>
						n
					</i>
					:
				</form:cell>

				<form:cell>
					<ul>
						<li>
							Outer Gruppe: 1 column
						</li>
						<li>
							Inner Gruppe: 1 column
						</li>
						<li>
							Textfields: 1 columns
						</li>
						<li>
							Textarea: 1 column
						</li>
					</ul>
				</form:cell>
			</form:cell>

			<form:columns count="3">
				<form:groupCell name="<%=TestBoxesComponentReactive.GROUP15_GROUP %>"
					titleText="Outer group with wholeLine-attribute"
					wholeLine="true"
				>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD4 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD5 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD6 %>"/>

					<form:descriptionCell>
						<form:description>
							<form:label name="<%=TestBoxesComponentReactive.TEXT_FIELD2 %>"/>
							<form:error name="<%=TestBoxesComponentReactive.TEXT_FIELD2 %>"/>
						</form:description>
						<form:custom name="<%=TestBoxesComponentReactive.TEXT_FIELD2 %>"/>
					</form:descriptionCell>
					<%-- inner group --%>
					<form:groupCell name="<%=TestBoxesComponentReactive.GROUP16_GROUP %>"
						titleText="Inner group"
					>
						<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
						<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
						<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
					</form:groupCell>
				</form:groupCell>
			</form:columns>

			<form:cell>
				<h4>
					3 columns layout with a group with wholeLine-attribute and 3 columns
				</h4>
			</form:cell>

			<form:cell cssClass="rf_infoBox">
				<form:cell cssClass="show-columns">
					Actual number of columns
					<i>
						n
					</i>
					:
				</form:cell>

				<form:cell>
					<ul>
						<li>
							Outer Gruppe: 1 column
						</li>
						<li>
							Inner Gruppe: 1 column
						</li>
						<li>
							Textfields: n columns
						</li>
						<li>
							Textarea: 1 column
						</li>
					</ul>
				</form:cell>
			</form:cell>

			<form:columns count="3">
				<form:groupCell name="<%=TestBoxesComponentReactive.GROUP17_GROUP %>"
					columns="3"
					titleText="Outer group with wholeLine-attribute and 3 columns inside"
					wholeLine="true"
				>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD4 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD5 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD6 %>"/>

					<form:descriptionCell>
						<form:description>
							<form:label name="<%=TestBoxesComponentReactive.TEXT_FIELD2 %>"/>
							<form:error name="<%=TestBoxesComponentReactive.TEXT_FIELD2 %>"/>
						</form:description>
						<form:custom name="<%=TestBoxesComponentReactive.TEXT_FIELD2 %>"/>
					</form:descriptionCell>
					<%-- inner group --%>
					<form:groupCell name="<%=TestBoxesComponentReactive.GROUP18_GROUP %>"
						titleText="Inner group"
					>
						<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
						<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
						<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
					</form:groupCell>
				</form:groupCell>
			</form:columns>

			<form:cell>
				<h4>
					3-columns layout with keep-attribute
				</h4>
			</form:cell>

			<form:cell cssClass="rf_infoBox">
				<form:cell cssClass="show-columns">
					Actual number of columns
					<i>
						n
					</i>
					:
				</form:cell>
				<form:cell>
					<ul>
						<li>
							Textfields: 3 columns
						</li>
					</ul>
				</form:cell>
			</form:cell>

			<form:columns
				count="3"
				keep="true"
			>
				<%-- Fields without a group --%>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD1 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD2 %>"/>
				<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD3 %>"/>
			</form:columns>
			<%-- Special cases --%>
			<form:cell>
				<h3>
					Special cases / known layout problems
				</h3>
			</form:cell>

			<form:cell cssClass="rf_infoBox">
				<form:cell cssClass="show-columns">
					Actual number of columns
					<i>
						n
					</i>
					:
				</form:cell>
				<form:cell>
					<ul>
						<li>
							Keep inline: keep label and input field in one line
						</li>
						<li>
							Multiple inputs: write inputs in different lines
						</li>
					</ul>
				</form:cell>
			</form:cell>

			<form:descriptionCell keepInline="true">
				<form:description>
					Keep inline:
				</form:description>
				<form:input name="<%=TestBoxesComponentReactive.STRING_FIELD4 %>"/>
			</form:descriptionCell>

			<form:descriptionCell>
				<form:description>
					Multiple inputs:
				</form:description>
				<form:columns>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD5 %>"/>
					<form:inputCell name="<%=TestBoxesComponentReactive.STRING_FIELD6 %>"/>
				</form:columns>
			</form:descriptionCell>
		</form:form>
	</layout:body>
</layout:html>