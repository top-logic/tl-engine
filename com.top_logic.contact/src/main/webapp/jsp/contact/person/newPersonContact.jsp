<%@page import="com.top_logic.model.util.TLModelUtil"
%><%@page import="com.top_logic.contact.business.ContactFactory"
%><%@page import="com.top_logic.contact.business.PersonContact"
%><%@page language="java"
contentType="text/html;charset=ISO-8859-1"
extends="com.top_logic.util.TopLogicJspBase"
buffer="none"
autoFlush="true"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="basic"    prefix="basic"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
		
		<basic:cssLink/>
	</layout:head>
	<layout:body>
		<form:formPage
			titleKeySuffix="title"
			type="<%= TLModelUtil.findType(ContactFactory.STRUCTURE_NAME, ContactFactory.PERSON_TYPE) %>"
		>
			<form:inputCell name="<%=PersonContact.FIRST_NAME%>"/>
			<form:inputCell name="<%=PersonContact.NAME_ATTRIBUTE%>"/>
		</form:formPage>
	</layout:body>
</layout:html>