<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.*"
%><%@page import="com.top_logic.knowledge.wrap.Wrapper"
%><%@page import="com.top_logic.element.structured.wrap.Mandator"
%><%@page import="com.top_logic.element.layout.structured.MandatorOverviewComponent"
%><%@taglib uri="layout"  	 prefix="layout"
%><%@taglib uri="ajaxform"  prefix="form"
%><%@taglib uri="basic"     prefix="basic"
%><%@taglib uri="meta"      prefix="meta"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<%
		MandatorOverviewComponent	theComp   = (MandatorOverviewComponent) MainLayout.getComponent(pageContext);
		Object                		theModel  = theComp.getModel();
		
		Wrapper        theAttributed = (Wrapper) theModel;
		%>
		<form:form>
			<meta:group object="<%= theAttributed %>">
				<basic:fieldset titleKeySuffix="attributes.key">
					<table>
						<tr>
							<td class="label">
								<meta:label name="<%=Mandator.DESCRIPTION %>"/>
							</td>
							<td class="content">
								<meta:attribute name="<%=Mandator.DESCRIPTION %>"/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%=Mandator.CONTACT_PERSONS %>"/>
							</td>
							<td class="content">
								<meta:attribute name="<%=Mandator.CONTACT_PERSONS %>"/>
							</td>
						</tr>
					</table>
				</basic:fieldset>
			</meta:group>
		</form:form>
	</layout:body>
</layout:html>