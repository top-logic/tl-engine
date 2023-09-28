<%@page import="com.top_logic.basic.col.TypedAnnotatable"
%><%@page extends="com.top_logic.util.MaintenanceJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.basic.col.TypedAnnotatable.Property"
%><%@page import="com.top_logic.layout.buttonbar.Icons"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"%>
<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>

<%-- Maintenance template version: 1.2 (1.6) --%>
<%!
// JSP page configuration:

// XXX_BUTTON = null: no xxx button
// RUN_BUTTON = null && SIMULATE_BUTTON = null: run automatic
private static final String RUN_BUTTON = "Create";
private static final String SIMULATE_BUTTON = "Simulate";
private static final String CLEAR_BUTTON = "Reset values";
private static final String REFRESH_BUTTON = null;
private static final String RESTART_LINK = "Reload page";
{
	LOG_PRINTS = true;
	LOG_TIME = true;
	WAITING_MESSAGE = "Running...";
	SIM_WAITING_MESSAGE = "Simulating...";
	USE_WAITING_ANI = true;
	TITLE = "CreateDemoUser";
	DESCRIPTION = "This page creates the selected ammount of dummy persons and dummy person contacts.";
}

private static final Integer DEFAULT_PERSON_COUNT = 0;
private static final Integer DEFAULT_CONTACT_COUNT = 0;
private static final String DEFAULT_PERSON_LOGIN_NAME_PREFIX = "demo_";
private static final String DEFAULT_PERSON_FIRST_NAME_PREFIX = "Demo_";
private static final String DEFAULT_PERSON_LAST_NAME_PREFIX = "User_";
private static final String DEFAULT_CONTACT_FIRST_NAME_PREFIX = "Dummy_";
private static final String DEFAULT_CONTACT_LAST_NAME_PREFIX = "Kontakt_";

private static final Property<Integer> PROPERTY_PERSON_COUNT = TypedAnnotatable.property(Integer.class, "ADMINJSP_CreateDemoUser_PersonCount", DEFAULT_PERSON_COUNT);
private static final Property<Integer> PROPERTY_CONTACT_COUNT = TypedAnnotatable.property(Integer.class, "ADMINJSP_CreateDemoUser_ContactCount", DEFAULT_CONTACT_COUNT);
private static final Property<String> PROPERTY_PERSON_LOGIN_NAME_PREFIX = TypedAnnotatable.property(String.class, "ADMINJSP_CreateDemoUser_PersonLoginNamePrefix", DEFAULT_PERSON_LOGIN_NAME_PREFIX);
private static final Property<String> PROPERTY_PERSON_FIRST_NAME_PREFIX = TypedAnnotatable.property(String.class, "ADMINJSP_CreateDemoUser_PersonFirstNamePrefix", DEFAULT_PERSON_FIRST_NAME_PREFIX);
private static final Property<String> PROPERTY_PERSON_LAST_NAME_PREFIX = TypedAnnotatable.property(String.class, "ADMINJSP_CreateDemoUser_PersonLastNamePrefix", DEFAULT_PERSON_LAST_NAME_PREFIX);
private static final Property<String> PROPERTY_CONTACT_FIRST_NAME_PREFIX = TypedAnnotatable.property(String.class, "ADMINJSP_CreateDemoUser_ContactFirstNamePrefix", DEFAULT_CONTACT_FIRST_NAME_PREFIX);
private static final Property<String> PROPERTY_CONTACT_LAST_NAME_PREFIX = TypedAnnotatable.property(String.class, "ADMINJSP_CreateDemoUser_ContactLastNamePrefix", DEFAULT_CONTACT_LAST_NAME_PREFIX);

private static final int MIN_INT = 0;
private static final int MAX_INT = 10000;
%>

<%-- Because of compatibility to IE 6, please don't insert import statements here but further below. --%>
<layout:html>
	<layout:head>
		<title>
			<%=TITLE%>
		</title>
		<meta
			content="text/html; charset=iso-8859-1"
			http-equiv="Content-Type"
		/>
		<basic:cssLink/>
		<basic:script>
			services.ajax.ignoreTLAttributes = false;
		</basic:script>
	</layout:head>
	<%-- Insert import statements here. --%>
	<%@page import="com.top_logic.base.security.attributes.PersonAttributes"
	%><%@page import="java.util.Map"
	%><%@page import="com.top_logic.basic.ArrayUtil"
	%><%@page import="com.top_logic.layout.form.template.TextInputFormFieldControlProvider"
	%><%@page import="com.top_logic.layout.form.template.ValueWithErrorControlProvider"
	%><%@page import="com.top_logic.layout.Control"
	%><%@page import="com.top_logic.layout.form.constraints.IntRangeConstraint"
	%><%@page import="com.top_logic.layout.form.ValueListener"
	%><%@page import="com.top_logic.layout.form.model.FormFactory"
	%><%@page import="com.top_logic.layout.form.model.FormContext"
	%><%@page import="com.top_logic.layout.form.FormField"
	%><%@page import="com.top_logic.gui.ThemeFactory"
	%><%@page import="com.top_logic.contact.business.PersonContact"
	%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
	%><%@page import="com.top_logic.layout.DisplayContext"
	%><%@page import="com.top_logic.mig.html.layout.MainLayout"
	%><%@page import="com.top_logic.mig.html.layout.LayoutComponent"
	%><%@page import="com.top_logic.tool.boundsec.CommandHandlerFactory"
	%><%@page import="com.top_logic.contact.layout.person.PersonContactCreateHandler"
	%><%@page import="com.top_logic.knowledge.gui.layout.person.NewPersonCommandHandler"
	%><%@page import="java.util.Iterator"
	%><%@page import="com.top_logic.basic.CollectionUtil"
	%><%@page import="java.util.Set"
	%><%@page import="java.util.List"
	%><%@page import="com.top_logic.contact.business.ContactFactory"
	%><%@page import="com.top_logic.knowledge.wrap.person.Person"
	%><%@page import="com.top_logic.knowledge.wrap.person.PersonManager"
	%><%@page import="com.top_logic.basic.col.Mapping"
	%><%@page import="com.top_logic.basic.col.Mappings"
	%><%@page import="com.top_logic.basic.StringServices"
	%><%@page import="com.top_logic.util.Utils"
	%><%@page import="com.top_logic.util.TLContext"
	%><%@page import="com.top_logic.basic.util.NumberUtil"
	%><%@page import="com.top_logic.basic.DebugHelper"
	%><%@page import="com.top_logic.knowledge.service.KnowledgeBaseFactory"
	%><%@page import="com.top_logic.basic.xml.TagUtil"
	%><%@page import="com.top_logic.knowledge.service.KnowledgeBase"
	%><%@page import="com.top_logic.basic.Logger"
	%><%@page import="com.top_logic.layout.URLBuilder"
	%><%@page import="com.top_logic.basic.xml.TagWriter"
	%><%@page import="com.top_logic.knowledge.wrap.person.infouser.LicenceOverviewUtil"%>
	
	<%!private String formatSuffix(int suffix) {
		return (suffix < 100 ? "0" : "") + (suffix < 10 ? "0" : "") + suffix;
	}
	
	private int getInt(Property<Integer> property, String label) throws Exception {
		int value = TLContext.getContext().get(property).intValue();
		if (value < MIN_INT || value > MAX_INT) {
			print(label + " (invalid): " + value);
			value = 0;
		}
		else {
			print(label + ": " + value);
		}
		return value;
	}
	
	@Override
	protected void doWork(JspWriter out, boolean simulate, HttpServletRequest request) throws Exception {
		print("Creating persons and contacts...");
		print(); print();
		
		try {
			int personCount = getInt(PROPERTY_PERSON_COUNT, "Persons to create");
			String pLoginNamePrefix = TLContext.getContext().get(PROPERTY_PERSON_LOGIN_NAME_PREFIX);
			if (StringServices.isEmpty(pLoginNamePrefix)) pLoginNamePrefix = DEFAULT_PERSON_LOGIN_NAME_PREFIX;
			print("Person login name prefix: " + pLoginNamePrefix);
			String pFistNamePrefix = Utils.getStringValue(TLContext.getContext().get(PROPERTY_PERSON_FIRST_NAME_PREFIX));
			if (StringServices.isEmpty(pFistNamePrefix)) pFistNamePrefix = DEFAULT_PERSON_FIRST_NAME_PREFIX;
			print("Person first name prefix: " + pFistNamePrefix);
			String pLastNamePrefix = Utils.getStringValue(TLContext.getContext().get(PROPERTY_PERSON_LAST_NAME_PREFIX));
			if (StringServices.isEmpty(pLastNamePrefix)) pLastNamePrefix = DEFAULT_PERSON_LAST_NAME_PREFIX;
			print("Person last name prefix: " + pLastNamePrefix);
			print();
			
			int contactCount = getInt(PROPERTY_CONTACT_COUNT, "Contacts to create");
			String cFistNamePrefix = Utils.getStringValue(TLContext.getContext().get(PROPERTY_CONTACT_FIRST_NAME_PREFIX));
			if (StringServices.isEmpty(cFistNamePrefix)) cFistNamePrefix = DEFAULT_CONTACT_FIRST_NAME_PREFIX;
			print("Contact first name prefix: " + cFistNamePrefix);
			String cLastNamePrefix = Utils.getStringValue(TLContext.getContext().get(PROPERTY_CONTACT_LAST_NAME_PREFIX));
			if (StringServices.isEmpty(cLastNamePrefix)) cLastNamePrefix = DEFAULT_CONTACT_LAST_NAME_PREFIX;
			print("Contact last name prefix: " + cLastNamePrefix);
			
			print(); print();
			
			// Existing persons
			Set<String> loginNames = Mappings.mapIntoSet(new Mapping<Person, String>() {
					@Override
					public String map(Person person) {
						return person.getName();
					}
			}, PersonManager.getManager().getAllPersonsList());
			
			// Existing contacts
			List<PersonContact> contacts = ContactFactory.getInstance().getAllContacts(ContactFactory.PERSON_TYPE);
			Set<String> contactNames = Mappings.mapIntoSet(new Mapping<PersonContact, String>() {
					@Override
					public String map(PersonContact contact) {
						return contact.getFirstName() + '.' + contact.getName();
					}
			}, contacts);
			
			// Existing mails
			Set<String> emails = CollectionUtil.newSet(contacts.size());
			for (Iterator<PersonContact> it = contacts.iterator(); it.hasNext();) {
				String mail = StringServices.trim(it.next().getEMail());
				if (!StringServices.isEmpty(mail)) {
					emails.add(mail.toLowerCase());
				}
			}
			
			// Create persons
			if (personCount > 0) {
				print("Creating persons...");
			}
			NewPersonCommandHandler personHandler = (NewPersonCommandHandler)CommandHandlerFactory.getInstance().getHandler(NewPersonCommandHandler.COMMAND_ID);
			int suffix = 0;
			for (int i = 0; i < personCount; i++) {
				String formSuffix = formatSuffix(++suffix);
				String loginName = pLoginNamePrefix + formSuffix;
				while (loginNames.contains(loginName)) {
					formSuffix = formatSuffix(++suffix);
					loginName = pLoginNamePrefix + formSuffix;
				}
				if (!simulate) {
					String genMail = (loginName + ".generated@dummymail.com").toLowerCase();
					if (emails.contains(genMail)) {
						int j = 1;
						do {
							genMail = (loginName + "." + (j++) + ".generated@dummymail.com").toLowerCase();
						}
						while (emails.contains(genMail));
					}
					Person person = personHandler.createPerson(loginName, pFistNamePrefix + formSuffix, pLastNamePrefix + formSuffix, null, "dbSecurity", null, false);
					Person.userOrNull(person).setAttributeValue(PersonAttributes.MAIL_NAME, genMail);
					PersonManager.getManager().handleRefreshPerson(person);
					emails.add(genMail);
				}
			}
			
			// Create contacts
			if (contactCount > 0) {
				print(); print("Creating contacts...");
			}
			PersonContactCreateHandler contactHandler = (PersonContactCreateHandler)CommandHandlerFactory.getInstance().getHandler(PersonContactCreateHandler.COMMAND_ID);
			suffix = 0;
			for (int i = 0; i < contactCount; i++) {
				String formSuffix = formatSuffix(++suffix);
				String contactName = cFistNamePrefix + formSuffix + '.' + cLastNamePrefix + formSuffix;
				while (contactNames.contains(contactName)) {
					formSuffix = formatSuffix(++suffix);
					contactName = cFistNamePrefix + formSuffix + '.' + cLastNamePrefix + formSuffix;
				}
				String genMail = (contactName + ".generated@dummymail.com").toLowerCase();
				if (emails.contains(genMail)) {
					int j = 1;
					do {
						genMail = (contactName + "." + (j++) + ".generated@dummymail.com").toLowerCase();
					}
					while (emails.contains(genMail));
				}
				PersonContact contact = contactHandler.createPersonContact(cLastNamePrefix + formSuffix, cFistNamePrefix + formSuffix);
				contact.setValue(PersonContact.EMAIL, genMail);
				emails.add(genMail);
			}
			
			
			print(); print();
			print("Done. Created " + personCount + " persons and " + contactCount + " contacts. Committing...");
			commit(simulate);
		}
		catch (Exception e) {
			printError("Error while processing maintenance work.");
			printError(e.toString(), e);
			rollback();
		}
	}%>
	<layout:body>
		<basic:access>
			<%
			LayoutComponent component = MainLayout.getComponent(pageContext);
			DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(pageContext);
			
			// waiting slider
			if (USE_WAITING_ANI && (request.getParameter("SUBMIT") != null || request.getParameter("SIMULATE") != null)) {
				boolean doSimulate = request.getParameter("SIMULATE") != null;
				%>
				<div id="progressDiv"
					class="fullProgressVisible"
				>
					<table
						height="100%"
						width="100%"
					>
						<tr>
							<td align="center">
								<b>
									<%=doSimulate ? SIM_WAITING_MESSAGE : WAITING_MESSAGE%>
								</b>
								<br/>
								<br/>
								<% if (USE_WAITING_ANI) {
									%>
									<basic:image
										border="0"
										cssClass="fullProgress"
										icon="<%= ThemeFactory.getTheme().getValue(Icons.SLIDER_IMG) %>"
									/>
									<% } else { %>
									&#xA0;
								<% } %>
							</td>
						</tr>
					</table>
				</div>
				<basic:script>
					self.location.href = "<%=addParameters(component.getComponentURL(displayContext), request).getURL()%>";
				</basic:script>
				<%
			}
			else {
				%>
				<h1>
					<%=TITLE%>
				</h1>
				<p>
					<%=DESCRIPTION%>
				</p>
				<%
			}
			
			// clear history
			if (request.getParameter("CLEAR") != null) {
				TLContext context = TLContext.getContext();
				context.reset(PROPERTY_PERSON_COUNT);
				context.reset(PROPERTY_CONTACT_COUNT);
				context.reset(PROPERTY_PERSON_LOGIN_NAME_PREFIX);
				context.reset(PROPERTY_PERSON_FIRST_NAME_PREFIX);
				context.reset(PROPERTY_PERSON_LAST_NAME_PREFIX);
				context.reset(PROPERTY_CONTACT_FIRST_NAME_PREFIX);
				context.reset(PROPERTY_CONTACT_LAST_NAME_PREFIX);
			}
			
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
										<% String name = doSimulate ? "SIMULATE" : "SUBMIT"; %>
										&#xA0;
										<input name="<%=name%>"
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
								private static class FieldListener<T> implements ValueListener {
									
									private final Property<T> propertyName;
									
									public FieldListener(Property<T> propertyName) {
										this.propertyName = propertyName;
									}
									
									@Override
									public void valueChanged(FormField formField, Object oldValue, Object newValue) {
										TLContext.getContext().set(propertyName, (T)newValue);
									}
								}
								
								private <T> void drawField(FormContext context, Property<T> propertyName,
									DisplayContext displayContext, PageContext pageContext) throws Exception {
									boolean intField = propertyName.getType().equals(Integer.class);
									FormField field = intField ? FormFactory.newIntField(propertyName.getName()) : FormFactory.newStringField(propertyName.getName());
									field.setLabel(propertyName.getName());
									field.addValueListener(new FieldListener(propertyName));
									if (intField) {
										field.addConstraint(new IntRangeConstraint(MIN_INT, MAX_INT));
									}
									Object value = TLContext.getContext().get(propertyName);
									field.initializeField(value);
									field.check();
									context.addMember(field);
									Control control = ValueWithErrorControlProvider.newInstance(new TextInputFormFieldControlProvider(1, intField ? 4 : 30, false)).createControl(field, null);
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
												Persons to create:
											</b>
										</td>
										<td>
											&#xA0;
										</td>
										<td>
											<% drawField(context, PROPERTY_PERSON_COUNT, displayContext, pageContext); %>
										</td>
									</tr>
									<tr>
										<td>
											<b>
												Person login name prefix:
											</b>
										</td>
										<td>
											&#xA0;
										</td>
										<td>
											<% drawField(context, PROPERTY_PERSON_LOGIN_NAME_PREFIX, displayContext, pageContext); %>
										</td>
									</tr>
									<tr>
										<td>
											<b>
												Person first name prefix:
											</b>
										</td>
										<td>
											&#xA0;
										</td>
										<td>
											<% drawField(context, PROPERTY_PERSON_FIRST_NAME_PREFIX, displayContext, pageContext); %>
										</td>
									</tr>
									<tr>
										<td>
											<b>
												Person last name prefix:
											</b>
										</td>
										<td>
											&#xA0;
										</td>
										<td>
											<% drawField(context, PROPERTY_PERSON_LAST_NAME_PREFIX, displayContext, pageContext); %>
										</td>
									</tr>
									<tr>
										<td colspan="3">
											&#xA0;
										</td>
									</tr>
									<tr>
										<td>
											<b>
												Contacts to create:
											</b>
										</td>
										<td>
											&#xA0;
										</td>
										<td>
											<% drawField(context, PROPERTY_CONTACT_COUNT, displayContext, pageContext); %>
										</td>
									</tr>
									<tr>
										<td>
											<b>
												Contact first name prefix:
											</b>
										</td>
										<td>
											&#xA0;
										</td>
										<td>
											<% drawField(context, PROPERTY_CONTACT_FIRST_NAME_PREFIX, displayContext, pageContext); %>
										</td>
									</tr>
									<tr>
										<td>
											<b>
												Contact last name prefix:
											</b>
										</td>
										<td>
											&#xA0;
										</td>
										<td>
											<% drawField(context, PROPERTY_CONTACT_LAST_NAME_PREFIX, displayContext, pageContext); %>
										</td>
									</tr>
									<tr>
										<td colspan="3">
											&#xA0;
										</td>
									</tr>
								</table>

								<hr/>
								<%
								TagWriter tagWriter = MainLayout.getTagWriter(pageContext);
								LicenceOverviewUtil.INSTANCE.writeLicenceOverview(displayContext, tagWriter);
								tagWriter.flushBuffer();
								%>
								<hr/>

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
									if (CLEAR_BUTTON != null) {
										%>
										&#xA0;
										<input name="CLEAR"
											type="submit"
											value="<%=CLEAR_BUTTON%>"
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
<%!
private URLBuilder addParameters(URLBuilder url, HttpServletRequest aRequest) {
	boolean runParam = false;
	Map theParameters = aRequest.getParameterMap();
	Iterator it = theParameters.entrySet().iterator();
	while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		String key = (String)entry.getKey();
		String value = (String)ArrayUtil.getFirst((String[])entry.getValue());
		if ("SUBMIT".equals(key)) {
			url.appendParameter("DO_SUBMIT", value);
			runParam = true;
		}
		else if ("SIMULATE".equals(key)) {
			url.appendParameter("DO_SIMULATE", value);
			runParam = true;
		}
		else {
			url.appendParameter((String)entry.getKey(), value);
		}
	}
	if (!runParam) {
		url.appendParameter("DO_SUBMIT", "SUBMIT");
	}
	return url;
}
%>