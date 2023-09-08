<%@page import="com.top_logic.layout.form.tag.Icons"
%><%@page import="com.top_logic.layout.basic.ThemeImage"
%><%@page import="com.top_logic.layout.form.tag.StaticPageRenderer"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="ajaxform" prefix="form"
%><layout:html>
	<layout:head/>
	
	<layout:body>
		<form:page pageRenderer="<%= StaticPageRenderer.INSTANCE %>">
			<form:title>
				The title
			</form:title>
			<form:subtitle>
				The subtitle
			</form:subtitle>
			<form:iconbar>
				<basic:image icon="<%=Icons.PLUS48 %>"/>
			</form:iconbar>
			<p>
				----------------------------S-o-m-e---v-e-r-y---l-o-n-g---c-o-n-t-e-n-t---t-h-a-t---d-o-e-s---n-o-t---w-r-a-p---l-i-n-e-s---c-a-u-s-i-n-g---a---h-o-r-i-z-o-n-t-a-l---s-c-r-o-l-l-b-a-r-----------------
			</p>
			<%
			for (int n = 0; n < 250; n++) {
				%>
				Some numbers (<%= n %>).
				<%
			}
			%>
		</form:page>
	</layout:body>
</layout:html>