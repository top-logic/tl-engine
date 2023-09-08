<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="com.top_logic.demo.layout.demo.DemoTabbingComponent"
%><%@page import="com.top_logic.gui.ThemeFactory"
%><layout:html>
	<layout:head/>
	<layout:body>
		<h1>
			Tabbing-Test zwischen Formularfeldern
		</h1>
		<form:form>
			<form:input name="<%=DemoTabbingComponent.FIELD1%>"/>
			<div style="position:relative; width:100%; height:100px; background-color:#F4F5F6">
			</div>
			<div style="position:relative; width:100%; height:100px; background-color:transparent">
			</div>
			<div style="position:relative; width:100%; height:100px; background-color:#F4F5F6">
			</div>
			<div style="position:relative; width:100%; height:100px; background-color:transparent">
			</div>
			<div style="position:relative; width:100%; height:100px; background-color:#F4F5F6">
			</div>
			<div style="position:relative; width:100%; height:100px; background-color:transparent">
			</div>
			<div style="position:relative; width:100%; height:100px; background-color:#F4F5F6">
			</div>
			<div style="position:relative; width:100%; height:100px; background-color:transparent">
			</div>
			<div style="position:relative; width:100%; height:100px; background-color:#F4F5F6">
			</div>
			<div style="position:relative; width:100%; height:100px; background-color:transparent">
			</div>
			<form:input name="<%=DemoTabbingComponent.FIELD2%>"/>
		</form:form>
	</layout:body>
</layout:html>