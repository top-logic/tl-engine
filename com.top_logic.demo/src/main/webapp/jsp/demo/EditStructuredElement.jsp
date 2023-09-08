<%@page import="com.top_logic.demo.model.types.DemoTypesAll"
%><%@page import="com.top_logic.demo.edit.DemoFormContextModificator"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.form.model.FormContext"
%><%@page import="com.top_logic.knowledge.wrap.Wrapper"
%><%@page import="com.top_logic.element.layout.structured.AdminElementComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="meta"     prefix="meta"
%><%@page import="com.top_logic.layout.form.FormField"
%><%@page import="com.top_logic.tool.boundsec.AbstractCommandHandler"
%><%@page import="com.top_logic.knowledge.wrap.AbstractWrapper"
%><layout:html>
	<layout:head/>
	<layout:body>
		<meta:formPage
			subtitleField="<%=AdminElementComponent.ELEMENT_TYPE %>"
			titleAttribute="<%=DemoTypesAll.NAME_ATTR %>"
		>
			<meta:group>
				<form:groupCell titleKeySuffix="basic">
					<form:inputCell name="<%=AdminElementComponent.ELEMENT_TYPE %>"/>
					<form:inputCell name="<%=AdminElementComponent.ELEMENT_ORDER %>"/>
				</form:groupCell>

				<meta:attributes
					firstColumnWidth="250px"
					legend="additional"
				/>

				<form:groupCell titleKey="layouts.demo.editStructureDetail.childrenTable">
					<form:ifExists name="<%=DemoFormContextModificator.CHILDREN_TABLE %>">
						<form:emptyCell/>
						<form:cell wholeLine="true">
							<form:tableview name="<%=DemoFormContextModificator.CHILDREN_TABLE %>"/>
							<form:error name="<%=DemoFormContextModificator.CHILDREN_TABLE %>"/>
						</form:cell>
					</form:ifExists>
				</form:groupCell>
			</meta:group>
		</meta:formPage>
	</layout:body>
</layout:html>