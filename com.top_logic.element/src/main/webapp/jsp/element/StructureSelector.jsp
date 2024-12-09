<%@page extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.model.annotate.LabelPosition"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="com.top_logic.element.layout.tree.StructureSelectorComponent"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:filterForm>
			<form:columns
				count="1"
				keep="true"
			>
				<form:inputCell name="<%=StructureSelectorComponent.FORM_FIELD_STRUCTURE%>"
					firstColumnWidth="12em"
					labelPosition="<%=LabelPosition.INLINE%>"
					width="180px"
				/>
			</form:columns>
		</form:filterForm>
	</layout:body>
</layout:html>