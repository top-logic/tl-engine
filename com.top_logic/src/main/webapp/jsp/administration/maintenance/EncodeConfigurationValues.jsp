<%@page import="com.top_logic.basic.xml.TagUtil"
%><%@page import="com.top_logic.mig.html.HTMLUtil"
%><%@page import="com.top_logic.basic.StringServices"
%><%@page import="com.top_logic.basic.ConfigurationEncryption"
%><%@page extends="com.top_logic.util.TopLogicJspBase"
contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="com.top_logic.basic.Logger"
%><%@page import="com.top_logic.util.Resources"
%><%@page import="java.util.Enumeration"
%><%@page import="com.top_logic.tool.boundsec.BoundHelper"
%><layout:html>
	<layout:head>
		<title>
			System Flags
		</title>
		<meta
			content="text/html; charset=iso-8859-1"
			http-equiv="Content-Type"
		/>
		<basic:cssLink/>
	</layout:head>

	<layout:body>
		<basic:access>
			<%
			String encodedConfigValue = null;
			String decodedConfigValue = null;
			if (request.getParameter("SUBMIT") != null) {
				String configValue = request.getParameter("configValue");
				if (!StringServices.isEmpty(configValue)) {
					encodedConfigValue = ConfigurationEncryption.encrypt(configValue);
					if (!configValue.equals(ConfigurationEncryption.decrypt(encodedConfigValue))) {
						throw new RuntimeException(
						"Encoding / decoding consistency problem. Decoding of encoded value does not match original value.");
					}
				}
				String configValueForDecode = request.getParameter("encodedConfigValue");
				if (!StringServices.isEmpty(configValueForDecode)) {
					decodedConfigValue = ConfigurationEncryption.decrypt(configValueForDecode);
				}
			}
			%>
			<h1>
				Encoding and decoding of configuration values.
			</h1>
			<p>
				This page helps to encode and decode configuration values.
			</p>
			<form method="POST">
				<h2>
					Encoding
				</h2>
				<p>
					Enter the configuration value to encrypt for encrypted into the input field and press "Encode / Decode".
					<br/>
					<b>
						Note:
					</b>
					Repeated execution of this function with the same value may not result in the same encrypted value.
				</p>
				<table>
					<tr>
						<td>
							Plain configuration value:
						</td>
						<td>
							<input name="configValue"
								type="text"
								value="<%=TagUtil.encodeXMLAttribute(StringServices.nonNull(request.getParameter("configValue")))%>"
							/>
						</td>
					</tr>
					<tr>
						<td>
							Encoded configuration value:
						</td>
						<td>
							<%=TagUtil.encodeXML(StringServices.nonNull(encodedConfigValue)) %>
						</td>
					</tr>
				</table>

				<h2>
					Decoding
				</h2>
				Enter the encrypted configuration value into the input field and press "Encode / Decode" to see the plain configuration value.
				<table>
					<tr>
						<td>
							Encoded configuration value:
						</td>
						<td>
							<input name="encodedConfigValue"
								type="text"
								value="<%=TagUtil.encodeXMLAttribute(StringServices.nonNull(request.getParameter("encodedConfigValue")))%>"
							/>
						</td>
					</tr>
					<tr>
						<td>
							Plain configuration value:
						</td>
						<td>
							<%=TagUtil.encodeXML(StringServices.nonNull(decodedConfigValue)) %>
						</td>
					</tr>
				</table>
				<p>
					<button class="tlButton cButton cmdButton" name="SUBMIT" type="submit">
						<h4 class="tlButtonLabel">Encode / Decode</h4>
					</button>
				</p>
			</form>
		</basic:access>
	</layout:body>
</layout:html>