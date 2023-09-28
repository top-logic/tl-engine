<%@page extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="java.util.Set"
%><%@page import="java.util.HashSet"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.form.component.FormComponent"
%><%@page import="com.top_logic.knowledge.wrap.Wrapper"
%><%@page import="com.top_logic.contact.mandatoraware.COSPersonContact"
%><%@page import="com.top_logic.layout.Accessor"
%><%@page import="com.top_logic.layout.table.TableRenderer"
%><%@page import="com.top_logic.knowledge.wrap.WrapperAccessor"
%><%@page import="com.top_logic.contact.mandatoraware.layout.COSEditPersonContactComponent"
%><%@page import="com.top_logic.knowledge.wrap.person.Person"
%><%@page import="com.top_logic.contact.business.CompanyContact"
%><%@page import="com.top_logic.contact.business.AddressHolder"
%><%@page import="com.top_logic.contact.business.PersonContact"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="basic"   prefix="basic"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="meta"     prefix="meta"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<%
			FormComponent theComponent = (FormComponent) MainLayout.getComponent(pageContext);
					Wrapper    theModel     = (Wrapper) theComponent.getModel();
					Person        thePerson   = ((PersonContact) theModel).getPerson();
					Set           theSet      = new HashSet();
					Accessor      theAccessor = WrapperAccessor.INSTANCE;
					
					theSet.add(COSPersonContact.PHONE_PRIVATE);
					theSet.add(COSPersonContact.TITLE);
			%>
			<meta:group object="<%=theModel %>">
				<basic:fieldset titleKeySuffix="basicAttributes">
					<table class="formular"
						width="100%"
					>
						<colgroup>
							<col width="0%"/>
							<col width="100%"/>
						</colgroup>
						<tr>
							<td class="label">
								<meta:label name="<%=COSPersonContact.NAME_ATTRIBUTE %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%=COSPersonContact.FIRST_NAME%>"
									inputSize="10"
								/>
								&#xA0;
								<meta:attribute name="<%=COSPersonContact.NAME_ATTRIBUTE %>"
									inputSize="15"
								/>
								<% if (thePerson != null) { %>(
										<form:resource key="user"/>
										:
										<layout:display
											hideImage="true"
											object="<%=thePerson %>"
										/>
								) <% } %>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%=COSPersonContact.POSITION%>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%=COSPersonContact.POSITION%>"/>
							</td>
						</tr>
						<form:ifExists name="<%=COSEditPersonContactComponent.PARAM_REPRESENTATIVES%>">
							<tr>
								<td class="label">
									<form:label name="<%=COSEditPersonContactComponent.PARAM_REPRESENTATIVES%>"/>
									:
								</td>
								<td class="content">
									<form:popup name="<%=COSEditPersonContactComponent.PARAM_REPRESENTATIVES%>"/>
									<form:error name="<%=COSEditPersonContactComponent.PARAM_REPRESENTATIVES%>"
										icon="true"
									/>
								</td>
							</tr>
						</form:ifExists>
						<tr>
							<td class="label">
								<meta:label name="<%=COSPersonContact.BOSS%>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%=COSPersonContact.BOSS%>"/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%=COSPersonContact.ATTRIBUTE_MANDATOR %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%=COSPersonContact.ATTRIBUTE_MANDATOR %>"/>
							</td>
						</tr>
						<tr>
							<td colspan="2">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%=COSPersonContact.EMAIL%>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%=COSPersonContact.EMAIL%>"
									inputSize="30"
								/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%=COSPersonContact.PHONE%>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%=COSPersonContact.PHONE%>"
									inputSize="30"
								/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%=COSPersonContact.PHONE_MOBILE%>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%=COSPersonContact.PHONE_MOBILE%>"
									inputSize="30"
								/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%=COSPersonContact.ATT_FAX %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%=COSPersonContact.ATT_FAX %>"
									inputSize="30"
								/>
							</td>
						</tr>
						<tr>
							<td colspan="2">
								<hr/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%=COSPersonContact.REMARKS_ATTRIBUTE %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%=COSPersonContact.REMARKS_ATTRIBUTE %>"/>
							</td>
						</tr>
					</table>
				</basic:fieldset>
				<meta:attributes
					exclude="<%=theSet %>"
					legend="additional"
				/>
			</meta:group>
			<form:ifExists name="<%=COSEditPersonContactComponent.PARAM_CO_WORKER %>">
				<basic:fieldset titleKeySuffix="coworker">
					<form:table name="<%=COSEditPersonContactComponent.PARAM_CO_WORKER %>"
						accessor="<%=theAccessor %>"
					/>
				</basic:fieldset>
			</form:ifExists>
			<form:ifExists name="<%=COSEditPersonContactComponent.PARAM_LEAD_BUYER %>">
				<basic:fieldset titleKeySuffix="leadBuyerFor">
					<form:table name="<%=COSEditPersonContactComponent.PARAM_LEAD_BUYER %>"
						accessor="<%=theAccessor %>"
					/>
				</basic:fieldset>
			</form:ifExists>
			<form:ifExists name="<%=COSEditPersonContactComponent.PARAM_REPRESENTATIVES_FOR %>">
				<basic:fieldset titleKeySuffix="representativefor">
					<form:table name="<%=COSEditPersonContactComponent.PARAM_REPRESENTATIVES_FOR %>"
						accessor="<%=theAccessor %>"
					/>
				</basic:fieldset>
			</form:ifExists>
		</form:form>
	</layout:body>
</layout:html>