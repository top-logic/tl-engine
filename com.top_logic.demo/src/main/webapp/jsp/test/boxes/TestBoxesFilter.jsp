<%@page import="com.top_logic.layout.form.boxes.tag.GroupCellTag"
%><%@page import="com.top_logic.gui.ThemeFactory"
%><%@page import="com.top_logic.util.Resources"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		

		<form:filterForm>
			Testing cells within a `filterForm` tag.
			
			<%@include file="TestBoxesContent.jsp" %>
		</form:filterForm>
	</layout:body>
</layout:html>