<%@page import="com.top_logic.basic.xml.TagUtil"
%><%@page import="java.util.Enumeration"
%><%@page import="com.top_logic.basic.StringServices,
com.top_logic.mig.html.HTMLUtil,
com.top_logic.mig.html.layout.LayoutComponent" %>
<!-- This page is included by the other Error Pages to dump the request -->
<table class="error">
	<tr>
		<th>
			Field
		</th>
		<th>
			Value
		</th>
	</tr>
	<%
	Enumeration theNames = request.getParameterNames();
	while(theNames.hasMoreElements()){
		String name  = (String)theNames.nextElement();
		String value = request.getParameter(name);
		if( !StringServices.isEmpty(value)
			&& name.indexOf("password") < 0) {
			%>
			<tr>
				<td class="label_small">
					<% TagUtil.writeText(out, name); %>
				</td>
				<td class="content_small">
					<% TagUtil.writeText(out, value); %>
				</td>
			</tr>
			<%
		} // if
	}
	%>
</table>