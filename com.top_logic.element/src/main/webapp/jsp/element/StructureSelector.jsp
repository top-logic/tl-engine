<%@page extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="com.top_logic.element.layout.tree.StructureSelectorComponent"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:filterForm>
			<form:columns
				count="1"
				keep="true"
			>
				<form:inputCell name="<%=StructureSelectorComponent.FORM_FIELD_STRUCTURE%>"
					firstColumnWidth="12em"
					labelAbove="false"
					width="180px"
				/>
			</form:columns>
		</form:filterForm>
	</layout:body>
</layout:html>