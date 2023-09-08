<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.currency.EditCurrencyListComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"%>

<%
EditCurrencyListComponent theComponent = (EditCurrencyListComponent) MainLayout.getComponent(pageContext);
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form>
			<br/>
			<table
				align="center"
				width="280px"
			>
				<tr>
					<td>
						<form:tablelist name="<%=EditCurrencyListComponent.FIELD_TABLE%>"
							initialSortColumn="1"
							rowObjectCreator="<%=theComponent%>"
							sortable="true"
						/>
					</td>
				</tr>
			</table>
			<form:error name="<%=EditCurrencyListComponent.FIELD_TABLE%>"/>
			<br/>
		</form:form>
	</layout:body>
</layout:html>