<%@page import="com.top_logic.demo.knowledge.test.layout.EditTreeDemo"
%><%@page extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="basic" prefix="basic"
%><%@page import="com.top_logic.layout.form.control.DefaultButtonRenderer"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<basic:fieldset>
				<basic:legend>
					Tree1
				</basic:legend>
				<form:custom name="tree1"/>
			</basic:fieldset>

			<basic:fieldset>
				<basic:legend>
					Tree2
				</basic:legend>
				<form:custom name="tree2"/>
			</basic:fieldset>

			<form:label name="<%=EditTreeDemo.NODE_NAME_FIELD %>"/>
			<form:input name="<%=EditTreeDemo.NODE_NAME_FIELD %>"/>
			<form:command name="moveUp"
				renderer="<%= DefaultButtonRenderer.INSTANCE %>"
			/>
			<form:command name="remove"
				renderer="<%= DefaultButtonRenderer.INSTANCE %>"
			/>
			<form:command name="createChild"
				renderer="<%= DefaultButtonRenderer.INSTANCE %>"
			/>
			<form:command name="reset"
				renderer="<%= DefaultButtonRenderer.INSTANCE %>"
			/>
			<form:command name="moveLevelUp"
				renderer="<%= DefaultButtonRenderer.INSTANCE %>"
			/>
			<form:command name="moveLevelDown"
				renderer="<%= DefaultButtonRenderer.INSTANCE %>"
			/>

			<basic:fieldset>
				<basic:legend>
					Ticket #2374
				</basic:legend>
				<form:custom name="tree1RootInvisible"/>
			</basic:fieldset>

			<basic:fieldset>
				<basic:legend>
					bread crumb #2486
				</basic:legend>
				<div>
					Alle breadcrumbs sind miteinander verknüpft.
				</div>
				<div>
					Der letzte Knoten ist immer selektiert. Selektion lässt das Breadcrumb kollabieren.
					<form:custom name="tree3"/>
				</div>
				<div>
					Die Selektion ist unabhängig von dem letzten Knoten. Selektion lässt angezeigten Pfad unangetastet.
					<form:custom name="tree4"/>
				</div>
				<div>
					Der letzte Knoten stimmt mit der Selektion in den anderen Bäumen überein. Es gibt keine Selektion.
					<form:custom name="tree5"/>
				</div>
			</basic:fieldset>
		</form:form>
	</layout:body>
</layout:html>