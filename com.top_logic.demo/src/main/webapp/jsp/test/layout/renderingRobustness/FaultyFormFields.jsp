<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<h3>
			Marker "Page End" must be visible at page end to validate proper error handling during rendering of faulty form fields.
		</h3>

		<form:form displayWithoutModel="true">
			<div style="margin-top: 5px;">
				<form:custom name="faultyStringField"/>
			</div>
			<div style="margin-top: 5px;">
				<form:select name="faultySingleSelectControl"/>
			</div>
			<div style="margin-top: 5px;">
				<form:select name="faultyMultiSelectControl"/>
			</div>
			<div style="margin-top: 5px;">
				<form:select name="faultyMultiImmutableSelectControl"/>
			</div>
			<div style="margin-top: 5px;">
				<form:popup name="faultyMultiSelectionControl"/>
			</div>
			<div style="margin-top: 5px;">
				<form:popup name="faultyMultiImmutableSelectionControl"/>
			</div>
			<div style="margin-top: 5px;">
				<form:tabbar name="faultyDeckField"/>
				<form:deck name="faultyDeckField"/>
			</div>
			<div style="margin-top: 5px;">
				<form:custom name="faultyTreeField"/>
			</div>
			<div style="margin-top: 5px;">
				<form:tableview name="faultyTableField"/>
			</div>
		</form:form>
		<h3>
			Page End
		</h3>
	</layout:body>
</layout:html>