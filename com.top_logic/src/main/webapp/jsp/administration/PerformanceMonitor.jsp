<%--
*
* @history   2001-09-12    kha    Incorportated servlet code here
* @history   2001-08-30    kha    Added missing taglibg and this comment
* @author    <a href="mailto:kha@top-logic.com">???</a>
--%>
<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.layout.admin.component.PerformanceMonitorComponent"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="java.util.Collection"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<form:tableview name="<%= PerformanceMonitorComponent.PERFORMANCE_TABLE %>"/>
		</form:form>
	</layout:body>
</layout:html>