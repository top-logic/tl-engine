<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.mig.html.layout.LayoutComponent"
%><%@page import="com.top_logic.util.Resources"%>

<%
LayoutComponent theComponent = MainLayout.getComponent(pageContext);
Object          theModel     = theComponent.getModel();
%><layout:html>
	<layout:head/>

	<layout:body>
		<%
		if (theModel == null) {
			out.write(Resources.getInstance().getString(theComponent.getResPrefix().key("noModel")));
		}
		else {
			%>
			<div style="text-align:center">
				<layout:img imgId="chart"/>
			</div>
			<%
		}
		%>
	</layout:body>
</layout:html>