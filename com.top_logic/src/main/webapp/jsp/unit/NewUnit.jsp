<%@page import="com.top_logic.model.util.TLModelUtil"
%><%@page extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="java.util.*,
com.top_logic.mig.html.layout.*"
%><%@page import="com.top_logic.knowledge.wrap.unit.Unit"
%><%@page import="com.top_logic.knowledge.wrap.unit.UnitWrapper"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage
			titleField="<%= UnitWrapper.NAME_ATTRIBUTE %>"
			type="<%= UnitWrapper.getUnitType() %>"
		>
			<form:inputCell name="<%=UnitWrapper.FORMAT%>"
				colon="true"
			/>
			<form:inputCell name="<%=UnitWrapper.SORT_ORDER%>"
				colon="true"
			/>
		</form:formPage>
	</layout:body>
</layout:html>