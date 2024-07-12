<%@page language="java" extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="com.top_logic.basic.xml.TagUtil"
%><%@page import="com.top_logic.base.security.util.CryptSupport"
%><%@page import="com.top_logic.basic.StringServices"%>

<%!
// JSP page configuration:

// RUN_BUTTON = null: run automatic ; REFRESH_BUTTON = null: no refresh button
private static final String RUN_BUTTON = "Run";
private static final String REFRESH_BUTTON = null;

private static final String TITLE = "Decode String";
private static final String DESCRIPTION =
"This page decodes the given string<br/>";
%>

<%!
// This methods does the actual work:
private void runWork(JspWriter out, HttpServletRequest request) throws Exception {
	writer = out;
	decodeString(out, request.getParameter("decodeString"));
	out.write("<p>Finished.</p>");
}


private void decodeString(JspWriter out, String theString)  throws Exception {
	if(StringServices.isEmpty(theString)){ return;}
	try{
		String theDecode = CryptSupport.getInstance().decodeString(theString);
		print("Decoded String:");
		print(theDecode);
	}catch(Exception e){
		out.write("<span class=\"tl-error-message\"> Failed to decode String.");
		out.write(e.toString());
		out.write("</span></br></br>");
	}
}

%>

<%!
// Util methods:
private JspWriter writer = null;

private void print(String string) throws Exception {
	writer.write(quote(string));
	writer.write("<br/>");
	writer.flush();
}

private String quote(String string) {
	return TagUtil.encodeXML(string);
}
%><layout:html>
	<layout:head>
		<title>
			<%=TITLE%>
		</title>
		<meta
			content="text/html; charset=iso-8859-1"
			http-equiv="Content-Type"
		/>
		<basic:cssLink/>
	</layout:head>

	<layout:body>
		<basic:access>
			<h1>
				<%=TITLE%>
			</h1>
			<p>
				<%=DESCRIPTION%>
			</p>
			<%
			if (RUN_BUTTON == null || request.getParameter("SUBMIT") != null) {
				%>
					<%
						runWork(out,request);
					%>
				<%
				if (REFRESH_BUTTON != null) {
					%>
					<form method="POST">
						<table>
							<tr>
								<td>
									<p>
										<button class="tlButton cButton cmdButton" name="SUBMIT" type="submit">
											<h4 class="tlButtonLabel"><%=REFRESH_BUTTON%></h4>
										</button>
									</p>
								</td>
							</tr>
						</table>
					</form>
					<%
				}
			}
			else {
				%>
				<form method="POST">
					<table>
						<tr>
							<td>
								<p>
									Decode the following String: &#xA0;
									<input name="decodeString"
										type="text"
										value=""
									/>
								</p>
							</td>
						</tr>
						<tr>
							<td>
								<p>
									<button class="tlButton cButton cmdButton" name="SUBMIT" type="submit">
										<h4 class="tlButtonLabel"><%=RUN_BUTTON%></h4>
									</button>
								</p>
							</td>
						</tr>
					</table>
				</form>
				<%
			}
			%>
			<br/>
		</basic:access>
	</layout:body>
</layout:html>