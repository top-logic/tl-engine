<%@page import="com.top_logic.service.openapi.server.OpenApiServer"
%><%@page import="com.top_logic.basic.io.FileCompiler"
%><%@page import="com.top_logic.basic.NoProtocol"
%><%@page import="com.top_logic.basic.Protocol"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%@page extends="com.top_logic.util.NoContextJspBase"
%><%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" session="false"
%><%!
private String swaggerResource(String ressource) {
	String webJar = "webjar:org.webjars.npm/swagger-ui-dist:/webjars/swagger-ui-dist:";
	Protocol log = NoProtocol.INSTANCE;
	return FileCompiler.resolveResourcePath(log, webJar + ressource);
}
%><layout:html>
	<layout:head>
		<link
			href="<%= request.getContextPath() + swaggerResource("swagger-ui.css") %>"
			rel="stylesheet"
		/>
	</layout:head>

	<layout:body>
		<section id="swagger-ui"
			class="section"
		>
		</section>

		<basic:js name="<%= \"..\" + swaggerResource(\"swagger-ui-bundle\") %>"
			i18n="false"
		/>

		<basic:script>
  window.onload = () => {
    window.ui = SwaggerUIBundle({
      url: '<%=request.getContextPath() + request.getParameter(OpenApiServer.API_URL_PARAM)%>',
      dom_id: '#swagger-ui',
    });
  };
		</basic:script>
	</layout:body>
</layout:html>