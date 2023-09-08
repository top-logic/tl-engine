<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform"   prefix="form"
%><%@page import="com.top_logic.tool.export.ExportTableComponent"
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:formPage
			ignoreModel="true"
			image="theme:ICONS_DOWNLOAD60"
			subtitleKeySuffix="message"
			titleKeySuffix="title"
		>
			<form:tableview name="exports"/>
		</form:formPage>
	</layout:body>
</layout:html>