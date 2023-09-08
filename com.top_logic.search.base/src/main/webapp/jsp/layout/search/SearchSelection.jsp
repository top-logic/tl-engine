<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform"   prefix="form"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<layout:body>
		<form:form>
			<table
				align="left"
				border="0"
				summary="generated form"
			>
				<tr>
					<td class="label">
						<form:label name="searchmode"/>
						:
					</td>
					<td class="content">
						<form:input name="searchmode"/>
						<form:error name="searchmode"/>
					</td>
				</tr>
				<tr>
					<td class="label">
						<form:label name="keywords"/>
						:
					</td>
					<td class="content">
						<form:input name="keywords"/>
						<form:error name="keywords"/>
					</td>
				</tr>
				<tr>
					<td class="label">
						<form:label name="attribute_name"/>
						:
					</td>
					<td class="content">
						<form:input name="attribute_name"/>
						<form:error name="attribute_name"/>
					</td>
				</tr>
				<tr>
					<td class="label">
						<form:label name="attribute_value"/>
						:
					</td>
					<td class="content">
						<form:input name="attribute_value"/>
						<form:error name="attribute_value"/>
					</td>
				</tr>
				<tr>
					<td class="label">
						<form:label name="attribute_filter"/>
						:
					</td>
					<td class="content">
						<form:input name="attribute_filter"/>
						<form:error name="attribute_filter"/>
					</td>
				</tr>
				<tr>
					<td class="label">
						<form:label name="rating"/>
						:
					</td>
					<td class="content">
						<form:input name="rating"/>
						<form:error name="rating"/>
					</td>
				</tr>
			</table>
		</form:form>
	</layout:body>
</layout:html>