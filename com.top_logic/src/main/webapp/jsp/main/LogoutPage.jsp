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
    	@font-face {
			font-family: "Inter-Regular";
			src: url("../../themes/default/fonts/Inter-Regular.otf") format("otf"),
				 url("../../themes/default/fonts/Inter-Regular.woff") format("woff"),
				 url("../../themes/default/fonts/Inter-Regular.ttf") format("truetype");
		}
		
		@font-face {
			font-family: "Inter-SemiBold";
			src: url("../../themes/default/fonts/Inter-SemiBold.otf") format("otf"),
				 url("../../themes/default/fonts/Inter-SemiBold.woff") format("woff"),
				 url("../../themes/default/fonts/Inter-SemiBold.ttf") format("truetype");
		}

        body {
        	font-family: 'Inter-Regular';
			font-size: .875rem;
			line-height: 1.125rem;
			letter-spacing: .15px;
            overflow: hidden;
            background: transparent;
            height: 100vh;
            margin: 0;
        }

        .container {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            height: 100%;
        }

        .caption {
            margin: 0 auto;
            color: #101113;
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
        	font-family: 'Inter-SemiBold';
			font-size: .875rem;
			line-height: 1.125rem;
			letter-spacing: .15px;
            padding: .5rem 1rem;
            height: 2rem;
            border-radius: .25rem;
            cursor: pointer;
			border: none;
            background-color: #2968c8;
            box-shadow: inset 0px 0px 0px 1px #2968c8;
            color: #ffffff;
   		}

        button:hover {
            background: #245db2;
            box-shadow: inset 0px 0px 0px 1px #245db2;
        }
        
        button:focus {
            box-shadow: inset 0px 0px 0px 2px #2968C8, inset 0px 0px 0px 3px #ffffff;
        }

        /** TL Logo Settings **/
        .logo {
            display: flex;
            background-repeat: no-repeat;
            justify-content: center;
            margin-bottom: 1.5rem;
        }

        .logo img {
            width: 340px;
            height: auto;
        }

        /** Text styling **/
        h1 {
        	font-family: 'Inter-SemiBold';
			font-size: 1.5rem;
			line-height: 2rem;
			letter-spacing: 0px;
            color: #101113;
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