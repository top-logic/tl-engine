<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.layout.servlet.CacheControl"%>
<%
// Prevent from caching, otherwise, test will work only once.
CacheControl.setNoCache(response);

// Wait a little to give the browser a chance to do something else.
Thread.sleep(3000);

%>Data transmitted through a XMLHttpRequest object.