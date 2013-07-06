/*global $, alert, async_request, clearTimeout, confirm, document, escape, location, navigator, open, prompt, setTimeout, window, worksheet_filenames */
/*jslint maxerr: 10000, white: true, onevar: true, undef: true, nomen: true, eqeqeq: true, plusplus: true, bitwise: true, regexp: true, strict: true, newcap: true, immed: true */
//"use strict";



translations = {
    
    "Your browser / OS combination is not supported.\nPlease use Firefox or Opera under Linux, Windows, or Mac OS X, or Safari." : "Your browser / OS combination is not supported.\nPlease use Firefox or Opera under Linux, Windows, or Mac OS X, or Safari.",
    
    "Java Applet Hidden" : "Java Applet Hidden",
    
    "Click here to pop out" : "Click here to pop out",
    
    "Error applying function to worksheet(s)." : "Error applying function to worksheet(s).",
    
    "Title of saved worksheet" : "Title of saved worksheet",
    
    "Failed to save worksheet." : "Failed to save worksheet.",
    
    "Rename worksheet" : "Rename worksheet",
    
    "Please enter a name for this worksheet." : "Please enter a name for this worksheet.",
    
    "Rename" : "Rename",
    
    "Possible failure deleting worksheet." : "Possible failure deleting worksheet.",
    
    "unprinted" : "unprinted",
    
    "You requested to evaluate a cell that, for some reason, the server is unaware of." : "You requested to evaluate a cell that, for some reason, the server is unaware of.",
    
    "Error" : "Error",
    
    "This worksheet is read only. Please make a copy or contact the owner to change it." : "This worksheet is read only. Please make a copy or contact the owner to change it.",
    
    "loading...Error updating cell output after " : "loading...Error updating cell output after ",
    
    "s (canceling further update checks)." : "s (canceling further update checks).",
    
    "Problem inserting new input cell after current input cell.\n" : "Problem inserting new input cell after current input cell.\n",
    
    "Worksheet is locked. Cannot insert cells." : "Worksheet is locked. Cannot insert cells.",
    
    "Unable to interrupt calculation." : "Unable to interrupt calculation.",
    
    "Close this box to stop trying." : "Close this box to stop trying.",
    
    "Interrupt attempt" : "Interrupt attempt",
    
    "<a href='javascript:restart_sage();'>Restart</a>, instead?" : "<a href='javascript:restart_sage();'>Restart</a>, instead?",
    
    "Emptying the trash will permanently delete all items in the trash. Continue?" : "Emptying the trash will permanently delete all items in the trash. Continue?",
    
    "Get Image" : "Get Image",
    
    "Jmol Image" : "Jmol Image",
    
    "To save this image, you can try right-clicking on the image to copy it or save it to a file, or you may be able to just drag the image to your desktop." : "To save this image, you can try right-clicking on the image to copy it or save it to a file, or you may be able to just drag the image to your desktop.",
    
    "Sorry, but you need a browser that supports the &lt;canvas&gt; tag." : "Sorry, but you need a browser that supports the &lt;canvas&gt; tag.",
    
    1 : {
        
        'Trying again in %(num)d second...' : function (n) {return 'Trying again in 1 second...'.replace("%(num)d", 1)} 
        
    },
    2: {
        
        'Trying again in %(num)d second...' : function (n) {return 'Trying again in 2 seconds...'.replace("%(num)d", n)} 
        
    }
};


