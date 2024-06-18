<%@page import="com.top_logic.mail.proxy.MailReceiverService"
%><%@page language="java" session="true" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="java.util.Date"
%><%@page import="javax.servlet.jsp.JspWriter"
%><%@page import="java.io.PrintWriter"
%><%@page import="com.top_logic.mig.html.layout.*"
%><%@page import="com.top_logic.basic.Logger"
%><%@page import="com.top_logic.basic.StringServices"
%><%@page import="com.top_logic.util.TLContext"
%><%@page import="com.top_logic.dob.DataObject"
%><%@page import="com.top_logic.dob.xml.DOXMLWriter"
%><%@page import="com.top_logic.knowledge.wrap.person.PersonManager"
%><%@page import="com.top_logic.mig.html.layout.LayoutComponent"
%><%@page import="com.top_logic.mig.html.layout.MainLayout"
%><%@page import="com.top_logic.mail.proxy.MailReceiver"
%><%@page import="javax.mail.Folder"
%><%@page import="javax.mail.Message"
%><%@taglib uri="layout" prefix="layout"
%><%@page import="com.top_logic.basic.xml.TagUtil"
%><layout:html>
	<layout:head>
		
	</layout:head>
	<%!
	private String folderRead;
	
	private String folderCreate;
	
	/**
	* Print the content of the given data object as debug info to system out.
	*
	* @param    anObject    The data object to be printed, may be <code>null</code>.
	* @throws   NoSuchAttributeException
	*/
	public void printDataObject(DataObject anObject, JspWriter aWriter) throws Exception {
		TagUtil.writeText(aWriter, new DOXMLWriter().convertDO2XML(anObject));
	}
	
	public boolean createFolder(com.sun.mail.imap.IMAPFolder aFolder, int i) {
		com.sun.mail.imap.protocol.IMAPProtocol imapprotocol = null;
		try {
			String theName = aFolder.getFullName();
			
			java.lang.reflect.Method theMethod = com.sun.mail.imap.IMAPFolder.class.getDeclaredMethod("getStoreProtocol", new Class[]{});
			theMethod.setAccessible(true);
			imapprotocol = (com.sun.mail.imap.protocol.IMAPProtocol) theMethod.invoke(aFolder, new Object[] {});
			
			if((i & 1) == 0) {
				imapprotocol.create(theName + '.');
			}
			else {
				imapprotocol.create(theName);
				
				if((i & 2) != 0) {
					com.sun.mail.imap.protocol.ListInfo alistinfo[] = imapprotocol.list("", theName);
					
					if(alistinfo != null && !alistinfo[0].hasInferiors) {
						imapprotocol.delete(theName);
						
						throw new javax.mail.MessagingException("Unsupported type");
					}
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			
			return (false);
		}
		
		return (true);
	}%><%
	boolean         isNew     = !"makeItSo".equals(request.getParameter("done"));
	LayoutComponent theComp   = MainLayout.getComponent(pageContext);
	String          theRead   = "INBOX";
	String          theCreate = "";
	
	if (!isNew) {
		theRead   = request.getParameter("folderRead");
		theCreate = request.getParameter("folderCreate");
	}
	%>
	<layout:body>
		<h1>
			Mail Server Connection Test
		</h1>
		<p>
			Please click on "Make It So" to test the Mail server connection with the given parameters.
		</p>
		<form name="select"
			action="<%=theComp.getComponentURL(pageContext).getURL()%>"
			method="post"
		>
			<input name="done"
				type="hidden"
				value="makeItSo"
			/>
			<table
				align="center"
				width="100%"
				style="padding-bottom: var(--spacing-02);"
			>
				<tr>
					<td class="label">
						Folder to read:
					</td>
					<td class="content">
						<input name="folderRead"
							size="50"
							type="text"
							value="<%=theRead%>"
						/>
					</td>
				</tr>
				<tr>
					<td class="label">
						Folder to create:
					</td>
					<td class="content">
						<input name="folderCreate"
							size="50"
							type="text"
							value="<%=theCreate%>"
						/>
					</td>
				</tr>
			</table>
			<div class="cmdButtons">
				<button class="tlButton cButton cmdButton" name="submit" type="submit">
					<h4 class="tlButtonLabel">Make It So</h4>
				</button>
			</div>
		</form>
		<%
		if (!isNew) {
			MailReceiver theProxy   = MailReceiverService.getMailReceiverInstance();
			try {
				Folder  theFolder  = null;
				boolean checkMails = false;
				int     theMailNr  = -1;
				
				if (!StringServices.isEmpty(theRead)) {
					int thePos = theRead.indexOf("#");
					
					if (thePos > 0) {
						if (thePos > theRead.length()) {
							try {
								String theMail = theRead.substring(thePos);
								
								theMailNr = Integer.parseInt(theMail);
							}
							catch (Exception ex) {
							}
						}
						
						checkMails = true;
						theRead    = theRead.substring(0, thePos - 1);
					}
					
					if (theRead.length() == 0) {
						theFolder = theProxy.getDefaultFolder();
					}
					else {
						theFolder = theProxy.getFolder(theRead);
					}
				}
				
				if (theFolder != null) {
					out.write("<hr />");
					
					out.write("Try to access folder '" + theFolder.getName() + "'...<br /><br />");
					out.write("Folder '" + theFolder.getName() + "' found and exists returns: " + theFolder.exists() + "<br />");
					
					Folder[] theSubs = theFolder.list();
					out.write("Number of sub folders: " + theSubs.length + "<br />");
					for (int i=0; i<theSubs.length; i++) {
						out.write("Sub-Folder '" + theSubs[i].getName() + "' found and exists returns: " + theSubs[i].exists() + "<br />");
					}
					
					if (checkMails) {
						out.write("<br />Checking mail in folder '" + theFolder.getName() + "'...<br /><br />");
						
						Message[] theMessages = theFolder.getMessages();
						
						out.write("Folder '" + theFolder.getName() + "' contains #" + theMessages.length + " mails!<br />");
						
						for (int thePos = 0; thePos < theMessages.length; thePos++) {
							out.write("#" + (thePos + 1) + ": '" + theMessages[thePos].getSubject() +
							"' (sent: " + theMessages[thePos].getSentDate() + ")<br />");
						}
						
						if (theMailNr > -1) {
							Message theMessage = theMessages[theMailNr];
							
							out.write("Mail number " + theMailNr + ":<br />");
							
							try {
								out.write("Sender: <b>" + TagUtil.encodeXML(StringServices.toString(theMessage.getFrom())) + "</b><br />");
								out.write("Receiver: <b>" + StringServices.toString(theMessage.getAllRecipients()) + "</b><br />");
								out.write("Subject: <b>" + theMessage.getSubject() + "</b><br />");
								out.write("Type: <b>" + theMessage.getContentType() + "</b><br />");
								out.flush();
								out.write("Content: <b>" + theMessage.getContent() + "</b><br />");
							}
							catch (Exception ex) {
								out.write("<br/><br />Error in reading mail content: " + ex.toString() + "<br />");
							}
						}
					}
				}
				else {
					out.write("Folder is: null" + "<br />");
				}
				
				if (!StringServices.isEmpty(theCreate)) {
					out.write("<hr />");
					
					out.write("Try to create folder '" + theCreate + "'...<br /><br />");
					theFolder = theProxy.getFolder(theCreate);
					
					if (theFolder.create(Folder.READ_WRITE)) {//this.createFolder((com.sun.mail.imap.IMAPFolder)theFolder, Folder.READ_WRITE)) {
						out.write("Folder '" + theFolder.getFullName() + "' created!<br />");
					}
					else {
						out.write("Folder '" + theFolder.getFullName() + "' creation failed!<br />");
					}
					
					if (theFolder != null) {
						out.write("Folder '" + theFolder.getName() + "' created and exists returns: " + theFolder.exists() + "<br />");
					}
					else {
						out.write("Folder '" + theCreate + "' not created (is: null)" + "<br />");
					}
				}
			}
			catch (Exception ex) {
				Logger.error("Mail server connection test failed: ", ex, this);
				out.write("<br />Mail server connection test failed: " + ex);
				
				out.write("<br />Reason is: <br /><pre>");
				
				ex.printStackTrace(new PrintWriter(out));
				
				out.write("</pre>");
			}
		}
		%>
	</layout:body>
</layout:html>