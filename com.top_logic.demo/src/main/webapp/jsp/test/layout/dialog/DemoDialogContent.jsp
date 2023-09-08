<%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<h1>
			Dialog Demo
		</h1>

		<form:form>
			<p>
				Modal dialog: <%= MainLayout.getComponent(pageContext).getName() %>
			</p>

			<div>
				<form:select name="select_1"/>
			</div>
			<div>
				<form:select name="select_2"/>
			</div>
			<div>
				<form:select name="select_3"/>
			</div>
			<div>
				<form:select name="select_4"/>
			</div>
			<div>
				<form:select name="select_5"/>
			</div>
			<div>
				<form:select name="select_6"/>
			</div>
			<div>
				<form:select name="select_7"/>
			</div>
			<div>
				<form:select name="select_8"/>
			</div>
		</form:form>
	</layout:body>
</layout:html>