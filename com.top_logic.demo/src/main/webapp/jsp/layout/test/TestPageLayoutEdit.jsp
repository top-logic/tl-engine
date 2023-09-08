<%@page import="com.top_logic.demo.layout.form.demo.TestPageLayoutEdit"
%><%@page import="com.top_logic.layout.form.tag.StaticPageRenderer"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	
	<layout:body>
		<form:formPage
			image="theme:DEMO_DIALOG_ICON"
			imageTooltipKeySuffix="iconTooltip"
			subtitleField="<%= TestPageLayoutEdit.SELECT_FIELD %>"
			titleField="<%= TestPageLayoutEdit.STRING_FIELD %>"
		>
			Form with fields in the title bar.
		</form:formPage>
	</layout:body>
</layout:html>