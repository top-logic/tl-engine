<%@page import="com.top_logic.contact.business.AddressHolder"
%><%@page import="com.top_logic.contact.layout.company.EditCompanyContactComponent"
%><%@page language="java" contentType="text/html;charset=ISO-8859-1" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.contact.business.CompanyContact"
%><%@page import="com.top_logic.element.meta.form.component.EditAttributedComponent"
%><%@page import="java.util.Set"
%><%@page import="java.util.HashSet"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="meta"     prefix="meta"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<meta:formPage titleAttribute="<%=CompanyContact.NAME_ATTRIBUTE %>">
			<meta:group>
				<form:groupCell titleKeySuffix="basicAttributes">
					<form:descriptionContainer>
						<meta:inputCell name="<%=CompanyContact.ATT_SUPPLIER%>"
							labelFirst="false"
						/>
						<meta:inputCell name="<%=CompanyContact.FKEY_ATTRIBUTE%>"/>
					</form:descriptionContainer>

					<form:descriptionContainer>
						<meta:inputCell name="<%=CompanyContact.ATT_CLIENT%>"
							labelFirst="false"
						/>
						<meta:inputCell name="<%=CompanyContact.FKEY2_ATTRIBUTE%>"/>
					</form:descriptionContainer>
				</form:groupCell>

				<form:groupCell titleKeySuffix="contactAttributes">
					<form:descriptionContainer>
						<meta:inputCell name="<%=AddressHolder.STREET%>"/>
						<meta:inputCell name="<%=AddressHolder.ZIP_CODE%>"/>
						<meta:inputCell name="<%=AddressHolder.CITY%>"/>
						<meta:inputCell name="<%=AddressHolder.COUNTRY%>"/>
					</form:descriptionContainer>

					<meta:inputCell name="<%=CompanyContact.ATT_DEPARTMENT%>"/>
					<meta:inputCell name="<%=CompanyContact.ATT_SECTOR%>"/>
					<meta:inputCell name="<%=AddressHolder.PHONE%>"/>
					<meta:inputCell name="<%=CompanyContact.ATT_MAIL%>"/>
					<meta:inputCell name="<%=AddressHolder.STATE%>"/>

					<form:descriptionCell wholeLine="true">
						<form:description>
							<meta:label name="<%=CompanyContact.REMARKS_ATTRIBUTE %>"
								colon="true"
							/>
						</form:description>
						<meta:attribute name="<%=CompanyContact.REMARKS_ATTRIBUTE %>"
							textareaCols="116"
							textareaRows="5"
						/>
					</form:descriptionCell>
				</form:groupCell>

				<form:groupCell titleKeySuffix="<%=CompanyContact.ATTRIBUTE_STAFF%>">
					<form:cell wholeLine="true">
						<meta:table name="<%=CompanyContact.ATTRIBUTE_STAFF%>"
							initialSortColumn="1"
						/>
					</form:cell>
				</form:groupCell>
			</meta:group>
		</meta:formPage>
	</layout:body>
</layout:html>