<%@page import="com.top_logic.demo.layout.demo.table.DemoFormulaTables"
%><%@page import="com.top_logic.demo.layout.demo.table.DemoTableAccessor"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="basic" prefix="basic"
%><%
%><layout:html>
	<layout:head/>
	<layout:body>
		<h1>
			Tables in formulas
		</h1>

		<form:form>
			<form:columns count="2">
				<basic:fieldset wholeLine="true">
					<basic:legend>
						Sortable table without column chooser
					</basic:legend>
					<form:table name="<%=DemoFormulaTables.FIELD_SELECT_SORT_NO_REPLACE %>"
						columnNames="title surname givenName"
					/>
				</basic:fieldset>

				<basic:fieldset>
					<basic:legend>
						Table with column chooser that is not sortable
					</basic:legend>
					<form:table name="<%=DemoFormulaTables.FIELD_REPLACE_NO_SORT %>"
						columnNames="title surname givenName"
					/>
				</basic:fieldset>

				<basic:fieldset>
					<basic:legend>
						Plain formula table without selector
					</basic:legend>
					<form:tableview name="<%=DemoFormulaTables.FIELD_TABLE %>"/>
				</basic:fieldset>

				<basic:fieldset>
					<basic:legend>
						Plain formula table with initially hidden select column
					</basic:legend>
					<form:tableview name="<%=DemoFormulaTables.TABLE_HIDDEN_SELECT %>"/>
				</basic:fieldset>

				<basic:fieldset wholeLine="true">
					<basic:legend>
						Plain formula table with excluded select column
					</basic:legend>
					<form:tableview name="<%=DemoFormulaTables.TABLE_EXCLUDED_SELECT %>"/>
				</basic:fieldset>
			</form:columns>
		</form:form>
	</layout:body>
</layout:html>