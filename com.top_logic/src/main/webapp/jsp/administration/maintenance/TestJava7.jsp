<%@page import="java.util.ArrayList"
%><%@page import="java.util.Collection"
%><%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<h1>
			Test for Java 7 source code compatibility on JSP
		</h1>
		<%
		// This is a test that Java7 features are supported on JSP's
		Collection<String> c = new ArrayList<>();
		%>
		<div>
			"Collection&lt;String&gt; c = new ArrayList&lt;&gt;();" compiles.
		</div>
	</layout:body>
</layout:html>