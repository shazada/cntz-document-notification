<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

/**
 * Notify on changes action
 * 
 * @method POST
 */

/**
 * Entrypoint required by action.lib.js
 * 
 * @method runAction
 * @param p_params
 *            {object} Object literal containing files array
 * @return {object|null} object representation of action results
 */



function runAction(p_params) {
	var results = [];
	var nodeString = p_params.files[0];	
	var nodeRef = utils.getNodeFromString(nodeString);
	var aspect = "cm:subscribable";
	var result = {
	         //nodeRef: null,
	         action: "notifyAction",
	         success: true,
	         added: false
	      }
	
	logger.log("name " + nodeRef.name);
	
	if (nodeRef.hasAspect(aspect)){
		if (addNotification(nodeRef))
			result.added = true;
	} else {
		nodeRef.addAspect(aspect);
		if (addNotification(nodeRef))
			result.added = true;
	}
	
	results.push(result);

	return results;
}

function addNotification(nodeRef){
	var add = true;
	var assocs = "cm:subscribedBy";
	logger.log("user " + args["user"]);
	var user = people.getPerson(args["user"]);
	logger.log("current person " + person.id + " real Person " + user.id);
	
	for each(assoc in nodeRef.assocs[assocs]){
		logger.log("assoc " + assoc.id);
		if (assoc.properties["cm:userName"] == user.properties["cm:userName"]){
			logger.log("removing notification");
			nodeRef.removeAssociation(user, assocs);
			add = false;
			break;
		}
	}
	
	if (add)
		nodeRef.createAssociation(user, assocs);
	return add;
}

/* Bootstrap action script */
main();