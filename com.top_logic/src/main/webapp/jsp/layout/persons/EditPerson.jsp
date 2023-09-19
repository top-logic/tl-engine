<%@page import="com.top_logic.layout.basic.ThemeImage"
%><%@page import="com.top_logic.layout.DisplayContext"
%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="java.util.*,
com.top_logic.mig.html.layout.*,
com.top_logic.knowledge.wrap.person.Person,
com.top_logic.knowledge.wrap.person.PersonManager"
%><%@page import="com.top_logic.knowledge.gui.layout.person.EditPersonComponent"
%><%@page import="com.top_logic.layout.form.FormField"
%><%@page import="com.top_logic.knowledge.gui.layout.person.Icons"
%><%@page import="com.top_logic.base.security.attributes.PersonAttributes"
%><%@page import="com.top_logic.base.security.device.TLSecurityDeviceManager"
%><%@page import="com.top_logic.basic.thread.ThreadContext"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%@taglib uri="basic" prefix="basic"
%><%@page import="com.top_logic.basic.xml.TagWriter"
%><%@page import="com.top_logic.knowledge.wrap.person.infouser.LicenceOverviewUtil"
%><layout:html>
	<layout:head/>
	<%
	EditPersonComponent theComponent = (EditPersonComponent) MainLayout.getComponent(pageContext);
	Person				thePerson    = (Person) theComponent.getModel();
	boolean             isInEdit     = theComponent.isInEditMode();
	%>
	<layout:body>
		<form:form>
			<form:layout type="vertical">
				<form:layout type="horizontal">
					<form:layout type="twoColumns">
						<form:groupCell titleKeySuffix="section.nameData">
							<form:layout type="horizontal">
								<form:layout type="vertical">
									<form:inputCell name="<%=PersonAttributes.USER_NAME%>"
										colon="true"
									>
										<form:input name="<%=PersonAttributes.USER_NAME%>"
											columns="20"
										/>
									</form:inputCell>

									<form:inputCell name="<%=PersonAttributes.TITLE%>"
										colon="true"
									/>

									<form:inputCell name="<%=PersonAttributes.GIVEN_NAME%>"
										colon="true"
									>
										<form:input name="<%=PersonAttributes.GIVEN_NAME%>"
											columns="20"
										/>
									</form:inputCell>

									<form:inputCell name="<%=PersonAttributes.SUR_NAME%>"
										colon="true"
									>
										<form:input name="<%=PersonAttributes.SUR_NAME%>"
											columns="20"
										/>
									</form:inputCell>
								</form:layout>

								<form:cell
									style="padding-left: 20px; vertical-align: top;"
									width="0%"
								>
									<basic:image
										icon="<%= com.top_logic.knowledge.gui.layout.person.Icons.UNKNOWN_PERSON %>"
										width="50"
									/>
								</form:cell>
							</form:layout>
						</form:groupCell>

						<form:groupCell titleKeySuffix="section.contactData">
							<form:inputCell name="<%=PersonAttributes.ORG_UNIT%>"
								colon="true"
							>
								<form:input name="<%=PersonAttributes.ORG_UNIT%>"
									columns="30"
								/>
							</form:inputCell>

							<form:inputCell name="<%=PersonAttributes.MOBILE_NR%>"
								colon="true"
							>
								<form:input name="<%=PersonAttributes.MOBILE_NR%>"
									columns="20"
								/>
							</form:inputCell>

							<form:inputCell name="<%= PersonAttributes.INTERNAL_NR %>"
								colon="true"
							>
								<form:input name="<%= PersonAttributes.INTERNAL_NR%>"
									columns="20"
								/>
							</form:inputCell>

							<form:inputCell name="<%= PersonAttributes.EXTERNAL_NR %>"
								colon="true"
							>
								<form:input name="<%= PersonAttributes.EXTERNAL_NR %>"
									columns="20"
								/>
							</form:inputCell>

							<form:inputCell name="<%= PersonAttributes.PRIVATE_NR %>"
								colon="true"
							>
								<form:input name="<%= PersonAttributes.PRIVATE_NR %>"
									columns="20"
								/>
							</form:inputCell>

							<form:inputCell name="<%= PersonAttributes.MAIL_NAME %>"
								colon="true"
							>
								<form:input name="<%= PersonAttributes.MAIL_NAME %>"
									columns="20"
								/>
							</form:inputCell>

							<form:inputCell name="<%= PersonAttributes.EXTERNAL_MAIL %>"
								colon="true"
							>
								<form:input name="<%= PersonAttributes.EXTERNAL_MAIL %>"
									columns="20"
								/>
							</form:inputCell>
						</form:groupCell>

						<form:groupCell titleKeySuffix="section.personalizationData">
							<form:inputCell name="<%=PersonAttributes.CUSTOMER%>"
								colon="true"
							>
								<form:input name="<%=PersonAttributes.CUSTOMER%>"
									columns="20"
								/>
							</form:inputCell>

							<form:inputCell name="<%=EditPersonComponent.COUNTRY %>"
								colon="true"
							>
								<form:select name="<%=EditPersonComponent.COUNTRY %>"/>
							</form:inputCell>

							<form:inputCell name="<%=EditPersonComponent.LANGUAGE %>"
								colon="true"
							/>
						</form:groupCell>
						<form:groupCell titleKeySuffix="section.securityData">
							<form:inputCell name="<%=PersonAttributes.RESTRICTED_USER%>"
								colon="true"
							/>
							<form:inputCell name="<%=EditPersonComponent.SUPER_USER_FIELD%>"
								colon="true"
							/>
							<%
							if (ThreadContext.isAdmin()) {
													Set<String> allDataDevices = TLSecurityDeviceManager.getInstance().getConfiguredDataAccessDeviceIDs();
													if (!allDataDevices.isEmpty()) {
							%>
									<form:inputCell name="<%=PersonAttributes.DATA_ACCESS_DEVICE_ID%>">
										<form:select name="<%=PersonAttributes.DATA_ACCESS_DEVICE_ID%>"/>
									</form:inputCell>
									<%
									}
									%>
								
								<%
																Set<String> allAuthDevices = TLSecurityDeviceManager.getInstance().getConfiguredAuthenticationDeviceIDs();
																						if (!allAuthDevices.isEmpty()) {
																%>
									<form:inputCell name="<%=PersonAttributes.AUTHENTICATION_DEVICE_ID%>">
										<form:select name="<%=PersonAttributes.AUTHENTICATION_DEVICE_ID%>"/>
									</form:inputCell>
									<%
									}
														}
									%>
							<form:ifExists name="<%=EditPersonComponent.GROUPS_FIELD_NAME%>">
								<form:inputCell name="<%=EditPersonComponent.GROUPS_FIELD_NAME%>"/>
							</form:ifExists>
						</form:groupCell>
					</form:layout>
				</form:layout>
			</form:layout>
			<%
			if(ThreadContext.isAdmin()){
			%>
				<form:groupCell titleKeySuffix="section.licenseData">
					<%
					TagWriter tagWriter = MainLayout.getTagWriter(pageContext);
					DisplayContext context = DefaultDisplayContext.getDisplayContext(pageContext);
					LicenceOverviewUtil.INSTANCE.writeLicenceOverview(context, tagWriter);
					tagWriter.flushBuffer();
					%>
				</form:groupCell>
			<% } //end if superuser%>
		</form:form>
	</layout:body>
</layout:html>