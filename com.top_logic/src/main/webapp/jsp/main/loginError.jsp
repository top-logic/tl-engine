<%@page import="com.top_logic.base.accesscontrol.ApplicationPages"
%><%@page language="java"  session="false"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="util" prefix="tl"
%><!DOCTYPE html>
<html>
	<head>
		<meta
			content="IE=edge"
			http-equiv="X-UA-Compatible"
		/>
		<title>
			<tl:label name="jsp.main.LoginErrorPage.title"/>
		</title>
		<basic:cssLink/>
		<basic:js name="tl/loginError"/>
	</head>

	<body>
		<div class="frmBody">
			<h1>
				<tl:label name="jsp.main.LoginErrorPage.title"/>
			</h1>

			<p>
				<tl:label name="jsp.main.LoginErrorPage.content1"/>
				<span id="timer">
					5
				</span>
				<tl:label name="jsp.main.LoginErrorPage.content2"/>
			</p>

			<p>
				<tl:label name="jsp.main.LoginErrorPage.content3"/>
			</p>
		</div>
	</body>
</html>