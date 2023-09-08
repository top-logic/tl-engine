<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<h3>
			Marker "Page End" must be visible at page end to validate proper error handling during rendering of faulty form component.
		</h3>

		<form:form displayWithoutModel="true">
			<div>
				<form:select name="inexistentSelectControl"/>
			</div>
		</form:form>

		<h3>
			Page End
		</h3>
	</layout:body>
</layout:html>