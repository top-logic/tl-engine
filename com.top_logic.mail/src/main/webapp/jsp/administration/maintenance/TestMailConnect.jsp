<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="java.util.Date"
%><%@page import="jakarta.servlet.jsp.JspWriter"
%><%@page import="java.io.PrintWriter"
%><%@page import="com.top_logic.mig.html.layout.*"
%><%@page import="com.top_logic.basic.Logger"
%><%@page import="com.top_logic.basic.StringServices"
%><%@page import="com.top_logic.util.TLContext"
%><%@page import="com.top_logic.dob.DataObject"
%><%@page import="com.top_logic.mail.connect.TLStore"
%><%@page import="com.top_logic.knowledge.wrap.person.PersonManager"
%><%@page import="com.top_logic.mig.html.layout.LayoutComponent"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="jakarta.mail.Folder"
%><%@page import="jakarta.mail.Message"
%><%@taglib uri="layout" prefix="layout"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<%
	boolean         isNew   = !"makeItSo".equals(request.getParameter("done"));
	LayoutComponent theComp = MainLayout.getComponent(pageContext);
	if (!isNew) {
		TLStore.setErrorFlag(!TLStore.getErrorFlag());
	}
	
	String theMode = TLStore.getErrorFlag() ? "activate" : "deactivate";
	%>
	<layout:body>
		<h1>
			Mail Server Connection Failure Test
		</h1>
		<p>
			Please click on "Make It So" to <%=theMode %> the mail server connection.
		</p>
		<form name="select"
			action="<%=theComp.getComponentURL(pageContext).getURL() %>"
			method="post"
		>
			<input name="done"
				type="hidden"
				value="makeItSo"
			/>
			<button class="tlButton cButton cmdButton" name="submit" type="submit">
				<h4 class="tlButtonLabel">Make It So</h4>
			</button>
		</form>
		<%
		%>
	</layout:body>
</layout:html>