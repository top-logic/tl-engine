<%@page language="java"
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
		<form:form>
			<basic:fieldset titleKeySuffix="basicAttributes">
				<table class="formular"
					width="100%"
				>
					<colgroup>
						<col width="0%"/>
						<col width="100%"/>
					</colgroup>
					<tr>
						<td class="label">
							<form:label name="name"/>
							:
						</td>
						<td class="content">
							<form:input name="name"
								columns="30"
							/>
							<form:error name="name"
								icon="true"
							/>
						</td>
					</tr>
					<tr>
						<td class="label">
							<form:label name="firstname"/>
							:
						</td>
						<td class="content">
							<form:input name="firstname"
								columns="30"
							/>
							<form:error name="firstname"
								icon="true"
							/>
						</td>
					</tr>
				</table>
			</basic:fieldset>
		</form:form>
	</layout:body>
</layout:html>