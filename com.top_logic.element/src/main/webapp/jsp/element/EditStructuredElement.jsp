<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.form.model.FormContext"
%><%@page import="com.top_logic.knowledge.wrap.Wrapper"
%><%@page import="com.top_logic.element.layout.structured.AdminElementComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="meta"     prefix="meta"
%><%@page import="com.top_logic.layout.form.FormField"
%><%!
private String getClick(FormContext aContext, String anID) {
	Object theField = aContext.getField(anID);
	
	if (theField instanceof FormField) {
		Object theValue = ((FormField) theField).getValue();
		
		if (!"[null]".equals(theValue)) {
			return ("&nbsp;(<a href=\"javascript:inspect('" + theValue + "');\">Click!</a>)");
		}
	}
	
	return ("");
}
%><%@page import="com.top_logic.tool.boundsec.AbstractCommandHandler"%>


<%
AdminElementComponent theComponent  = (AdminElementComponent) MainLayout.getComponent(pageContext);
FormContext           theContext    = theComponent.getFormContext();
Object                theModel      = theComponent.getModel();
%><%@page import="com.top_logic.knowledge.wrap.AbstractWrapper"
%><layout:html>
	<layout:head>
		<basic:script>
			function inspect(anID) {
				openWindow_admin_technical_attributed_inspector_inspector({<%= AbstractCommandHandler.OBJECT_ID %>: anID});
			}
		</basic:script>
	</layout:head>
	<layout:body>
		<meta:formPage
			subtitleField="<%=AdminElementComponent.ELEMENT_TYPE %>"
			titleAttribute="<%=AbstractWrapper.NAME_ATTRIBUTE %>"
		>
			<meta:group>
				<basic:fieldset titleKeySuffix="basic">
					<table class="frm"
						border="0"
						bordercolor="black"
						summary="Basic element information"
					>
						<colgroup>
							<col width="0%"/>
							<col width="100%"/>
						</colgroup>
						<tr>
							<td class="label">
								<form:label name="<%=AdminElementComponent.ELEMENT_ORDER %>"/>
								:
							</td>
							<td class="content">
								<form:select name="<%=AdminElementComponent.ELEMENT_ORDER %>"/>
								<form:error name="<%=AdminElementComponent.ELEMENT_ORDER %>"/>
							</td>
						</tr>
					</table>
				</basic:fieldset>
				<meta:attributes legend="additional"/>
			</meta:group>
		</meta:formPage>
	</layout:body>
</layout:html>