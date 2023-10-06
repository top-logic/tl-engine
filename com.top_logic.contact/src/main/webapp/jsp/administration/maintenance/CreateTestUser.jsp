<%@page import="java.util.Arrays"
%><%@page import="com.top_logic.basic.ArrayUtil"
%><%@page import="com.top_logic.contact.business.PersonContact"
%><%@page import="java.util.Random"
%><%@page import="com.top_logic.base.security.password.PasswordManager"
%><%@page extends="com.top_logic.util.TopLogicJspBase" contentType="text/html; charset=UTF-8"
%><%@taglib uri="basic" prefix="basic"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="com.top_logic.tool.boundsec.BoundHelper"
%><%@page import="com.top_logic.base.accesscontrol.LoginCredentials"
%><%@page import="com.top_logic.basic.StringServices"
%><%@page import="com.top_logic.knowledge.service.PersistencyLayer"
%><%@page import="com.top_logic.knowledge.service.KnowledgeBase"
%><%@page import="com.top_logic.knowledge.service.Transaction"
%><%@page import="com.top_logic.knowledge.wrap.person.PersonManager"
%><%@page import="com.top_logic.knowledge.wrap.person.Person"
%><%@page import="com.top_logic.base.security.attributes.PersonAttributes"
%><%@page import="java.util.List"
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
	<%!private static final String testUserPrefix = "testUser";
	
	private String createUserName(boolean addPrefix, String foreName, String surName) {
		String fn = normalize(foreName);
		String sn = normalize(surName);
		for (int i = 0; i < fn.length(); i++) {
			String s1 = fn.substring(i, i+1);
			for (int j = 0; j < sn.length()-1; j++) {
				String s2 = sn.substring(j, j+2);
				String name = s1+s2;
				name = addPrefix ? testUserPrefix + name : name;
				if (PersonManager.getManager().byName(name) == null) {
					return name;
				}
			}
		}
		Random rand = new Random();
		while (true) {
			// always add prefix to random-usernames to allow easy remove
			String name = testUserPrefix + StringServices.getRandomString(rand, 8);
			if (PersonManager.getManager().byName(name) == null) {
				return name;
			}
		}
	}
	
	private String normalize(String name) {
		name = name.toUpperCase();
		name = name.replaceAll("ß", "S");
		name = name.replaceAll("Ä", "A");
		name = name.replaceAll("Ö", "O");
		name = name.replaceAll("Ü", "U");
		return name;
	}%>
	

	<layout:body>
		<basic:access>
			<%
			String message = "";
				if (request.getParameter("CREATE") != null) {
					String[] femaleForenames = {"Brigitte", "Heike", "Steffi", "Gabriela",
						"Sabine", "Barbara", "Claudia", "Maria",
					"Sandra", "Monika"};
					String[] maleForenames = {"Heinz", "Herbert", "Dirk", "Steffen",
						"Siegfried", "Andreas", "Erik", "Christian",
					"Matthias", "Alexander"};
					String[] surNames = {"Schmidt", "Krause", "Schubert", "Klein",
						"Rose", "Schill", "Vogler", "Ganter",
						"Brunner", "Lehner", "Härtig", "Aßmann",
						"Baader", "Spallek", "Nagel", "Hochberger",
					"Pfitzmann", "Petersohn", "Flach", "Meißner"};
					
					
					int testUserAmount = 0;
					String rawAmountValue = request.getParameter("testUserAmount");
					if(!StringServices.isEmpty(rawAmountValue)) {
						testUserAmount = Integer.parseInt(rawAmountValue);
					}
					if(testUserAmount > 0) {
						KnowledgeBase knowledgeBase = PersistencyLayer.getKnowledgeBase();
						try (Transaction transaction = knowledgeBase.beginTransaction()) {
							int stop = 0;
							PersonManager persMan = PersonManager.getManager();
							boolean addPrefix = "true".equals(request.getParameter("PREFIX"));
							char[] initialPassword = StringServices.nonNull(request.getParameter("PWD")).toCharArray();
							int surNameCount = surNames.length;
							int foreNameCount = femaleForenames.length; // asserts femaleForenames.length == maleForenames.length;
							for(int i = 0; i < testUserAmount; i++) {
								stop = i+1;
								float surNameRandomNumber = (float)Math.random();
								float foreNameRandomNumber = (float)Math.random();
								int surNameIndex = Math.round(surNameRandomNumber * (surNameCount-1));
								String surName = surNames[surNameIndex];
								int foreNameIndex = Math.round(foreNameRandomNumber * (foreNameCount-1));
								String foreName;
								if(foreNameRandomNumber > 0.5) {
									foreName = maleForenames[foreNameIndex];
								}
								else {
									foreName = femaleForenames[foreNameIndex];
								}
								Person person = persMan.createPerson(createUserName(addPrefix, foreName, surName), "dbSecurity", null, false);
								Person.userOrNull(person).setAttributeValue(PersonAttributes.GIVEN_NAME, foreName);
								Person.userOrNull(person).setAttributeValue(PersonAttributes.SUR_NAME, surName);
								if (initialPassword.length > 0) {
									PasswordManager.getInstance().setPassword(LoginCredentials.fromUserAndPassword(person, initialPassword));
								}
								PersonContact pc = PersonContact.getPersonContact(person);
								pc.copyPersonAttributesToContact(person);
							}
							transaction.commit();
							message = "Created " + stop + " user";
							message = message + (StringServices.isEmpty(initialPassword) ? "" : " with initial password '" + new String(initialPassword) + "'");
							message = message + (addPrefix ? " with prefixed username." : ".");
							} catch (Exception ex) {
							message = "ERROR: " + ex.getLocalizedMessage();
						}
					}
				}
				
				else if (request.getParameter("DELETE_ALL") != null) {
					int delCount = 0;
					PersonManager persMan = PersonManager.getManager();
					KnowledgeBase knowledgeBase = PersistencyLayer.getKnowledgeBase();
					Transaction transaction = knowledgeBase.beginTransaction();
					List<Person> persons = persMan.getAllPersonsList();
					for(Person person : persons) {
						if(Person.userOrNull(person).getUserName().startsWith(testUserPrefix)) {
							delCount++;
							persMan.deleteUser(person);
							person.tDelete();
						}
					}
					transaction.commit();
					message = "Deleted "+delCount+" user with prefixed username.";
				}
			%>
			<h1>
				Test User Generator
			</h1>
			<p>
				This page allows to create some users for test purposes.
			</p>
			<%
			if (request.getParameter("CREATE") != null || request.getParameter("DELETE_ALL") != null) {
				%>
				<p>
					<b>
						<%=message%>
					</b>
				</p>
				<%
			}
			%>
			<br/>
			<form method="POST">
				<table>
					<tr>
						<td>
							Number of users to create
						</td>
						<td>
							<input name="testUserAmount"
								type="text"
								value="20"
							/>
						</td>
					</tr>
					<tr>
						<td>
							Add 'testUser' prefix to username
						</td>
						<td>
							<input name="PREFIX"
								checked="checked"
								type="checkbox"
								value="true"
							/>
						</td>
					</tr>
					<tr>
						<td>
							Set initial password
						</td>
						<td>
							<input name="PWD"
								type="text"
								value="123"
							/>
						</td>
					</tr>
					<tr>
						<td>
							&#xA0;
						</td>
						<td>
							<input name="CREATE"
								type="submit"
								value="Generate test users"
							/>
						</td>
					</tr>
					<tr>
						<td>
							&#xA0;
						</td>
						<td>
							<input name="DELETE_ALL"
								type="submit"
								value="Delete all test users"
							/>
						</td>
					</tr>
				</table>
			</form>
			<br/>
		</basic:access>
	</layout:body>
</layout:html>