<%@page import="com.top_logic.layout.structure.OrientationAware.Orientation"
%><%@page import="com.top_logic.model.util.TLModelUtil"
%><%@page import="com.top_logic.knowledge.wrap.person.Person"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.gui.Theme"
%><%@page import="com.top_logic.layout.provider.MetaLabelProvider"
%><%@page import="com.top_logic.mig.html.HTMLConstants"
%><%@page import="com.top_logic.gui.ThemeFactory"
%><%@page import="com.top_logic.knowledge.gui.layout.person.PersonGroupComponent"
%><%@page import="com.top_logic.util.Resources"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><%
PersonGroupComponent theComponent = (PersonGroupComponent) MainLayout.getComponent(pageContext);
String               theExplain   = theComponent.isInEditMode() ? "explainEdit" : "explain";
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage
			actionImage="theme:ICONS_PERSON_ASSIGNMENTS48"
			imageTooltipKeySuffix="assignPersonGroups"
			subtitleKeySuffix="<%=theExplain %>"
			titleMessageKeySuffix="groupsOf(user)"
			type="<%= Person.getPersonType() %>"
		>
			<form:groupCell titleKeySuffix="groups">
				<form:choice name="<%=PersonGroupComponent.FORM_MEMBER_GROUP %>"
					orientation="<%=Orientation.VERTICAL %>"
				/>
			</form:groupCell>
		</form:formPage>
	</layout:body>
</layout:html>