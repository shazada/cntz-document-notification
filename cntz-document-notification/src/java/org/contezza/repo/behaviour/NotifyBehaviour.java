/**
 * 
 */
package org.contezza.repo.behaviour;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * @author Tahir
 * 
 */
public class NotifyBehaviour implements NodeServicePolicies.OnUpdateNodePolicy, NodeServicePolicies.OnUpdatePropertiesPolicy {

	private static Logger logger = Logger.getLogger(NotifyBehaviour.class);

	private PolicyComponent policyComponent;
	private NodeService nodeService;
	private SearchService searchService;
	private ActionService actionService;
	// should be an XPath
	private String emailTemplate = "/app:company_home/app:dictionary/app:email_templates/app:notify_email_templates/cm:notify_user_email.ftl";

	public static final QName ASPECT_NOTIFY = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subscribable");
	public static final QName PROP_NOTIFY = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subscribedBy");

	public void init() {
		// policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdateNodePolicy.QNAME,
		// ASPECT_NOTIFY, new JavaBehaviour(this,
		// "onUpdateNode"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ASPECT_NOTIFY, new JavaBehaviour(this,
				"onUpdateProperties"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see OnUpdateNodePolicy#onUpdateNode
	 * (org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	public void onUpdateNode(NodeRef actionedUponNodeRef) {

		try {
			logger.debug("Triggering NotifyBehaviour");
			Action action = actionService.createAction("mail");

			List<Serializable> recipients = new ArrayList<Serializable>();
			List<AssociationRef> assocsList = nodeService.getTargetAssocs(actionedUponNodeRef, PROP_NOTIFY);
			for (AssociationRef associationRef : assocsList) {
				recipients.add(nodeService.getProperty(associationRef.getTargetRef(), ContentModel.PROP_USERNAME));
			}
			logger.debug("Recipients to send mail: " + recipients.size());
			if (recipients.size() > 0) {
				action.setParameterValue(MailActionExecuter.PARAM_TO_MANY, (Serializable) recipients);
				action.setParameterValue(MailActionExecuter.PARAM_SUBJECT,
						"Nofication: Document '" + nodeService.getProperty(actionedUponNodeRef, ContentModel.PROP_NAME)
								+ "' has been updated");
				// Getting nodeRef of template
				ResultSet results = searchService.query(actionedUponNodeRef.getStoreRef(), SearchService.LANGUAGE_XPATH, emailTemplate);
				action.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, results.getNodeRef(0));

				actionService.executeAction(action, actionedUponNodeRef);
				logger.debug("Mail send");
			}
		} catch (InvalidNodeRefException e) {
			logger.error("Error getting NodeRef " + e.getMessage() + e.getCause());
		} catch (Exception e) {
			logger.error("Error firing mail Action " + e.getMessage() + e.getCause());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#
	 * onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef,
	 * java.util.Map, java.util.Map)
	 */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		Serializable beforeVersion = before.get(ContentModel.PROP_VERSION_LABEL);
		if (beforeVersion == null)
			return;
		else if (!beforeVersion.equals(after.get(ContentModel.PROP_VERSION_LABEL))){
			logger.debug("Versions: " + beforeVersion + ", " + after.get(ContentModel.PROP_VERSION_LABEL));
			onUpdateNode(nodeRef);
		}
	}

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	public void setEmailTemplate(String emailTemplate) {
		this.emailTemplate = emailTemplate;
	}

	public void setSearchService(SearchService search) {
		this.searchService = search;
	}
}