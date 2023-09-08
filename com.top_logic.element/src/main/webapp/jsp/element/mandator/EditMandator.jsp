<%@page import="java.util.HashSet"
%><%@page import="com.top_logic.basic.ArrayUtil"
%><%@page import="com.top_logic.basic.StringServices"
%><%@page  extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.form.model.FormContext"
%><%@page import="java.util.List"
%><%@page import="java.util.ArrayList"
%><%@page import="com.top_logic.element.layout.structured.MandatorAdminComponent"
%><%@page import="com.top_logic.knowledge.wrap.Wrapper"
%><%@page import="com.top_logic.model.TLStructuredTypePart"
%><%@page import="com.top_logic.model.TLClass"
%><%@page import="com.top_logic.element.layout.structured.AdminElementComponent"
%><%@page import="com.top_logic.element.structured.wrap.Mandator"
%><%@taglib uri="layout"    prefix="layout"
%><%@taglib uri="ajaxform"  prefix="form"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="meta"      prefix="meta"
%><%!
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<%
			MandatorAdminComponent theComponent  = (MandatorAdminComponent) MainLayout.getComponent(pageContext);
			
			Mandator      theMandator = (Mandator) theComponent.getModel();
			%>
			<meta:group object="<%= theMandator %>">
				<basic:fieldset titleKeySuffix="attributes.key">
					<table class="frm"
						summary="generated form"
					>
						<colgroup>
							<col width="0%"/>
							<col width="100%"/>
						</colgroup>
						<tr>
							<td colspan="2">
								&#xA0;
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%=Mandator.NAME_ATTRIBUTE %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%=Mandator.NAME_ATTRIBUTE %>"/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= Mandator.NUMBER_HANDLER_ID %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%= Mandator.NUMBER_HANDLER_ID %>"/>
							</td>
						</tr>
						<%
						if (theMandator.allowsMove()) {
							%>
							<tr>
								<td class="label">
									<form:label name="<%=AdminElementComponent.ELEMENT_ORDER %>"
										colon="true"
									/>
								</td>
								<td class="content">
									<form:select name="<%=AdminElementComponent.ELEMENT_ORDER %>"/>
								</td>
							</tr>
							<%
						}
						%>
					</table>
				</basic:fieldset>

				<meta:attributes legend="additional"/>
			</meta:group>
		</form:form>
	</layout:body>
</layout:html>