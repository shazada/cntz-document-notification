(function() {
    YAHOO.Bubbling.fire("registerAction", {
	actionName : "notifyAction",
	fn : function moveToFolder(file) {
	    this.modules.actions.genericAction({
		success : {
		    events : [ {
			name : "metadataRefresh"
		    } ],
		    callback : {
			fn : function DL_oAN_success(data) {
			    var resultJson = YAHOO.lang.JSON.parse(data.serverResponse.responseText);
			    var added = resultJson.results[0].added;
			    if (added)
				Alfresco.util.PopupManager.displayMessage({
				    text : this.msg("message.notify.added", file.displayName)
				});
			    else
				Alfresco.util.PopupManager.displayMessage({
				    text : this.msg("message.notify.removed", file.displayName)
				});
			},
			scope : this
		    }
		},
		failure : {
		    message : this.msg("message.notify.failure", file.displayName)
		},
		webscript : {
		    name : "contezza/document/notify/site/{site}/{container}?user=" + Alfresco.constants.USERNAME,
		    stem: Alfresco.constants.PROXY_URI,
		    method : Alfresco.util.Ajax.POST
		},
		params : {
		    site : this.options.siteId,
		    container : this.options.containerId
		},
		config : {
		    requestContentType : Alfresco.util.Ajax.JSON,
		    dataObj : {
			nodeRefs : [ file.nodeRef ]
		    }
		}
	    });
	}
    });
})();