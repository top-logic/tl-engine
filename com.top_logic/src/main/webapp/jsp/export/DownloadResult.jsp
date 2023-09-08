<%@page import="com.top_logic.common.webfolder.ui.commands.Icons"
%><%@page import="com.top_logic.layout.form.tag.FormPageTag"
%><%@page import="com.top_logic.mig.html.layout.LayoutComponent"
%><%@page language="java" session="true"  extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="ajaxform"  prefix="form"
%><%@taglib uri="basic" prefix="basic"
%><%LayoutComponent theComp = MainLayout.getComponent(pageContext);
%><layout:html>
	<layout:head/>
	<layout:body>
		<form:page>
			<form:title>
				<basic:text value="<%=theComp.getResMessage(\"headline\")%>"/>
			</form:title>
			<form:subtitle>
				<basic:text value="<%=theComp.getResMessage(\"message\")%>"/>
			</form:subtitle>
			<form:iconbar>
				<basic:image
					cssClass="<%=FormPageTag.IMAGE_CSS_CLASS %>"
					icon="<%= Icons.DOWNLOAD60 %>"
				/>
			</form:iconbar>
		</form:page>
	</layout:body>
</layout:html>