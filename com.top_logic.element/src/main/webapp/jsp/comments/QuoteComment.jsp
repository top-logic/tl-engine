<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head>
	</layout:head>
	<layout:body>
		<form:formPage
			actionImage="theme:ICONS_QUOTE48"
			image="theme:ICONS_COMMENT60"
			subtitleKeySuffix="quoteCommentMessage"
			titleMessageKeySuffix="quoteCommentTitle"
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