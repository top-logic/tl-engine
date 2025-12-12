<%@page language="java" extends="com.top_logic.util.TopLogicJspBase"
%><%@page import="com.top_logic.chat.model.Chat"
%><%@page import="com.top_logic.chat.model.ChatMessage"
%><%@page import="com.top_logic.chat.component.ChatDetailComponent"
%><%@page import="com.top_logic.knowledge.wrap.Wrapper"
%><%@page import="com.top_logic.layout.form.FormMember"
%><%@page import="java.text.SimpleDateFormat"
%><%@page import="java.util.Date"
%><%@taglib uri="layout" prefix="layout"
%><%@taglib uri="form" prefix="form"
%><layout:html>
	<layout:head/>

	<layout:body>
		<style>
			.chat-detail {
				display: flex;
				flex-direction: column;
				height: 100%;
			}
			.messages-container {
				flex: 1;
				overflow-y: auto;
				padding: 15px;
				background: #f8f9fa;
			}
			.message {
				margin-bottom: 15px;
				padding: 10px;
				border-radius: 8px;
				max-width: 80%;
				clear: both;
			}
			.user-message {
				background: #e3f2fd;
				float: right;
				text-align: right;
			}
			.assistant-message {
				background: #ffffff;
				border: 1px solid #dee2e6;
				float: left;
				text-align: left;
			}
			.message-role {
				font-weight: bold;
				font-size: 0.9em;
				margin-bottom: 5px;
				color: #6c757d;
			}
			.message-text {
				white-space: pre-wrap;
				word-wrap: break-word;
			}
			.message-time {
				font-size: 0.8em;
				color: #999;
				margin-top: 5px;
			}
			.input-container {
				padding: 15px;
				background: #fff;
				border-top: 1px solid #dee2e6;
			}
			.clearfix::after {
				content: "";
				display: table;
				clear: both;
			}
		</style>

		<div class="chat-detail">
			<%
			ChatDetailComponent component = (ChatDetailComponent) mainLayout.getComponentByName("chatDetail");
			Chat chat = component != null ? component.getChat() : null;

			if (chat == null) {
			%>
				<div style="padding: 20px; text-align: center; color: #999;">
					<p>Select a chat from the list to start messaging</p>
				</div>
			<%
			} else {
			%>
				<div class="messages-container" id="messages-container">
					<div class="clearfix">
					<%
					SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
					for (ChatMessage msg : chat.getMessages()) {
						String role = msg.getRole();
						String text = msg.getText();
						Date timestamp = msg.getTimestamp();
						String messageId = ((Wrapper) msg).tId().toString();

						String cssClass = "user".equals(role) ? "user-message" : "assistant-message";
						String displayRole = "user".equals(role) ? "You" : "Assistant";
					%>
						<div class="message <%= cssClass %>" id="message-<%= messageId %>">
							<div class="message-role"><%= displayRole %></div>
							<div class="message-text"><%= text != null ? text : "" %></div>
							<div class="message-time"><%= dateFormat.format(timestamp) %></div>
						</div>
					<%
					}
					%>
					</div>
				</div>

				<div class="input-container">
					<form:form>
						<form:input name="<%= ChatDetailComponent.MESSAGE_INPUT_FIELD %>"
						           multiLine="true"
						           rows="3"
						           columns="80"/>
						<form:button name="sendMessage"/>
					</form:form>
				</div>
			<%
			}
			%>
		</div>

		<script>
			// Auto-scroll to bottom on page load
			var messagesContainer = document.getElementById('messages-container');
			if (messagesContainer) {
				messagesContainer.scrollTop = messagesContainer.scrollHeight;
			}
		</script>
	</layout:body>
</layout:html>
