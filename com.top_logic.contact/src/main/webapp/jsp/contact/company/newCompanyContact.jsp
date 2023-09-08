<%@page import="com.top_logic.contact.business.CompanyContact"
%><%@page language="java" contentType="text/html;charset=ISO-8859-1"
extends="com.top_logic.util.TopLogicJspBase"
buffer="none" autoFlush="true"
%><%@page import="com.top_logic.contact.business.AddressHolder"
%><%@page import="com.top_logic.model.TLClass"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.contact.layout.company.CreateCompanyContactComponent"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="basic"    prefix="basic"
%><%@taglib uri="meta"     prefix="meta"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
		<basic:cssLink/>
	</layout:head>
	<layout:body>
		<%
		CreateCompanyContactComponent   theComponent    = (CreateCompanyContactComponent) MainLayout.getComponent(pageContext);
		TLClass     theMetaElement      = theComponent.getMetaElement();
		%>
		<meta:formPage titleAttribute="<%=CompanyContact.NAME_ATTRIBUTE %>">
			<meta:group object="<%= theMetaElement %>">
				<form:groupCell
					cssClass="line"
					titleKeySuffix="basicAttributes"
				>
					<meta:inputCell name="<%=AddressHolder.STREET%>"/>
					<meta:inputCell name="<%=AddressHolder.ZIP_CODE%>"/>
					<meta:inputCell name="<%=AddressHolder.CITY%>"/>
					<meta:inputCell name="<%=AddressHolder.COUNTRY%>"/>
					<meta:inputCell name="<%=AddressHolder.STATE%>"/>
					<meta:inputCell name="<%=AddressHolder.PHONE%>"/>
				</form:groupCell>
				<meta:attributes legend="additional"/>
			</meta:group>
		</meta:formPage>
	</layout:body>
</layout:html>