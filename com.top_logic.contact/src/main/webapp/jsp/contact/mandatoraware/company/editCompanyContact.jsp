<%@page import="com.top_logic.contact.layout.company.EditCompanyContactComponent"
%><%@page import="com.top_logic.contact.layout.person.EditPersonContactComponent"
%><%@page language="java"
contentType="text/html;charset=ISO-8859-1"
extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.form.component.FormComponent"
%><%@page import="com.top_logic.knowledge.wrap.Wrapper"
%><%@page import="com.top_logic.contact.business.CompanyContact"
%><%@page import="com.top_logic.contact.mandatoraware.COSContactConstants"
%><%@page import="com.top_logic.knowledge.wrap.WrapperAccessor"
%><%@page import="com.top_logic.contact.business.PersonContact"
%><%@page import="java.util.Arrays"
%><%@page import="java.util.*,com.top_logic.basic.xml.TagWriter,com.top_logic.contact.business.AddressHolder,com.top_logic.basic.Logger"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="meta"     prefix="meta"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<meta:formPage titleAttribute="<%=CompanyContact.NAME_ATTRIBUTE%>">
			<%
			FormComponent   theComponent    = (FormComponent) MainLayout.getComponent(pageContext);
			Wrapper      theModel        = (Wrapper) theComponent.getModel();
			Set             theSet          = new HashSet();
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
								<meta:label name="<%=CompanyContact.NAME_ATTRIBUTE %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%=CompanyContact.NAME_ATTRIBUTE %>"/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%=AddressHolder.STREET %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%=AddressHolder.STREET %>"/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= AddressHolder.ZIP_CODE %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%= AddressHolder.ZIP_CODE %>"/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= AddressHolder.CITY %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%= AddressHolder.CITY %>"/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= AddressHolder.COUNTRY %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%= AddressHolder.COUNTRY %>"/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= AddressHolder.STATE %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%= AddressHolder.STATE  %>"/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= AddressHolder.PHONE %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%= AddressHolder.PHONE %>"/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= COSContactConstants.ATTRIBUTE_VOLUME %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%= COSContactConstants.ATTRIBUTE_VOLUME %>"/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= COSContactConstants.ATTRIBUTE_CURRENCY %>"
									colon="true"
								/>
							</td>
							<td class="content">
								<meta:attribute name="<%= COSContactConstants.ATTRIBUTE_CURRENCY %>"/>
							</td>
						</tr>
					</table>
				</basic:fieldset>
				<basic:fieldset titleKeySuffix="<%=CompanyContact.ATTRIBUTE_STAFF%>">
					<meta:table name="<%=CompanyContact.ATTRIBUTE_STAFF%>"/>
				</basic:fieldset>
				<meta:attributes
					exclude="<%=theSet %>"
					legend="additional"
				/>
			</meta:group>
		</meta:formPage>
	</layout:body>
</layout:html>