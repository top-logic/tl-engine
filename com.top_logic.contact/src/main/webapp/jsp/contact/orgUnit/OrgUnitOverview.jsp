<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.knowledge.wrap.WrapperAccessor"
%><%@page import="com.top_logic.contact.business.PersonContact"
%><%@page import="com.top_logic.contact.orgunit.OrgUnitAllConstants"
%><%@page import="com.top_logic.contact.orgunit.OrgUnitBase"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="meta"     prefix="meta"
%><%@taglib uri="ajaxform" prefix="form"
%><%
String theColumns = PersonContact.NAME_ATTRIBUTE + ' ' + PersonContact.ATT_MAIL + ' ' +
PersonContact.ATT_PHONE_OFFICE + ' ' + PersonContact.ATT_PHONE_MOBILE;
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<meta:formPage titleAttribute="<%= OrgUnitAllConstants.ATTRIBUTE_NAME %>">
			<meta:group>
				<form:groupCell titleKeySuffix="basicAttributes">
					<meta:exists name="<%= OrgUnitBase.ATTRIBUTE_ORG_ID %>">
						<form:cell wholeLine="true">
							<meta:inputCell name="<%=OrgUnitBase.ATTRIBUTE_ORG_ID%>"
								colon="true"
							/>
							<meta:inputCell name="<%=OrgUnitBase.ATTRIBUTE_BOSS%>"
								colon="true"
							/>
						</form:cell>
					</meta:exists>
					<meta:exists name="<%=OrgUnitBase.ATTRIBUTE_ORG_ID %>">
						<form:cell wholeLine="true">
							<meta:table name="<%=OrgUnitBase.ATTRIBUTE_MEMBER %>"
								columnNames="<%=theColumns %>"
								initialSortColumn="1"
							/>
						</form:cell>
					</meta:exists>
				</form:groupCell>

				<meta:attributes legend="additional"/>
			</meta:group>
		</meta:formPage>
	</layout:body>
</layout:html>