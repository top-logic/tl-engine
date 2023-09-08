<%@page import="com.top_logic.basic.xml.TagUtil"
%><%@page import="com.top_logic.basic.version.Version"%><basic:script>
	var appName = <% TagUtil.writeJsString(out, Version.getApplicationVersion().getName()); %>

    function doOnload() {
        if (top.opener) { 
              	// This window was opened from some other window. Check, whether 
              	// the opener belongs to the same application.
              	
            var openerServices
            try {
				// Test, whether the opener is accessible.
                openerServices = top.opener.services;
              		
                if (openerServices && openerServices.appName == appName) {
               		// Login in opener, close this window.
                    top.opener.location.href = self.location;
                    top.close();
                    return false;
                }
            } catch (ex) {
              		// No access to opener window, it belongs to another application.
            }
        }

        // Escape from inner frames of the own application.
        var root = window.self;
        while (true) {
        	var rootParent = root.parent;
        	if (rootParent == null) {
        		break;
        	}
        	if (rootParent === root) {
        		break;
        	}
        	root = rootParent;
        }
        
        if (root !== window.self) {
        	root.location.href=window.self.location;
            return false;
        }

		window.onerror=handleError;
		window.status = ' ';

		selectFirst();
	}
</basic:script>