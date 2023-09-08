<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.layout.form.boxes.tag.GroupCellTag"
%><%@page import="com.top_logic.gui.ThemeFactory"
%><%@page import="com.top_logic.util.Resources"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		
		Testing cells on a plain page.
		<%@include file="TestBoxesContent.jsp" %>
	</layout:body>
</layout:html>