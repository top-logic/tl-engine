<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform"   prefix="form"
%><layout:html>
	<layout:head>
	</layout:head>
	<layout:body>
		<form:form>
			<table class="frm"
				summary="generated form"
			>
				<tr>
					<th colspan="2">
						<form:resource key="title"/>
					</th>
				</tr>
				<tr>
					<td colspan="2">
						&#xA0;
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<form:resource key="message"/>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						&#xA0;
					</td>
				</tr>
				<tr>
					<td class="label">
						<form:label name="name"/>
						:
					</td>
					<td class="content">
						<form:input name="name"
							columns="30"
						/>
						<form:error name="name"/>
					</td>
				</tr>
				<tr>
					<td colspan="2">
						<%@include file="/jsp/element/metaattributes/PublishForm.inc" %>
					</td>
				</tr>
			</table>
		</form:form>
	</layout:body>
</layout:html>