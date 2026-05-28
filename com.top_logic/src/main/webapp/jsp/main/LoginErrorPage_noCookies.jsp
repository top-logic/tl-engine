<%@page extends="com.top_logic.util.NoContextJspBase" session="false"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="util" prefix="tl"
%>
<basic:html>
	<head>
		<title>
			<tl:label nameConst="<%= com.top_logic.base.accesscontrol.I18NConstants.ERROR_SET_COOKIES %>"/>
		</title>
		<basic:cssLink/>
	</head>

	<body bgcolor="white">
		<table
			align="center"
			cellpadding="10"
		>
			<tr>
				<td class="content"
					align="center"
					bgcolor="#eeeeee"
				>
					<tl:label nameConst="<%= com.top_logic.base.accesscontrol.I18NConstants.ERROR_SET_COOKIES.tooltip() %>"/>
				</td>
			</tr>
		</table>
	</body>
</basic:html>