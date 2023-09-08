<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="java.util.List"
%><%@page import="java.util.Date"
%><%@page import="java.util.ArrayList"
%><%@page import="com.top_logic.knowledge.wrap.Wrapper"
%><%@page import="com.top_logic.model.TLClass"
%><%@page import="com.top_logic.model.TLStructuredTypePart"
%><%@page import="com.top_logic.mail.layout.MailComponent"
%><%@page import="com.top_logic.mail.base.Mail"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.form.model.FormContext"
%><%@page import="com.top_logic.util.ResourceViewerServlet"
%><%@page import="com.top_logic.knowledge.objects.KOAttributes"
%><%@page import="com.top_logic.basic.StringServices"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%
MailComponent theComponent  = (MailComponent) MainLayout.getComponent(pageContext);
Mail          theModel      = (Mail) theComponent.getModel();
FormContext   theContext    = theComponent.getFormContext();
%><%@page import="com.top_logic.basic.Logger"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<%
			try {
				%>
				<table
					align="center"
					border="0"
					cellpadding="0"
					summary="mail details"
					width="100%"
				>
					<tr>
						<td class="label">
							<form:label name="<%= MailComponent.FIELD_NAME %>"
								colon="true"
							/>
						</td>
						<td class="content">
							<form:input name="<%= MailComponent.FIELD_NAME %>"/>
						</td>
					</tr>
					<tr>
						<td class="label">
							<form:label name="<%= MailComponent.FIELD_SEND_DATE %>"
								colon="true"
							/>
						</td>
						<td class="content">
							<form:input name="<%= MailComponent.FIELD_SEND_DATE %>"/>
						</td>
					</tr>
					<tr>
						<td class="label">
							<form:label name="<%= MailComponent.FIELD_FROM %>"
								colon="true"
							/>
						</td>
						<td class="content">
							<form:input name="<%= MailComponent.FIELD_FROM %>"/>
						</td>
					</tr>
					<tr>
						<td class="label">
							<form:label name="<%= MailComponent.FIELD_TO %>"
								colon="true"
							/>
						</td>
						<td class="content">
							<form:input name="<%= MailComponent.FIELD_TO %>"/>
						</td>
					</tr>
					<tr>
						<td class="label">
							<form:label name="<%= MailComponent.FIELD_CC %>"
								colon="true"
							/>
						</td>
						<td class="content">
							<form:input name="<%= MailComponent.FIELD_CC %>"/>
						</td>
					</tr>
					<tr>
						<td class="label">
							<form:label name="<%= MailComponent.FIELD_ATTACHMENTS %>"
								colon="true"
							/>
						</td>
						<td>
							<form:scope name="<%= MailComponent.FIELD_ATTACHMENTS %>">
								<form:forEach member="attachment">
									<form:dataItem name="${attachment}"/>
								</form:forEach>
							</form:scope>
						</td>
					</tr>
					<tr>
						<td colspan="2">
							<hr/>
						</td>
					</tr>
					<tr>
						<td colspan="2">
						</td>
					</tr>
				</table>
				<%
				String theDSN = ResourceViewerServlet.getDocumentReference((String) theModel.getValue(KOAttributes.PHYSICAL_RESOURCE), null);
				String theURL = request.getContextPath() + "/servlet/ResourceViewerServlet?command=" + ResourceViewerServlet.COMMAND_INLINE +
				'&' + ResourceViewerServlet.PARAM_REF + '=' + theDSN;
				%>
				<div style="width:99%;height:100%;position:absolute">
					<iframe
						frameborder="1"
						height="100%"
						src="<%=theURL %>"
						width="100%"
					>
						<p>
						</p>
					</iframe>
				</div>
				<%
				} catch(Exception ex) {
				Logger.error("", ex, this);
				%>
				<form:resource key="errorInModel"/>
				<%
			}
			%>
		</form:form>
	</layout:body>
</layout:html>