<%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@taglib uri="util"  prefix="util"
%><!DOCTYPE html>
<html>
	<head>
		<title>
			util:replaceVar (VariableReplaceTag)
		</title>
		<style type="text/css">
			td,th {vertical-align:top}
		</style>
	</head>
	<body bgcolor="#FFFFFF">
		<h2>
			Testspage for util:replaceVar (VariableReplaceTag)
		</h2>
		<table
			cellpadding="20"
			with="95%"
		>
			<thead>
				<th>
					Fieldname
				</th>
				<th>
					Testtext
				</th>
				<th>
					Description
				</th>
			</thead>
			<tr>
				<td>
					<util:label name="replaceVar"/>
				</td>
				<td>
					<util:replaceVar prefix="base.edit.">
						Dies ist ein I18N Text. Bitte
						klicken Sie auf "{cancel}" wenn Sie keinen Bedarf haben.
						Falls das nicht klappt, fragen Sie sich
						Folgendes:"{confirmDelete}"
						oder holen Sie sich einen Tee.
					</util:replaceVar>
				</td>
				<td>
					<code>
						prefix=base.edit.
					</code>
					used keys are {cancel} and {confirmDelete}
				</td>
			</tr>
			<tr>
				<td>
					<util:label name="replaceVar"/>
				</td>
				<td>
					<util:replaceVar prefix="">
						Nun reden wir mal Klartext: Sagen Sie {false}
						zu {december} oder sind Sie {disabled}?
					</util:replaceVar>
				</td>
				<td>
					Empty prefix, used keys are {false}, {december} and
					{disabled}.
				</td>
			</tr>
		</table>
	</body>
</html>