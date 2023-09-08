<%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page import="com.top_logic.layout.basic.RenderErrorUtil"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head/>
	<layout:body>
		<%
		RenderErrorUtil.produceErrorOutput(DefaultDisplayContext.getDisplayContext(pageContext),
			MainLayout.getTagWriter(pageContext),
			com.top_logic.layout.form.component.I18NConstants.ERROR_VIEW_CREATION,
			(String)request.getAttribute(RenderErrorUtil.RENDERING_ERROR_MESSAGE_ATTRIBUTE),
			(Throwable)request.getAttribute(RenderErrorUtil.RENDERING_ERROR_ATTRIBUTE),
		this);
		%>
	</layout:body>
</layout:html>