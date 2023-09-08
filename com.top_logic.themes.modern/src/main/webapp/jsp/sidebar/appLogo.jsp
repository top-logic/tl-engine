<%@page import="com.top_logic.layout.tabbar.TabbarUtil"
%><%@page import="com.top_logic.basic.xml.TagWriter"
%><%@page import="com.top_logic.layout.DisplayContext"
%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.mig.html.layout.LayoutComponent"
%><%@page import="com.top_logic.util.TLContext"
%><%@page import="com.top_logic.basic.version.Version"
%><%@page import="com.top_logic.layout.basic.contextmenu.menu.Menu"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="layout"   prefix="layout"
%><%@taglib uri="basic"    prefix="basic"
%><%LayoutComponent theComponent = MainLayout.getComponent(pageContext);
DisplayContext  theDisplay   = DefaultDisplayContext.getDisplayContext(pageContext);
TagWriter       writer       = MainLayout.getTagWriter(pageContext);%><layout:html>
	<layout:head/>
	<layout:body>
		<div class="sblTitle">
			<basic:image
				cssClass="sidebar-max"
				height="30px"
				srcKey="theme:IMAGES_APP_LOGO"
			/>
			<basic:image
				cssClass="sidebar-min"
				height="30px"
				srcKey="theme:IMAGES_APP_LOGO_MINIMIZED"
			/>
			<span class="accText sblTitleContent">
				<span class="sblTitleName">
					<basic:text key="<%= com.top_logic.layout.I18NConstants.APPLICATION_TITLE %>"/>
				</span>
				<span class="sblTitleVersion">
					<basic:text value="<%=Version.getApplicationVersion().getVersionString()  + ' ' + '(' + TLContext.getContext().getCurrentUserName() + ')'%>"/>
				</span>
			</span>
		</div>
		<% TabbarUtil.writePopup(theComponent, theDisplay, writer);	%>
		<basic:hasView name="quickSearch">
			<div class="quickSearchTitle accText">
				<basic:view name="quickSearch"/>
			</div>
		</basic:hasView>
		<basic:notHasView name="quickSearch"/>
	</layout:body>
</layout:html>