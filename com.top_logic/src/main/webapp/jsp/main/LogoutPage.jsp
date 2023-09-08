<%@page import="com.top_logic.basic.xml.TagUtil"%>
<%@page extends="com.top_logic.util.NoContextJspBase" 
%><%@taglib uri="basic" prefix="basic"
%><%@page import="com.top_logic.base.accesscontrol.ApplicationPages"
%><%@page import="com.top_logic.basic.util.ResKey"
%><%@page import="com.top_logic.util.Resources"
%><%
try {
	String logoutPage = ApplicationPages.getInstance().getLogoutPage();
	String logoutURL = request.getContextPath() + logoutPage;
	Resources res = Resources.getInstance();
%>
<basic:html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
	<meta http-equiv="refresh" content="1; URL=<%=logoutURL %>" />
    <title>TL logout screen</title>
	
    <style>
        body {
            overflow: hidden;
            background: transparent;
            height: 100vh;
        }

        .container {
            display: flex;
            flex-direction: column;
            margin-top: 40vh;
            height: 100%;
            justify-content: flex-start;
        }

        .caption {
            margin: 0 auto;
            font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
            font-size: 1rem;
            color: #000;
            text-align: center;
            margin-bottom: 24px;
        }

        /** Button styling **/
        /* Reset styling*/
        button {
            background-color: transparent;
            border-width: 0;
            font-family: inherit;
            font-size: inherit;
            font-style: inherit;
            font-weight: inherit;
            line-height: inherit;
            padding: 0;
        }

        button {
            /* display + box model */
            min-width: 120px;
            padding-left: 20px;
            padding-right: 20px;
            height: 2.5rem;
            border-radius: .25rem;
            /* color */
            background-color: #0090b8;
            color: white;
            /* text label */
            font-weight: 600;
            letter-spacing: 1px;
   		}

        button:hover {
            background-color: #0078a3;
        }

        /** TL Logo Settings **/
        .logo {
            display: flex;
            background-repeat: no-repeat;
            justify-content: center;
            margin-bottom: 1.5rem;
        }

        img {
            width: 240px;
        }

        /** Text styling **/
        h1 {
            margin-top: 0;
            margin-bottom: 1.5rem;
        }

        .small {
            font-size: .875rem;
        }
    </style>
</head>

<body>
    <div class="container">
        <div class="logo">
        	<basic:image icon="<%= com.top_logic.layout.Icons.APP_LOGO %>" altKey="<%= com.top_logic.layout.I18NConstants.APPLICATION_TITLE %>"/>
        </div>
        <form action="<%=logoutURL %>">
	        <div class="caption">
	            <div>
	                <h1><% TagUtil.writeText(out, res.getString(ResKey.legacy("tl.logout.message0"))); %></h1>
	            </div>
	            <p class="small">
	                <% TagUtil.writeText(out, res.getString(ResKey.legacy("tl.logout.message1"))); %>
	            </p>
	            <button type="submit"><% TagUtil.writeText(out, res.getString(ResKey.legacy("tl.logout.message2"))); %></button>
	        </div>
        </form>
    </div>
</body>
</basic:html>
<%
} finally {
	// Note: Session invalidation must be done after rendering the page to
	// say goodbye in the users language.
	HttpSession theSession = request.getSession(/*create*/ false);
	if (theSession != null) {
		theSession.invalidate();
	}
}
%>