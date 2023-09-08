<%@page import="com.top_logic.knowledge.wrap.WrapperAccessor"
%><%@page import="com.top_logic.layout.form.component.EditComponent"
%><%@page import="com.top_logic.layout.basic.ResourceRenderer"
%><%@page import="com.top_logic.contact.business.PersonContact"
%><%@page extends="com.top_logic.util.TopLogicJspBase"
%><%@page import=  "com.top_logic.mig.html.layout.MainLayout,
com.top_logic.layout.form.component.FormComponent,
com.top_logic.layout.form.model.FormContext,com.top_logic.model.TLClass,com.top_logic.model.TLStructuredTypePart,com.top_logic.knowledge.wrap.Wrapper,
com.top_logic.contact.layout.person.EditPersonContactComponent,
com.top_logic.basic.Logger,
com.top_logic.basic.xml.TagWriter"
%><%@page import="com.top_logic.contact.business.AddressHolder"
%><%@page import="java.util.HashSet"
%><%@page import="java.util.Set"
%><%@page import="com.top_logic.knowledge.wrap.person.Person"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="meta"     prefix="meta"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:formPage>
			<%
			EditComponent theComponent = (EditComponent) MainLayout.getComponent(pageContext);
			ResourceRenderer personFieldRenderer = theComponent.isInEditMode() ? ResourceRenderer.NO_LINK_INSTANCE : ResourceRenderer.INSTANCE;
			FormContext theContext = theComponent.getFormContext();
			%>
			<meta:group>
				<form:groupCell titleKeySuffix="basicAttributes">
					<form:descriptionContainer>
						<meta:inputCell name="<%=PersonContact.ATT_TITLE%>"/>
						<meta:inputCell name="<%=PersonContact.ATT_FIRSTNAME%>"/>
						<meta:inputCell name="<%=PersonContact.NAME_ATTRIBUTE%>"/>
					</form:descriptionContainer>

					<form:descriptionContainer>
						<form:descriptionCell>
							<form:description>
								<form:label name="<%=EditPersonContactComponent.FIELD_PERSON%>"/>
								<form:error name="<%=EditPersonContactComponent.FIELD_PERSON%>"/>
							</form:description>
							<form:popup name="<%=EditPersonContactComponent.FIELD_PERSON%>"
								renderer="<%=personFieldRenderer%>"
							/>
						</form:descriptionCell>
						<%if (theContext.hasMember(EditPersonContactComponent.PARAM_REPRESENTATIVES)){%>
							<form:descriptionCell>
								<form:description>
									<form:label name="<%=EditPersonContactComponent.PARAM_REPRESENTATIVES%>"
										colon="true"
									/>
									<form:error name="<%=EditPersonContactComponent.PARAM_REPRESENTATIVES%>"/>
								</form:description>
								<form:popup name="<%=EditPersonContactComponent.PARAM_REPRESENTATIVES%>"/>
							</form:descriptionCell>
						<%}%>
						<meta:inputCell name="<%=PersonContact.ATT_POSITION%>"/>
					</form:descriptionContainer>
				</form:groupCell>

				<form:groupCell titleKeySuffix="contactAttributes">
					<form:descriptionContainer>
						<meta:inputCell name="<%=PersonContact.ATT_PHONE_MOBILE%>"/>
						<meta:inputCell name="<%=PersonContact.ATT_PHONE_OFFICE%>"/>
						<meta:inputCell name="<%=PersonContact.ATT_PHONE_PRIVATE%>"/>
					</form:descriptionContainer>

					<form:descriptionContainer>
						<meta:inputCell name="<%=PersonContact.ATT_BOSS%>"/>
						<meta:inputCell name="<%=PersonContact.ATT_COMPANY%>"/>
						<meta:inputCell name="<%=PersonContact.ATT_FAX%>"/>
					</form:descriptionContainer>

					<meta:inputCell name="<%=PersonContact.ATT_MAIL%>"/>

					<meta:input name="<%=PersonContact.REMARKS_ATTRIBUTE %>"
						textareaCols="116"
						textareaRows="5"
						wholeLine="true"
					/>
				</form:groupCell>
			</meta:group>

			<form:ifExists name="<%=EditPersonContactComponent.PARAM_CO_WORKER %>">
				<form:groupCell titleKeySuffix="coworker">
					<form:cell wholeLine="true">
						<form:table name="<%=EditPersonContactComponent.PARAM_CO_WORKER %>"
							initialSortColumn="1"
						/>
					</form:cell>
				</form:groupCell>
			</form:ifExists>
			<form:ifExists name="<%=EditPersonContactComponent.PARAM_REPRESENTATIVES_FOR %>">
				<form:groupCell titleKeySuffix="representativefor">
					<form:cell wholeLine="true">
						<form:table name="<%=EditPersonContactComponent.PARAM_REPRESENTATIVES_FOR %>"
							initialSortColumn="1"
						/>
					</form:cell>
				</form:groupCell>
			</form:ifExists>
		</form:formPage>
	</layout:body>
</layout:html>