<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.element.layout.admin.component.ShowSecurityComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%
ShowSecurityComponent component = (ShowSecurityComponent) MainLayout.getComponent(pageContext);
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<p>
				<form:resource key="pageMessage"/>
			</p>

			<form:columns 
				count="4"
				keep="true"
			>
				<form:descriptionCell labelAbove="true">
					<form:description>
						<form:label name="<%=ShowSecurityComponent.FIELD_PERSONS%>"/>
						<form:error name="<%=ShowSecurityComponent.FIELD_PERSONS%>"/>
					</form:description>
					<form:popup name="<%=ShowSecurityComponent.FIELD_PERSONS%>"
						clearButton="true"
					/>
				</form:descriptionCell>

				<form:descriptionCell labelAbove="true">
					<form:description>
						<form:label name="<%=ShowSecurityComponent.FIELD_TYPES%>"/>
						<form:error name="<%=ShowSecurityComponent.FIELD_TYPES%>"/>
					</form:description>
					<form:popup name="<%=ShowSecurityComponent.FIELD_TYPES%>"
						clearButton="true"
					/>
				</form:descriptionCell>

				<form:descriptionCell labelAbove="true">
					<form:description>
						<form:label name="<%=ShowSecurityComponent.FIELD_OBJECTS%>"/>
						<form:error name="<%=ShowSecurityComponent.FIELD_OBJECTS%>"/>
					</form:description>
					<form:popup name="<%=ShowSecurityComponent.FIELD_OBJECTS%>"
						clearButton="true"
					/>
				</form:descriptionCell>

				<form:descriptionCell labelAbove="true">
					<form:description>
						<form:label name="<%=ShowSecurityComponent.FIELD_ROLES%>"/>
						<form:error name="<%=ShowSecurityComponent.FIELD_ROLES%>"/>
					</form:description>
					<form:popup name="<%=ShowSecurityComponent.FIELD_ROLES%>"
						clearButton="true"
					/>
				</form:descriptionCell>
			</form:columns>

			<form:cell wholeLine="true">
				<form:inputCell name="<%=ShowSecurityComponent.FIELD_SIMPLE%>"
					firstColumnWidth="14em"
				/>
			</form:cell>
		</form:form>
	</layout:body>
</layout:html>