<%@page import="com.top_logic.layout.DisplayContext"
%><%@page import="com.top_logic.basic.xml.TagWriter"
%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page import="com.top_logic.layout.basic.ResourceRenderer"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.element.structured.wrap.Mandator"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="meta"   prefix="meta"
%><%@taglib uri="ajaxform" prefix="form"
%><%
MandatorAdminComponent.NewMandatorComponent theComponent = (MandatorAdminComponent.NewMandatorComponent) MainLayout.getComponent(pageContext);
%><%@page import="com.top_logic.element.layout.structured.MandatorAdminComponent"
%><layout:html>
	<layout:head>
	</layout:head>
	<layout:body>
		<form:form>
			<h2>
				<form:resource key="title"/>
			</h2>
			<p>
				<form:resource key="message"/>
			</p>
			<meta:group object="<%=theComponent.getMetaElement() %>">
				<table class="frm"
					summary="Create mandator"
				>
					<tr>
						<td class="label">
							<form:resource key="model"/>
							:
						</td>
						<td class="content">
							<%
							DisplayContext context = DefaultDisplayContext.getDisplayContext(pageContext);
							TagWriter writer = MainLayout.getTagWriter(pageContext);
							Object mandator = theComponent.getModel();
							ResourceRenderer.INSTANCE.write(context, writer, mandator);
							writer.flushBuffer();
							%>
						</td>
					</tr>
					<tr>
						<td class="label">
							<meta:label name="<%=Mandator.NAME %>"
								colon="true"
							/>
						</td>
						<td class="content">
							<meta:attribute name="<%=Mandator.NAME %>"
								inputSize="20"
							/>
						</td>
					</tr>
					<tr>
						<td class="label">
							<meta:label name="<%=Mandator.NUMBER_HANDLER_ID %>"
								colon="true"
							/>
						</td>
						<td class="content">
							<meta:attribute name="<%=Mandator.NUMBER_HANDLER_ID %>"
								inputSize="20"
							/>
						</td>
					</tr>
				</table>
				<meta:attributes
					exclude="<%=theComponent.getExcludeForUI() %>"
					legend="additional"
				/>
			</meta:group>
		</form:form>
	</layout:body>
</layout:html>