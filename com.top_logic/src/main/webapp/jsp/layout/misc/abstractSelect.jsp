<%@page language="java"
extends="com.top_logic.util.TopLogicJspBase"
contentType="text/html; charset=UTF-8"
buffer="none"
autoFlush="true"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="basic"    prefix="basic"
%><%@taglib uri="ajaxform" prefix="form"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.form.component.AbstractSelectorComponent"%><%
AbstractSelectorComponent  theComp      = (AbstractSelectorComponent) MainLayout.getComponent(pageContext);
String                     theFieldName = theComp.getSelectFieldName();
%><layout:html>
	<layout:head>
		<basic:cssLink/>
	</layout:head>
	<layout:body>
		<form:form>
			<basic:fieldset titleKeySuffix="selectLegend">
				<table class="formular"
					width="100%"
				>
					<colgroup>
						<col width="100%"/>
					</colgroup>
					<tr>
						<td class="content">
							<form:popup name="<%=theFieldName %>"
								clearButton="true"
							/>
							<form:error name="<%=theFieldName %>"
								icon="true"
							/>
						</td>
					</tr>
				</table>
			</basic:fieldset>
		</form:form>
	</layout:body>
</layout:html>