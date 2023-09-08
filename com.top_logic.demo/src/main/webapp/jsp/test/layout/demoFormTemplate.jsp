<%@page import="com.top_logic.layout.ResPrefix"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="layout" prefix="layout"
%><%@page	import="com.top_logic.demo.layout.form.demo.model.DemoFormTemplateContext"
%><%@page import="com.top_logic.demo.layout.form.demo.model.DemoFormTemplate"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%
DemoFormTemplate theComponent = (DemoFormTemplate) MainLayout.getComponent(pageContext);
%><%@page import="com.top_logic.layout.form.template.SimpleListControlProvider"
%><%@page import="com.top_logic.util.Resources"
%><layout:html>
	<layout:head/>
	<layout:body>
		

		<form:form>
			<form:groupCell titleKeySuffix="info.legend">
				<form:cell wholeLine="true">
					<form:resource key="info"/>
				</form:cell>
			</form:groupCell>

			<form:group name="<%=DemoFormTemplateContext.CONTROL_GROUP %>">
				<table>
					<tr>
						<td>
							<form:label name="<%= DemoFormTemplateContext.SELECT_NUMBER_OF_ROWS %>"/>
						</td>
						<td>
							<form:select name="<%= DemoFormTemplateContext.SELECT_NUMBER_OF_ROWS %>"/>
						</td>
					</tr>
					<tr>
						<td>
							<form:label name="<%= DemoFormTemplateContext.SELECT_NUMBER_OF_COLUMNS %>"/>
						</td>
						<td>
							<form:select name="<%= DemoFormTemplateContext.SELECT_NUMBER_OF_COLUMNS %>"/>
						</td>
					</tr>
					<tr>
						<td>
							&#xA0;
						</td>
						<td>
							&#xA0;
						</td>
						<td>
							<form:button command="<%= DemoFormTemplate.ApplyCommand.COMMAND %>"/>
						</td>
					</tr>
				</table>
			</form:group>

			<form:groupCell titleKeySuffix="table">
				<form:custom name="<%=DemoFormTemplateContext.CONTROLLED_GROUP %>"
					controlProvider="<%=DemoFormTemplate.CONTROL_PROVIDER %>"
				/>
			</form:groupCell>

			<form:groupCell titleKeySuffix="formGroupDemo">
				<form:custom name="<%=DemoFormTemplateContext.FORM_GROUP %>"
					controlProvider="<%=DemoFormTemplate.CONTROL_PROVIDER %>"
				/>
			</form:groupCell>
			<%
			ResPrefix i18nKey = theComponent.getResPrefix();
			%>
			<form:groupCell columns="2">
				<form:cellTitle>
					<%=Resources.getInstance().getMessage(i18nKey.key("simpleListDemo"), SimpleListControlProvider.class.getName())%>
				</form:cellTitle>
				<form:cell>
					<%=Resources.getInstance().getMessage(i18nKey.key("simpleListDemoInfo"), SimpleListControlProvider.class.getName())%>
				</form:cell>
				<form:cell>
					<form:custom name="<%=DemoFormTemplateContext.SIMPLE_GROUP %>"
						controlProvider="<%= theComponent.getSimpleListControlProvider()%>"
					/>
				</form:cell>
			</form:groupCell>
		</form:form>
	</layout:body>
</layout:html>