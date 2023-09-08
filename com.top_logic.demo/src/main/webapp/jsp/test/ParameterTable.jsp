<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="java.util.Enumeration" %>
<table>
	<tr>
		<th>
			Parameter
		</th>
		<th>
			Value
		</th>
	</tr>
	<%
	String theName;
	
	for (Enumeration theNames = request.getParameterNames ();
		theNames.hasMoreElements (); ) {
		theName = theNames.nextElement ().toString ();
		%>
		<tr>
			<td class="label">
				<%=theName %>:
			</td>
			<td class="content">
				<%=request.getParameter (theName) %>
			</td>
		</tr>
		<%
	}
	%>
</table>