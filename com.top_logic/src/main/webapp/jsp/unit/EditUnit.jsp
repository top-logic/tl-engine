<%@page import="com.top_logic.model.util.TLModelUtil"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
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
			<form:groupCell titleKeySuffix="basicAttributes">
				<form:inputCell name="<%=UnitWrapper.FORMAT%>"/>
				<form:inputCell name="<%=UnitWrapper.SORT_ORDER%>"/>
				<form:inputCell name="<%=UnitWrapper.BASE_FORMAT_REF%>"/>
				<form:inputCell name="<%=UnitWrapper.FACTOR%>"/>
			</form:groupCell>
			<%@include file="/jsp/util/changeI18N.inc" %>
		</form:formPage>
	</layout:body>
</layout:html>