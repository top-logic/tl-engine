<%@page import="com.top_logic.basic.col.TypedAnnotatable"
%><%@page import="com.top_logic.basic.col.TypedAnnotatable.Property"
%><%@page import="com.top_logic.layout.form.template.DefaultFormFieldControlProvider"
%><%@page import="com.top_logic.util.Utils"
%><%@page import="com.top_logic.basic.CollectionUtil"
%><%@page import="com.top_logic.base.mail.MailHelper.SendMailResult"
%><%@page import="com.top_logic.base.mail.MailHelper"
%><%@page import="com.top_logic.basic.StringServices"
%><%@page import="com.top_logic.layout.form.template.TextInputFormFieldControlProvider"
%><%@page import="com.top_logic.layout.form.template.ValueWithErrorControlProvider"
%><%@page import="com.top_logic.layout.Control"
%><%@page import="com.top_logic.basic.xml.TagWriter"
%><%@page import="com.top_logic.util.TLContext"
%><%@page import="com.top_logic.layout.form.ValueListener"
%><%@page import="com.top_logic.layout.form.FormField"
%><%@page import="com.top_logic.layout.form.model.FormFactory"
%><%@page import="com.top_logic.layout.form.model.FormContext"
%><%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"%>
<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>

<%-- Maintenance template version: 1.6 --%>
<%!
// JSP page configuration:

// XXX_BUTTON = null: no xxx button
// RUN_BUTTON = null && SIMULATE_BUTTON = null: run automatic
private static final String RUN_BUTTON = "Send";
private static final String SIMULATE_BUTTON = null;
private static final String REFRESH_BUTTON = null;
private static final String RESTART_LINK = "Reload page";
{
	LOG_PRINTS = true;
	LOG_TIME = true;
	WAITING_MESSAGE = "Running...";
	SIM_WAITING_MESSAGE = "Simulating...";
	USE_WAITING_ANI = true;
	TITLE = "Send test mail";
	DESCRIPTION = "This page sends a test mail to check whether the mail connection is configured properly.";
}

private static final Property<Object> PROPERTY_RECIPIENT = TypedAnnotatable.property(Object.class, "ADMINJSP_SendTestMail_Recipient");
private static final Property<Object> PROPERTY_SUBJECT = TypedAnnotatable.property(Object.class, "ADMINJSP_SendTestMail_Subject");
private static final Property<Object> PROPERTY_CONTENT = TypedAnnotatable.property(Object.class, "ADMINJSP_SendTestMail_Content");
private static final Property<Object> PROPERTY_HTML = TypedAnnotatable.property(Object.class, "ADMINJSP_SendTestMail_HTML");
private static final String DEFAULT_RECIPIENT = null;
private static final String DEFAULT_SUBJECT = "Top-Logic Test-Mail";
private static final String DEFAULT_CONTENT = "<h1>Top-Logic Test-Mail</h1>"
+"<p>Hallo!</p><p><font color=\"#0000FF\">Wenn Sie diese E-Mail erhalten, ist die E-Mail-Anbindung in <i>Top-Logic</i> richtig konfiguriert.</font></p><p>Mit freundlichen Grüßen,<br/>Ihr Top-Logic Team";
private static final Boolean DEFAULT_HTML = Boolean.TRUE;
%>

<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>
<layout:html>
	<layout:head>
		<title>
			<%=TITLE%>
		</title>
		<meta
			content="text/html; charset=UTF-8"
			http-equiv="Content-Type"
		/>
		<basic:cssLink/>
		<basic:script>
			services.ajax.ignoreTLAttributes = true;
		</basic:script>
	</layout:head>
	<%-- Insert import statements here. --%>
	<%@page import="com.top_logic.mig.html.layout.MainLayout"
	%><%@page import="com.top_logic.mig.html.layout.LayoutComponent"
	%><%@page import="com.top_logic.layout.DisplayContext"
	%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"%>
	
	<%!
	// This methods does the actual work:
	@Override
	protected void doWork(JspWriter out, boolean simulate, HttpServletRequest request) throws Exception {
		print("Sending test mail...");
		print(); print();
		
		try {
			String recipient = (String)TLContext.getContext().get(PROPERTY_RECIPIENT);
			if (StringServices.isEmpty(recipient)) {
				print("No recipient entered.");
			}
			else {
				String subject = Utils.getStringValue(TLContext.getContext().get(PROPERTY_SUBJECT));
				if (subject == null) subject = DEFAULT_SUBJECT;
				String content = Utils.getStringValue(TLContext.getContext().get(PROPERTY_CONTENT));
				if (content == null) content = DEFAULT_CONTENT;
				boolean html = Utils.getbooleanValue(TLContext.getContext().get(PROPERTY_HTML));
				MailHelper mailHelper = MailHelper.getInstance();
				SendMailResult result = mailHelper.sendSystemMail(CollectionUtil.intoList(recipient), subject, content,
				html ? MailHelper.CONTENT_TYPE_HTML_UTF8 : MailHelper.CONTENT_TYPE_TEXT_UTF8);
				if (result.isSuccess()) {
					printInfo("Mail successfully sent to '" + recipient + "'.");
				}
				else {
					printError("Failed to send mail to '" + recipient + "': " + StringServices.toString(result.getException()), result.getException());
					printError(result.getErrorResultString().toString());
				}
				
			}
		}
		catch (Exception e) {
			printError("Error while sending test mail.");
			printError(e.toString(), e);
			rollback();
		}
	}
	%>
	<layout:body>
		<basic:access>
			<%
			LayoutComponent component = MainLayout.getComponent(pageContext);
			DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(pageContext);
			
			writeWaitingSlider(pageContext, "SUBMIT", "SIMULATE");
			
			// run work
			if ((RUN_BUTTON == null && SIMULATE_BUTTON == null) ||
				(USE_WAITING_ANI && (request.getParameter("DO_SUBMIT") != null || request.getParameter("DO_SIMULATE") != null)) ||
				(!USE_WAITING_ANI && (request.getParameter("SUBMIT") != null || request.getParameter("SIMULATE") != null))) {
				
				boolean doSimulate = request.getParameter(USE_WAITING_ANI ? "DO_SIMULATE" : "SIMULATE") != null;
				if (doSimulate) {
					out.write("<br/><b>Simulating...</b><br/><br/><br/>\n");
				}
				%>
				<table style="margin: 5px">
					<tr>
						<td>
							<code class="normal">
								<%
								try {
									runWork(out, doSimulate, request);
								}
								catch (Throwable t) {
									printError("Error: An error occured while running the JSP page:");
									printError(t.toString());
									rollback();
								}
								%>
							</code>
						</td>
					</tr>
				</table>
				<%
				if (REFRESH_BUTTON != null) {
					%>
					<br/>
					<form method="POST">
						<table>
							<tr>
								<td>
									<p>
										&#xA0;
										<input name="<%=doSimulate ? "SIMULATE" : "SUBMIT"%>"
											type="submit"
											value="<%=REFRESH_BUTTON%>"
										/>
									</p>
								</td>
							</tr>
						</table>
					</form>
					<%
				}
				if (RESTART_LINK != null) {
					%>
					<p>
						<a
							href="javascript:self.location.href = '<%=component.getComponentURL(displayContext).getURL()%>';"
							style="color:darkblue"
						>
							&#xA0;<%=RESTART_LINK%>
						</a>
					</p>
					<%
				}
			}
			else {
				%>
				<form method="POST">
					<table>
						<tr>
							<td>
								<%!
								private static class FieldListener implements ValueListener {
									
									private final Property<Object> propertyName;
									
									public FieldListener(Property<Object> propertyName) {
										this.propertyName = propertyName;
									}
									
									@Override
									public void valueChanged(FormField formField, Object oldValue, Object newValue) {
										TLContext.getContext().set(propertyName, newValue);
									}
								}
								
								private void drawField(FormContext context, boolean multiLine, Property<Object> propertyName, Object defaultValue,
									DisplayContext displayContext, PageContext pageContext) throws Exception {
									FormField field = FormFactory.newStringField(propertyName.getName());
									field.setLabel(propertyName.getName());
									field.addValueListener(new FieldListener(propertyName));
									Object value = TLContext.getContext().get(propertyName);
									if (value == null) value = defaultValue;
									field.initializeField(value);
									field.check();
									context.addMember(field);
									Control control = ValueWithErrorControlProvider.newInstance(new TextInputFormFieldControlProvider(multiLine ? 5 : 1, multiLine ? 50 : 30, multiLine)).createControl(field, null);
									TagWriter tagWriter = MainLayout.getTagWriter(pageContext);
									control.write(displayContext, tagWriter);
									tagWriter.flushBuffer();
								}
								
								private void drawBooleanField(FormContext context, Property<Object> propertyName, Boolean defaultValue,
									DisplayContext displayContext, PageContext pageContext) throws Exception {
									FormField field = FormFactory.newBooleanField(propertyName.getName());
									field.setLabel(propertyName.getName());
									field.addValueListener(new FieldListener(propertyName));
									Object value = TLContext.getContext().get(propertyName);
									if (value == null) value = defaultValue;
									field.initializeField(value);
									field.check();
									context.addMember(field);
									Control control = ValueWithErrorControlProvider.newInstance(DefaultFormFieldControlProvider.INSTANCE).createControl(field, null);
									TagWriter tagWriter = MainLayout.getTagWriter(pageContext);
									control.write(displayContext, tagWriter);
									tagWriter.flushBuffer();
								}
								%>
								<table>
									<% FormContext context = new FormContext(component); %>
									<tr>
										<td>
											<b>
												Recipient:
											</b>
										</td>
										<td>
											&#xA0;
										</td>
										<td>
											<% drawField(context, false, PROPERTY_RECIPIENT, DEFAULT_RECIPIENT, displayContext, pageContext); %>
										</td>
										<td>
											The recipient of the test mail (only one recipient allowed).
										</td>
									</tr>
									<tr>
										<td>
											<b>
												Subject:
											</b>
										</td>
										<td>
											&#xA0;
										</td>
										<td>
											<% drawField(context, false, PROPERTY_SUBJECT, DEFAULT_SUBJECT, displayContext, pageContext); %>
										</td>
										<td>
											The subject of the test mail.
										</td>
									</tr>
									<tr>
										<td>
											<b>
												HTML:
											</b>
										</td>
										<td>
											&#xA0;
										</td>
										<td>
											<% drawBooleanField(context, PROPERTY_HTML, DEFAULT_HTML, displayContext, pageContext); %>
										</td>
										<td>
											Flag whether the content is HTML.
										</td>
									</tr>
									<tr>
										<td>
											<b>
												Content:
											</b>
										</td>
										<td>
											&#xA0;
										</td>
										<td>
											<% drawField(context, true, PROPERTY_CONTENT, DEFAULT_CONTENT, displayContext, pageContext); %>
										</td>
										<td>
											The content of the test mail.
										</td>
									</tr>
								</table>

								<p>
									<%
									if (RUN_BUTTON != null) {
										%>
										&#xA0;
										<input name="SUBMIT"
											type="submit"
											value="<%=RUN_BUTTON%>"
										/>
										<%
									}
									if (SIMULATE_BUTTON != null) {
										%>
										&#xA0;
										<input name="SIMULATE"
											type="submit"
											value="<%=SIMULATE_BUTTON%>"
										/>
										<%
									}
									%>
								</p>
							</td>
						</tr>
					</table>
				</form>
				<%
			}
			%>
			<br/>
		</basic:access>
	</layout:body>
</layout:html>