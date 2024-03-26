<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.util.error.Icons"
%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page import="com.top_logic.layout.basic.ThemeImage"
%><%@page import="com.top_logic.gui.ThemeFactory"
%><%@page import="com.top_logic.util.Resources"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="basic" prefix="basic"
%>
<form:hr reskeyConst="<%=Resources.encodeLiteralText(\"HR rendered as title bar\") %>"/>
Here is something that looks like the contents of the hr title, but is not.
<form:hr/>

<form:groupCell
	personalizationName="foobar"
	titleText="Text in group cell"
>
	<form:cell wholeLine="true">
		Here is something that is the contents of the group box.
	</form:cell>
</form:groupCell>

<form:hr/>

<form:columns count="2">
	<form:groupCell
		columns="2"
		personalizationName="bar"
		titleText="One group besides another"
	>
		<form:descriptionCell>
			<form:description>
				First label:
			</form:description>
			Content of cell 1
		</form:descriptionCell>
		<form:descriptionCell>
			<form:description>
				Additional label:
			</form:description>
			Additional cell in first group
		</form:descriptionCell>
		<form:descriptionCell>
			Entry without description.
		</form:descriptionCell>

		<form:separator/>

		<form:cell wholeLine="true">
			Plain cell without special handling in the context of description/content cells.
		</form:cell>
	</form:groupCell>

	<form:groupCell
		columns="2"
		personalizationName="bar"
	>
		<form:cellTitle raw="true">
			<span>
				Cell with a border setting contrary to the current theme
			</span>
			<span class="dfToolbar">
				<basic:image icon="<%= Icons.INFO %>"/>
			</span>
		</form:cellTitle>

		<form:descriptionCell>
			<form:description>
				Second label:
			</form:description>
			A box explicitly
			<br/>
			spawning two rows.
		</form:descriptionCell>
		<form:descriptionCell>
			<form:description>
				Last label:
			</form:description>
			Content
			<br/>
			of last box in a row grabs all
			<br/>
			space (cells) below that is not occupied.
		</form:descriptionCell>
	</form:groupCell>

	<form:groupCell
		columns="2"
		firstColumnWidth="15em"
		personalizationName="foo"
		wholeLine="true"
	>
		<form:descriptionContainer>
			<form:descriptionCell>
				<form:description>
					Third label:
				</form:description>
				Contents of a description box within an untitled group cell.
			</form:descriptionCell>

			<form:columns count="2">
				<form:cell
					cssClass="rf_label"
					width="14.5em"
				>
					Simulated description cell
				</form:cell>
				<form:cell>
					Simulated content cell.
				</form:cell>
			</form:columns>

			<form:descriptionCell>
				<form:description>
					The very last (little long) label:
				</form:description>
				Content of cell 4
			</form:descriptionCell>
		</form:descriptionContainer>
		<form:descriptionContainer>
			<form:cell>
				Some big cell...
			</form:cell>
		</form:descriptionContainer>
	</form:groupCell>

	<form:groupCell
		initiallyCollapsed="true"
		personalizationName="fooBar"
		titleText="Initially collapsed group"
		wholeLine="true"
	>
		<form:cell>
			Initially collapsed group. After changing collapse state it is stored in the personal configuration.
		</form:cell>
	</form:groupCell>

	<form:groupCell
		columns="1"
		titleText="Group with two groups"
		wholeLine="true"
	>
		<form:cell wholeLine="true">
			<form:columns>
				<form:cell width="80%">
					<form:groupCell
						columns="1"
						personalizationName="80percentgroup"
						titleText="Group with width 80%"
					>
						<form:cell>
							Group with width 80%.
						</form:cell>
					</form:groupCell>
				</form:cell>
				<form:cell width="20%">
					<form:groupCell
						columns="1"
						personalizationName="20percentgroup"
						titleText="Group with width 20%"
					>
						<form:cell>
							Group with width 20%.
						</form:cell>
					</form:groupCell>
				</form:cell>
			</form:columns>
		</form:cell>
	</form:groupCell>
</form:columns>