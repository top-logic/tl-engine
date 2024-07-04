<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
	</layout:head>
	<layout:body>
		<form:formPage
			actionImage="theme:ICON_PLUS"
			displayWithoutModel="true"
			image="theme:ICONS_COMMENT60"
			noModelKeySuffix="newCommentTitle"
			subtitleKeySuffix="newCommentMessage"
			titleMessageKeySuffix="newCommentTitle"
		>
			<div style="text-align:center">
				<form:input name="comments"
					columns="90"
					multiLine="true"
					rows="15"
				/>
				&#xA0;
				<form:error name="comments"
					icon="true"
				/>
			</div>
		</form:formPage>
	</layout:body>
</layout:html>