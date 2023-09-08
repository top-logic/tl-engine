<%@page import="com.top_logic.base.accesscontrol.ApplicationPages"
%><%@page import="com.top_logic.basic.util.ResKey"
%><%@page language="java" session="true"  extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.knowledge.gui.layout.TLLayoutServlet"
%><%@page import="com.top_logic.layout.ContentHandlersRegistry"
%><%@page import="com.top_logic.layout.DisplayContext"
%><%@page import="com.top_logic.layout.basic.DefaultDisplayContext"
%><%@page import="com.top_logic.basic.version.Version"
%><%@page import="java.io.File"
%><%@page import="com.top_logic.basic.StringServices"
%><%@page import="com.top_logic.util.Resources"
%><%@page import="com.top_logic.mig.html.UserAgent"
%><%@taglib uri="basic" prefix="basic"
%><%Resources   res             = Resources.getInstance();

String      theContext      = request.getContextPath();
String      theLoginLogoURL = theContext + "/images/login/logo.jpg";

String theEnabledImage = theContext + "/images/login/" + res.getString(ResKey.legacy("tl.login.button"));
boolean allowSubsession = ContentHandlersRegistry.allowSubsession(pageContext);%>
<basic:html>
	<!-- Note: IE does not accept comments before the top-level element in XHTML. -->

	<head>
		<title>
			<basic:text key="<%= com.top_logic.layout.I18NConstants.APPLICATION_TITLE %>"/>
		</title>
		<basic:js/>
		<style type="text/css">
			body {
				font-family:Arial,sans-serif;
				margin:0;
				padding:0;
				background-color:#F5F5F5
			}
			
			form {
				margin:0px;
			}
			
			div.logindiv {
				background-image: url(<%=theContext%>/images/login/loginscreen.png);
				background-repeat: no-repeat;
				z-index:42;
				position:relative;
				left:-10px;
				top:0px;
				width:280px;
				height: 200px;
				padding-top: 80px;
				padding-left: 60px;
				padding-right: 60px;
			}
			td.message {
				height: 100px;
				text-align: left;
				font-family:Arial,sans-serif;
				font-size:9pt;
				font-weight:bold;
				vertical-align:middle;
				color:#6F6F6F;
			}
			input {
				font-family:Arial,sans-serif;
				font-size:8pt;
			}
			img {
				border: none;
			}
			img.hidden {
				width:0px;
				height:0px;
				display:none;
			}
			
			tr.hspace {
				height: 10px;
			}
			
			tr.buttonbar {
				height: 22px;
			}
			
			td.button {
				padding: 0px;
				text-align: center;
				width: 82px;
				height: 22px;
				vertical-align: middle;
				background-image: url(<%=theContext%>/images/icons/button-background.png);
				background-repeat: no-repeat;
			}
			
			td.button a {
				text-decoration: none;
				font-size: 10pt;
				color: white;
				font-weight: normal;
			}
		</style>

		<basic:favicon/>
	</head>

	<body class="<%=UserAgent.getUserAgent(request).getCSSClasses("")%>">
		<table
			border="0"
			width="100%"
		>
			<tr valign="bottom">
				<td align="center">
					<div class="logindiv">
						<table
							border="0"
							cellpadding="0"
							cellspacing="0"
							width="100%"
						>
							<tr>
								<td height="10px">
								</td>
							</tr>
							<tr>
								<td class="message">
									<%=res.getMessage(allowSubsession ? ResKey.legacy("tl.subsession.create") : ResKey.legacy("tl.subsession.deny"), Version.getApplicationVersion().getName())%>
								</td>
							</tr>
							<tr class="hspace">
								<td>
								</td>
							</tr>
							<tr>
								<td align="right">
									<table>
										<tr class="buttonbar">
											<td class="button">
												<a
													href="#"
													onclick="window.close(); return false;"
												>
													<basic:text i18n="tl.subsession.cancel"/>
												</a>
											</td>
											<%
											if (allowSubsession) {
												%>
												<td width="5px">
												</td>
												<td class="button">
													<a href="<%=theContext + ApplicationPages.getInstance().getStartPage() %>">
														<basic:text i18n="tl.subsession.load"/>
													</a>
												</td>
												<%
											}
											%>
										</tr>
									</table>
								</td>
							</tr>
						</table>
					</div>
				</td>
			</tr>
		</table>
	</body>
</basic:html>