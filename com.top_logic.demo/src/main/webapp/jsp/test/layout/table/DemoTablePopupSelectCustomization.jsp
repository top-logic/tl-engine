<%@page import="com.top_logic.demo.layout.form.demo.DemoTablePopupSelectCustomization"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form displayWithoutModel="true">
			<form:inputCell name="<%=DemoTablePopupSelectCustomization.DESCRIPTION%>"/>
			<form:separator/>
			<form:groupCell name="<%= DemoTablePopupSelectCustomization.TABLE_FIELDS %>">
				<form:inputCell name="<%= DemoTablePopupSelectCustomization.TABLE_WITH_STANDARD_SETTINGS %>"/>
				<form:groupCell name="<%= DemoTablePopupSelectCustomization.TABLE_WITH_NAME_COLUMN_FIELDS %>">
					<form:inputCell name="<%=DemoTablePopupSelectCustomization.TABLE_WITH_NAME_COLUMN_RESOURCE_PROVIDER%>"/>
					<form:inputCell name="<%=DemoTablePopupSelectCustomization.TABLE_WITH_NAME_COLUMN_RENDERER%>"/>
					<form:inputCell name="<%=DemoTablePopupSelectCustomization.TABLE_WITH_NAME_COLUMN_CELL_RENDERER%>"/>
				</form:groupCell>
				<form:groupCell name="<%= DemoTablePopupSelectCustomization.TABLE_WITH_CUSTOM_COLUMN_FIELDS %>">
					<form:inputCell name="<%= DemoTablePopupSelectCustomization.TABLE_WITH_CUSTOM_COLUMN_RESOURCE_PROVIDER %>"/>
					<form:inputCell name="<%= DemoTablePopupSelectCustomization.TABLE_WITH_CUSTOM_COLUMN_RENDERER %>"/>
					<form:inputCell name="<%= DemoTablePopupSelectCustomization.TABLE_WITH_CUSTOM_COLUMN_CELL_RENDERER %>"/>
				</form:groupCell>
			</form:groupCell>
			<form:groupCell name="<%= DemoTablePopupSelectCustomization.TREE_FIELDS %>">
				<form:inputCell name="<%=DemoTablePopupSelectCustomization.TREE_WITH_STANDARD_SETTINGS %>"/>
				<form:groupCell name="<%= DemoTablePopupSelectCustomization.TREE_WITH_NAME_COLUMN_FIELDS %>">
					<form:inputCell name="<%=DemoTablePopupSelectCustomization.TREE_WITH_NAME_COLUMN_RESOURCE_PROVIDER%>"/>
					<form:inputCell name="<%=DemoTablePopupSelectCustomization.TREE_WITH_NAME_COLUMN_RENDERER%>"/>
					<form:inputCell name="<%=DemoTablePopupSelectCustomization.TREE_WITH_NAME_COLUMN_CELL_RENDERER%>"/>
				</form:groupCell>
				<form:groupCell name="<%= DemoTablePopupSelectCustomization.TREE_WITH_CUSTOM_COLUMN_FIELDS %>">
					<form:inputCell name="<%= DemoTablePopupSelectCustomization.TREE_WITH_CUSTOM_COLUMN_RESOURCE_PROVIDER %>"/>
					<form:inputCell name="<%= DemoTablePopupSelectCustomization.TREE_WITH_CUSTOM_COLUMN_RENDERER %>"/>
					<form:inputCell name="<%= DemoTablePopupSelectCustomization.TREE_WITH_CUSTOM_COLUMN_CELL_RENDERER %>"/>
				</form:groupCell>
			</form:groupCell>
		</form:form>
	</layout:body>
</layout:html>