<%@page import="com.top_logic.layout.structure.OrientationAware.Orientation"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.knowledge.gui.layout.table.ExportModalDialogComponent"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%ExportModalDialogComponent theComp = (ExportModalDialogComponent)MainLayout.getComponent(pageContext);
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:form displayWithoutModel="true">
			<table class="frm"
				summary="generated form"
			>
				<tr>
					<td colspan="2">
						<form:resource key="message"/>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						&#xA0;
					</td>
				</tr>
				<tr>
					<td>
						<form:choice name="<%= theComp.EXPORT_OPTIONS_FIELD %>"
							orientation="<%=Orientation.VERTICAL %>"
						/>
					</td>
				</tr>
			</table>
		</form:form>
	</layout:body>
</layout:html>