<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.element.layout.security.AttributeClassifierRolesComponent"
%><%@page import="java.util.Iterator"
%><%@page import="java.util.Collection"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<%
		AttributeClassifierRolesComponent theComponent = (AttributeClassifierRolesComponent) MainLayout.getComponent(pageContext);
		/* Fetch FormContext here, because to get classifiers, the FormContext must be created. */
		theComponent.getFormContext();
		Collection         theClassifiers   = theComponent.getClassificationAttributeNames();
		
		%>
		<form:form displayCondition="<%=theClassifiers != null && !theClassifiers.isEmpty() %>">
			<%
			for (Iterator theIt = theClassifiers.iterator(); theIt.hasNext(); ) {
				
				String   theAttName       = (String) theIt.next();
				String   theColumnDecl    = theComponent.getTableColumnNamesDeclaration(theAttName);
				
				%>
				<form:groupCell titleKeySuffix="<%= theAttName %>">
					<form:cell wholeLine="true">
						<form:table name="<%= theAttName %>"
							columnNames="<%= theColumnDecl %>"
							initialSortColumn="1"
						/>
					</form:cell>
				</form:groupCell>
				<%
			}
			%>
		</form:form>
	</layout:body>
</layout:html>