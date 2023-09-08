<%@page import="com.top_logic.demo.layout.form.demo.TestCustomConfirmComponent"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<h1>
			TestCustomConfirmComponent
		</h1>

		<form:form>
			<p>
				Siehe
				<a
					href="http://tl/trac/wiki/Testfallkatalog/CommandConfirm"
					target="wiki"
				>
					wiki:Testfallkatalog/CommandConfirm
				</a>
				.
			</p>

			<p>
				<form:label name="<%=TestCustomConfirmComponent.NUMBER_FIELD %>"
					colon="true"
				/>
				<form:custom name="<%=TestCustomConfirmComponent.NUMBER_FIELD %>"/>
			</p>

			<p>
				Wert muss bei erstem Übernehmen/Speichern mindestens 1000 sein.
				Bei jedem weiteren Versuch halbiert sich die Schwelle.
				Dies ist ein Test für die Möglichkeit von nutzerdefinierten bedingten Nachfragen.
			</p>
		</form:form>
	</layout:body>
</layout:html>