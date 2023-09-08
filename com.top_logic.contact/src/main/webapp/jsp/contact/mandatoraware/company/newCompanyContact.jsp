<%@page import="com.top_logic.contact.business.CompanyContact"
%><%@page language="java"
contentType="text/html;charset=ISO-8859-1"
extends="com.top_logic.util.TopLogicJspBase"
buffer="none"
autoFlush="true"
%><%@page import="com.top_logic.contact.business.AddressHolder"
%><%@page import="com.top_logic.model.TLClass"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.contact.mandatoraware.COSContactConstants"
%><%@page import="com.top_logic.contact.mandatoraware.layout.COSCreateCompanyContactComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="basic"    prefix="basic"
%><%@taglib uri="meta"     prefix="meta"
%><layout:html>
	<layout:head>
		
		<basic:cssLink/>
	</layout:head>
	<layout:body>
		<%
		COSCreateCompanyContactComponent   theComponent    = (COSCreateCompanyContactComponent) MainLayout.getComponent(pageContext);
		TLClass     theMetaElement      = theComponent.getMetaElement();
		%>
		<meta:formPage titleAttribute="<%=CompanyContact.NAME_ATTRIBUTE %>">
			<meta:group object="<%= theMetaElement %>">
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
								<meta:label name="<%= AddressHolder.STREET %>"/>
								:
							</td>
							<td class="content">
								<meta:attribute name="<%= AddressHolder.STREET %>"
									inputSize="30"
								/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= AddressHolder.ZIP_CODE %>"/>
								:
							</td>
							<td class="content">
								<meta:attribute name="<%= AddressHolder.ZIP_CODE %>"
									inputSize="30"
								/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= AddressHolder.CITY%>"/>
								:
							</td>
							<td class="content">
								<meta:attribute name="<%= AddressHolder.CITY %>"
									inputSize="30"
								/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= AddressHolder.PHONE%>"/>
								:
							</td>
							<td class="content">
								<meta:attribute name="<%= AddressHolder.PHONE %>"
									inputSize="30"
								/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= AddressHolder.COUNTRY%>"/>
								:
							</td>
							<td class="content">
								<meta:attribute name="<%= AddressHolder.COUNTRY %>"/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= AddressHolder.STATE%>"/>
								:
							</td>
							<td class="content">
								<meta:attribute name="<%= AddressHolder.STATE %>"
									inputSize="30"
								/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= COSContactConstants.SUPPLIER%>"/>
								:
							</td>
							<td class="content">
								<meta:attribute name="<%= COSContactConstants.SUPPLIER %>"/>
							</td>
						</tr>
						<tr>
							<td class="label">
								<meta:label name="<%= COSContactConstants.CLIENT%>"/>
								:
							</td>
							<td class="content">
								<meta:attribute name="<%= COSContactConstants.CLIENT %>"/>
							</td>
						</tr>
					</table>
				</basic:fieldset>
				<meta:attributes legend="additional"/>
			</meta:group>
		</meta:formPage>
	</layout:body>
</layout:html>