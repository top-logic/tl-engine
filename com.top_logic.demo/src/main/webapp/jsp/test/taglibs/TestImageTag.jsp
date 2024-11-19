<!DOCTYPE html>
<%@page import="com.top_logic.layout.form.tag.Icons"
%><%@page extends="com.top_logic.util.TopLogicJspBase" language="java"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="basic" prefix="basic"
%><layout:html>
	<layout:head/>
	<layout:body>
		<h1>
			Demo for the basic:image tag
		</h1>
		<basic:image
			alt="Direct alternative text"
			icon="<%= Icons.SYSTEMSTATE_GREEN %>"
		/>
		<basic:image
			altKey="demo.imageTag.altText"
			icon="<%= com.top_logic.util.error.Icons.INFO %>"
			tooltipKey="demo.imageTag.tooltip"
		/>
		<basic:image
			altKey="demo.imageTag.altText"
			srcKey="theme:ICONS_DIALOG_QUESTION"
			tooltipKey="demo.imageTag.tooltip"
		/>
	</layout:body>
</layout:html>