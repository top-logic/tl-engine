<%@page import="com.top_logic.gui.ThemeFactory"
%><%@page import="com.top_logic.util.Resources"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		

		<form:form>
			Testing cells within a `form` tag.
			
			<%@include file="TestBoxesContent.jsp" %>
		</form:form>
	</layout:body>
</layout:html>