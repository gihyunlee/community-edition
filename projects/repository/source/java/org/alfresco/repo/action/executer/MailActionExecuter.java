/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.action.executer;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.template.DateCompareMethod;
import org.alfresco.repo.template.HasAspectMethod;
import org.alfresco.repo.template.I18NMessageMethod;
import org.alfresco.repo.template.TemplateNode;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TemplateService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.util.Pair;
import org.alfresco.util.UrlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.mail.MailException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.util.StringUtils;

/**
 * Mail action executor implementation.
 * 
 * @author Roy Wetherall
 */
public class MailActionExecuter extends ActionExecuterAbstractBase
                                implements InitializingBean, TestModeable
{
    private static Log logger = LogFactory.getLog(MailActionExecuter.class);
    
    /**
     * Action executor constants
     */
    public static final String NAME = "mail";
    public static final String PARAM_LOCALE = "locale";
    public static final String PARAM_TO = "to";
    public static final String PARAM_TO_MANY = "to_many";
    public static final String PARAM_SUBJECT = "subject";
    public static final String PARAM_SUBJECT_PARAMS = "subjectParams";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_HTML = "html";
    public static final String PARAM_FROM = "from";
    public static final String PARAM_FROM_PERSONAL_NAME = "fromPersonalName";
    public static final String PARAM_TEMPLATE = "template";
    public static final String PARAM_TEMPLATE_MODEL = "template_model";
    public static final String PARAM_IGNORE_SEND_FAILURE = "ignore_send_failure";
    public static final String PARAM_SEND_AFTER_COMMIT = "send_after_commit";
   
    /**
     * From address
     */
    private static final String FROM_ADDRESS = "alfresco@alfresco.org";
    
    /**
     * The java mail sender
     */
    private JavaMailSender mailService;
    
    /**
     * The Template service
     */
    private TemplateService templateService;
    
    /**
     * The Person service
     */
    private PersonService personService;
    
    /**
     * The Authentication service
     */
    private AuthenticationService authService;
    
    /**
     * The Node Service
     */
    private NodeService nodeService;
    
    /**
     * The Authority Service
     */
    private AuthorityService authorityService;
    
    /**
     * The Service registry
     */
    private ServiceRegistry serviceRegistry;
    
    /**
     * System Administration parameters, including URL information
     */
    private SysAdminParams sysAdminParams;
    
    /**
     * The Preference Service
     */
    private PreferenceService preferenceService;
    
    /**
     * The Tenant Service
     */
    private TenantService tenantService;
    
    /**
     * Mail header encoding scheme
     */
    private String headerEncoding = null;
    
    /**
     * Default from address
     */
    private String fromDefaultAddress = null;
    
    /**
     * Is the from field enabled? Or must we always use the default address.
     */
    private boolean fromEnabled = true;
    
    
    private boolean sendTestMessage = false;
    private String testMessageTo = null;
    private String testMessageSubject = "Test message";
    private String testMessageText = "This is a test message.";

    private boolean validateAddresses = true;
    
    /**
     * Test mode prevents email messages from being sent.
     * It is used when unit testing when we don't actually want to send out email messages.
     * 
     * MER 20/11/2009 This is a quick and dirty fix. It should be replaced by being 
     * "mocked out" or some other better way of running the unit tests. 
     */
    private boolean testMode = false;
    private MimeMessage lastTestMessage;
    
    /**
     * @param javaMailSender    the java mail sender
     */
    public void setMailService(JavaMailSender javaMailSender) 
    {
        this.mailService = javaMailSender;
    }
    
    /**
     * @param templateService   the TemplateService
     */
    public void setTemplateService(TemplateService templateService)
    {
        this.templateService = templateService;
    }
    
    /**
     * @param personService     the PersonService
     */
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    public void setPreferenceService(PreferenceService preferenceService)
    {
    	this.preferenceService = preferenceService;
    }
    
    /**
     * @param authService       the AuthenticationService
     */
    public void setAuthenticationService(AuthenticationService authService)
    {
        this.authService = authService;
    }
    
    /**
     * @param serviceRegistry   the ServiceRegistry
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }
    
    /**
     * @param authorityService  the AuthorityService
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }
    
    /**
     * @param nodeService       the NodeService to set.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param tenantService       the TenantService to set.
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * @param headerEncoding     The mail header encoding to set.
     */
    public void setHeaderEncoding(String headerEncoding)
    {
        this.headerEncoding = headerEncoding;
    }
    
    /**
     * @param fromAddress   The default mail address.
     */
    public void setFromAddress(String fromAddress)
    {
        this.fromDefaultAddress = fromAddress;
    }

    
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }
    
    public void setTestMessageTo(String testMessageTo)
    {
        this.testMessageTo = testMessageTo;
    }
    
    public String getTestMessageTo()
    {
        return testMessageTo;
    }
    
    public void setTestMessageSubject(String testMessageSubject)
    {
        this.testMessageSubject = testMessageSubject;
    }
    
    public void setTestMessageText(String testMessageText)
    {
        this.testMessageText = testMessageText;
    }

    public void setSendTestMessage(boolean sendTestMessage)
    {
        this.sendTestMessage = sendTestMessage;
    }
    
    /**
     * This stores an email address which, if it is set, overrides ALL email recipients sent from
     * this class. It is intended for dev/test usage only !!
     */
    private String testModeRecipient;

    /**
     * Send a test message
     * 
     * @return true, message sent 
     * @throws AlfrescoRuntimeException 
     */
    public boolean sendTestMessage() 
    {
        if(testMessageTo == null || testMessageTo.length() == 0)
        {
            throw new AlfrescoRuntimeException("email.outbound.err.test.no.to");
        }
        if(testMessageSubject == null || testMessageSubject.length() == 0)
        {
            throw new AlfrescoRuntimeException("email.outbound.err.test.no.subject");
        }
        if(testMessageText == null || testMessageText.length() == 0)
        {
            throw new AlfrescoRuntimeException("email.outbound.err.test.no.text");
        }
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put(PARAM_TO, testMessageTo);
        params.put(PARAM_SUBJECT, testMessageSubject);
        params.put(PARAM_TEXT, testMessageText);
        
        Action ruleAction = serviceRegistry.getActionService().createAction(NAME, params);
        
        MimeMessageHelper message = prepareEmail(ruleAction, null,
                new Pair<String, Locale>(testMessageTo, getLocaleForUser(testMessageTo)), getFrom(ruleAction));
        try
        {
            mailService.send(message.getMimeMessage());
            onSend();
        }
        catch (MailException me)
        {
            onFail();
            StringBuffer txt = new StringBuffer();
            
            txt.append(me.getClass().getName() + ", " + me.getMessage());
            
            Throwable cause = me.getCause();
            while (cause != null)
            {
                txt.append(", ");
                txt.append(cause.getClass().getName() + ", " + cause.getMessage());
                cause = cause.getCause();
            }
            
            Object[] args = {testMessageTo, txt.toString()};
            throw new AlfrescoRuntimeException("email.outbound.err.send.failed", args, me);
        }
        
        return true;
    }
    
    public void setTestModeRecipient(String testModeRecipient)
    {
        this.testModeRecipient = testModeRecipient;
    }

    public void setValidateAddresses(boolean validateAddresses)
    {
        this.validateAddresses = validateAddresses;
    }

    
    @Override
    public void init()
    {
        numberSuccessfulSends.set(0);
        numberFailedSends.set(0);
        
        super.init();
        if (sendTestMessage && testMessageTo != null)
        {
            AuthenticationUtil.runAs(new RunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    Map<String, Serializable> params = new HashMap<String, Serializable>();
                    params.put(PARAM_TO, testMessageTo);
                    params.put(PARAM_SUBJECT, testMessageSubject);
                    params.put(PARAM_TEXT, testMessageText);

                    Action ruleAction = serviceRegistry.getActionService().createAction(NAME, params);
                    executeImpl(ruleAction, null);
                    return null;
                }
            }, AuthenticationUtil.getSystemUserName());
        }
    }

    /**
     * Initialise bean
     */
    public void afterPropertiesSet() throws Exception
    {
        if (fromDefaultAddress == null || fromDefaultAddress.length() == 0)
        {
            fromDefaultAddress = FROM_ADDRESS;
        }
        
    }
    
    /**
     * Send an email message
     * 
     * @throws AlfrescoRuntimeExeption
     */
    @Override
    protected void executeImpl(
            final Action ruleAction,
            final NodeRef actionedUponNodeRef) 
    {
        // TODO review & test converged code !!
        if (sendAfterCommit(ruleAction))
        {
            AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter()
            {
                @Override
                public void afterCommit()
                {
                    RetryingTransactionHelper helper = serviceRegistry.getRetryingTransactionHelper();
                    helper.doInTransaction(new RetryingTransactionCallback<Void>()
                    {
                        @Override
                        public Void execute() throws Throwable
                        {
                            if (validNodeRefIfPresent(actionedUponNodeRef))
                            {
                                prepareAndSendEmail(ruleAction, actionedUponNodeRef);
                            }
                            return null;
                        }
                    }, false, true);
                }
            });
        }
        else
        {
            if (validNodeRefIfPresent(actionedUponNodeRef))
            {
                prepareAndSendEmail(ruleAction, actionedUponNodeRef);
            }
        }
    }
    
    
    private boolean validNodeRefIfPresent(NodeRef actionedUponNodeRef)
    {
        if (actionedUponNodeRef == null)
        {
            // We must expect that null might be passed in (ALF-11625)
            // since the mail action might not relate to a specific nodeRef.
            return true;
        }
        else
        {
            // Only try and send the email if the actioned upon node reference still exists
            // (i.e. if one has been specified it must be valid)
            return nodeService.exists(actionedUponNodeRef);
        }
    }
    
    private boolean sendAfterCommit(Action action)
    {
        Boolean sendAfterCommit = (Boolean) action.getParameterValue(PARAM_SEND_AFTER_COMMIT);
        return sendAfterCommit == null ? false : sendAfterCommit.booleanValue();
    }
    
    private void prepareAndSendEmail(final Action ruleAction, final NodeRef actionedUponNodeRef)
    {
        List<Pair<String, Locale>> recipients = getRecipients(ruleAction);
        
        Pair<InternetAddress, Locale> from = getFrom(ruleAction);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("From: address=" + from.getFirst() + " ,locale=" + from.getSecond());
        }
        
        for (Pair<String, Locale> recipient : recipients)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Recipient: address=" + recipient.getFirst() + " ,locale=" + recipient.getSecond());
            }
            
            prepareAndSendEmail(ruleAction, actionedUponNodeRef, recipient, from);
        }
    }
    
    public MimeMessageHelper prepareEmail(final Action ruleAction , final NodeRef actionedUponNodeRef, final Pair<String, Locale> recipient, final Pair<InternetAddress, Locale> sender)
    {
        // Create the mime mail message.
        // Hack: using an array here to get around the fact that inner classes aren't closures.
        // The MimeMessagePreparator.prepare() signature does not allow us to return a value and yet
        // we can't set a result on a bare, non-final object reference due to Java language restrictions.
        final MimeMessageHelper[] messageRef = new MimeMessageHelper[1];
        MimeMessagePreparator mailPreparer = new MimeMessagePreparator()
        {
            @SuppressWarnings("unchecked")
            public void prepare(MimeMessage mimeMessage) throws MessagingException
            {
                if (logger.isDebugEnabled())
                {
                   logger.debug(ruleAction.getParameterValues());
                }
                
                messageRef[0] = new MimeMessageHelper(mimeMessage);
                
                // set header encoding if one has been supplied
                if (headerEncoding != null && headerEncoding.length() != 0)
                {
                    mimeMessage.setHeader("Content-Transfer-Encoding", headerEncoding);
                }
                
                // set recipient
                String to = (String)ruleAction.getParameterValue(PARAM_TO);
                if (to != null && to.length() != 0)
                {
                    messageRef[0].setTo(to);
                }
                else
                {
                    // see if multiple recipients have been supplied - as a list of authorities
                    Serializable authoritiesValue = ruleAction.getParameterValue(PARAM_TO_MANY);
                    List<String> authorities = null;
                    if (authoritiesValue != null)
                    {
                        if (authoritiesValue instanceof String)
                        {
                            authorities = new ArrayList<String>(1);
                            authorities.add((String)authoritiesValue);
                        }
                        else
                        {
                            authorities = (List<String>)authoritiesValue;
                        }
                    }
                    
                    if (authorities != null && authorities.size() != 0)
                    {
                        List<String> recipients = new ArrayList<String>(authorities.size());
                        for (String authority : authorities)
                        {
                            AuthorityType authType = AuthorityType.getAuthorityType(authority);
                            if (authType.equals(AuthorityType.USER))
                            {
                                if (personService.personExists(authority) == true)
                                {
                                    NodeRef person = personService.getPerson(authority);
                                    String address = (String)nodeService.getProperty(person, ContentModel.PROP_EMAIL);
                                    if (address != null && address.length() != 0 && validateAddress(address))
                                    {
                                        recipients.add(address);
                                    }
                                }
                            }
                            else if (authType.equals(AuthorityType.GROUP) || authType.equals(AuthorityType.EVERYONE))
                            {
                                // Notify all members of the group
                                Set<String> users;
                                if (authType.equals(AuthorityType.GROUP))
                                {        
                                    users = authorityService.getContainedAuthorities(AuthorityType.USER, authority, false);
                                }
                                else
                                {
                                    users = authorityService.getAllAuthorities(AuthorityType.USER);
                                }
                                
                                for (String userAuth : users)
                                {
                                    if (personService.personExists(userAuth) == true)
                                    {
                                        NodeRef person = personService.getPerson(userAuth);
                                        String address = (String)nodeService.getProperty(person, ContentModel.PROP_EMAIL);
                                        if (address != null && address.length() != 0)
                                        {
                                            recipients.add(address);
                                        }
                                    }
                                }
                            }
                        }
                        
                        if(recipients.size() > 0)
                        {
                            messageRef[0].setTo(recipients.toArray(new String[recipients.size()]));
                        }
                        else
                        {
                            // All recipients were invalid
                            throw new MailPreparationException(
                                    "All recipients for the mail action were invalid"
                            );
                        }
                    }
                    else
                    {
                        // No recipients have been specified
                        throw new MailPreparationException(
                                "No recipient has been specified for the mail action"
                        );
                    }
                }
                
                // from person
                NodeRef fromPerson = null;
            
                // from is enabled
                if (! authService.isCurrentUserTheSystemUser())
                {
                    fromPerson = personService.getPerson(authService.getCurrentUserName());
                }
                
                if(isFromEnabled())
                {   
                    // Use the FROM parameter in preference to calculating values.
                    String from = (String)ruleAction.getParameterValue(PARAM_FROM);
                    if (from != null && from.length() > 0)
                    {
                        if(logger.isDebugEnabled())
                        {
                            logger.debug("from specified as a parameter, from:" + from);
                        }
                        
                        // Check whether or not to use a personal name for the email (will be RFC 2047 encoded)
                        String fromPersonalName = (String)ruleAction.getParameterValue(PARAM_FROM_PERSONAL_NAME);
                        if(fromPersonalName != null && fromPersonalName.length() > 0) 
                        {
                            try
                            {
                                messageRef[0].setFrom(from, fromPersonalName);
                            }
                            catch (UnsupportedEncodingException error)
                            {
                                // Uses the JVM's default encoding, can never be unsupported. Just in case, revert to simple email
                                messageRef[0].setFrom(from);
                            }
                        }
                        else
                        {
                            messageRef[0].setFrom(from);
                        }
                    }
                    else
                    {
                        // set the from address from the current user
                        String fromActualUser = null;
                        if (fromPerson != null)
                        {
                            fromActualUser = (String) nodeService.getProperty(fromPerson, ContentModel.PROP_EMAIL);
                        }
                    
                        if (fromActualUser != null && fromActualUser.length() != 0)
                        {
                            if(logger.isDebugEnabled())
                            {
                                logger.debug("looked up email address for :" + fromPerson + " email from " + fromActualUser);
                            }
                            messageRef[0].setFrom(fromActualUser);
                        }
                        else
                        {
                            // from system or user does not have email address
                            messageRef[0].setFrom(fromDefaultAddress);
                        }
                    }

                }
                else
                {
                    if(logger.isDebugEnabled())
                    {
                        logger.debug("from not enabled - sending from default address:" + fromDefaultAddress);
                    }
                    // from is not enabled.
                    messageRef[0].setFrom(fromDefaultAddress);
                }
                


                
                // set subject line
                messageRef[0].setSubject((String)ruleAction.getParameterValue(PARAM_SUBJECT));
                
                if ((testModeRecipient != null) && (testModeRecipient.length() > 0) && (! testModeRecipient.equals("${dev.email.recipient.address}")))
                {
                    // If we have an override for the email recipient, we'll send the email to that address instead.
                    // We'll prefix the subject with the original recipient, but leave the email message unchanged in every other way.
                    messageRef[0].setTo(testModeRecipient);
                    
                    String emailRecipient = (String)ruleAction.getParameterValue(PARAM_TO);
                    if (emailRecipient == null)
                    {
                       Object obj = ruleAction.getParameterValue(PARAM_TO_MANY);
                       if (obj != null)
                       {
                           emailRecipient = obj.toString();
                       }
                    }
                    
                    String recipientPrefixedSubject = "(" + emailRecipient + ") " + (String)ruleAction.getParameterValue(PARAM_SUBJECT);
                    
                    messageRef[0].setSubject(recipientPrefixedSubject);
                }
                
                
                // See if an email template has been specified
                String text = null;
                
                // templateRef: either a nodeRef or classpath (see ClasspathRepoTemplateLoader)
                Serializable ref = ruleAction.getParameterValue(PARAM_TEMPLATE);
                String templateRef = (ref instanceof NodeRef ? ((NodeRef)ref).toString() : (String)ref);
                if (templateRef != null)
                {
                    Map<String, Object> suppliedModel = null;
                    if(ruleAction.getParameterValue(PARAM_TEMPLATE_MODEL) != null)
                    {
                        Object m = ruleAction.getParameterValue(PARAM_TEMPLATE_MODEL);
                        if(m instanceof Map)
                        {
                            suppliedModel = (Map<String, Object>)m;
                        }
                        else
                        {
                            logger.warn("Skipping unsupported email template model parameters of type "
                                    + m.getClass().getName() + " : " + m.toString());
                        }
                    }
                    
                    // build the email template model
                    Map<String, Object> model = createEmailTemplateModel(actionedUponNodeRef, suppliedModel, fromPerson);

                    // Determine the locale to use to send the email.
                    Locale locale = recipient.getSecond();
                    if (locale == null)
                    {
                        locale = (Locale)ruleAction.getParameterValue(PARAM_LOCALE);
                    }
                    if (locale == null)
                    {
                    	locale = sender.getSecond();
                    }
                    
                    // set subject line
                    String subject = (String)ruleAction.getParameterValue(PARAM_SUBJECT);
                    Object[] subjectParams = (Object[])ruleAction.getParameterValue(PARAM_SUBJECT_PARAMS);
                    String localizedSubject = getLocalizedSubject(subject, subjectParams, locale);
                    if (locale == null)
                    {
    					// process the template against the model
                        text = templateService.processTemplate("freemarker", templateRef, model);
                    }
                    else
                    {
						// process the template against the model
	                    text = templateService.processTemplate("freemarker", templateRef, model, locale);
                    }
                    if ((testModeRecipient != null) && (testModeRecipient.length() > 0) && (! testModeRecipient.equals("${dev.email.recipient.address}")))
                    {
                        // If we have an override for the email recipient, we'll send the email to that address instead.
                        // We'll prefix the subject with the original recipient, but leave the email message unchanged in every other way.
                        messageRef[0].setTo(testModeRecipient);
                        
                        String emailRecipient = recipient.getFirst();
                        
                        String recipientPrefixedSubject = "(" + emailRecipient + ") " + localizedSubject;
                        
                        messageRef[0].setSubject(recipientPrefixedSubject);
                    }
                    else 
                    {
                        messageRef[0].setTo(recipient.getFirst());
                        messageRef[0].setSubject(localizedSubject);
                    }
                }
                
                // set the text body of the message
                
                boolean isHTML = false;
                if (text == null)
                {
                    text = (String)ruleAction.getParameterValue(PARAM_TEXT);
                }
                
                if (text != null)
                {
                    // Note: only simplistic match here - expects <html tag at the start of the text
                    String htmlPrefix = "<html";
                    String trimmedText = text.trim();
                    if (trimmedText.length() >= htmlPrefix.length() &&
                            trimmedText.substring(0, htmlPrefix.length()).equalsIgnoreCase(htmlPrefix))
                    {
                        isHTML = true;
                    }
                }
                else
                {
                    text = (String)ruleAction.getParameterValue(PARAM_HTML);
                    if (text != null)
                    {
                        // assume HTML
                        isHTML = true;
                    }
                }
                
                if (text != null)
                {
                    messageRef[0].setText(text, isHTML);
                }
                
            }
        };
        MimeMessage mimeMessage = mailService.createMimeMessage(); 
        try
        {
            mailPreparer.prepare(mimeMessage);
        } catch (Exception e)
        {
            // We're forced to catch java.lang.Exception here. Urgh.
            if (logger.isInfoEnabled())
            {
                logger.warn("Unable to prepare mail message. Skipping.", e);
            }
        }
        
        return messageRef[0];
    }
    
    private void prepareAndSendEmail(final Action ruleAction, final NodeRef actionedUponNodeRef, final Pair<String, Locale> recipient, final Pair<InternetAddress, Locale> sender)
    {
        MimeMessageHelper preparedMessage = prepareEmail(ruleAction, actionedUponNodeRef, recipient, sender);

        try
        {
            // Send the message unless we are in "testMode"
            if (!testMode)
            {
                mailService.send(preparedMessage.getMimeMessage());
                onSend();
            }
            else
            {
                lastTestMessage = preparedMessage.getMimeMessage();
            }
        }
        catch (MailException e)
        {
            onFail();
            String to = (String)ruleAction.getParameterValue(PARAM_TO);
            if (to == null)
            {
               Object obj = ruleAction.getParameterValue(PARAM_TO_MANY);
               if (obj != null)
               {
                  to = obj.toString();
               }
            }
            
            // always log the failure
            logger.error("Failed to send email to " + to, e);
            
            // optionally ignore the throwing of the exception
            Boolean ignoreError = (Boolean)ruleAction.getParameterValue(PARAM_IGNORE_SEND_FAILURE);
            if (ignoreError == null || ignoreError.booleanValue() == false)
            {
                Object[] args = {to, e.toString()};
                throw new AlfrescoRuntimeException("email.outbound.err.send.failed", args, e);
            }   
        }
    }
    
    /**
     * Attempt to localize the subject, using the subject parameter as the message key.
     * 
     * @param subject Message key for subject lookup
     * @param params Parameters for the message
     * @param locale Locale to use
     * @return The localized message, or subject if the message format could not be found
     */
    private String getLocalizedSubject(String subject, Object[] params, Locale locale)
    {
    	String localizedSubject = null;
        if (locale == null)
        {
        	localizedSubject = I18NUtil.getMessage(subject, params);
        }
        else 
        {
        	localizedSubject = I18NUtil.getMessage(subject, locale, params);
        }
        
        if (localizedSubject == null)
        {
        	return subject;
        }
        else
        {
        	return localizedSubject;
        }
        
    }
    
    private Pair<InternetAddress, Locale> getFrom(Action ruleAction)
    {
        try 
        {
            InternetAddress address;
            Locale locale = null;
            // from person
            String fromPersonName = null;
        
            // from is enabled
            if (! authService.isCurrentUserTheSystemUser())
            {
                String currentUserName = authService.getCurrentUserName();
                if (currentUserName != null && personExists(currentUserName))
                {
                    fromPersonName = currentUserName;
                    locale = getLocaleForUser(fromPersonName);
                }
            }

            // Use the FROM parameter in preference to calculating values.
            String from = (String)ruleAction.getParameterValue(PARAM_FROM);
            if (from != null && from.length() > 0)
            {
                if(logger.isDebugEnabled())
                {
                    logger.debug("from specified as a parameter, from:" + from);
                }
                
                // Check whether or not to use a personal name for the email (will be RFC 2047 encoded)
                String fromPersonalName = (String)ruleAction.getParameterValue(PARAM_FROM_PERSONAL_NAME);
                if(fromPersonalName != null && fromPersonalName.length() > 0) 
                {
                    try
                    {
                    	address = new InternetAddress(from, fromPersonalName);
                    }
                    catch (UnsupportedEncodingException error)
                    {
                    	address = new InternetAddress(from);
                    }
                }
                else
                {
                    address = new InternetAddress(from);
                }
                if (locale == null)
                {
                    if (personExists(from))
                    {
                        locale = getLocaleForUser(from);
                    }
                }
            }
            else
            {
                if (fromPersonName != null && fromPersonName.length() != 0)
                {
                    address = new InternetAddress(fromPersonName);
                }
                else
                {
                    // from system or user does not have email address
                    address = new InternetAddress(fromDefaultAddress);
                }
            }
            
            return new Pair<InternetAddress, Locale>(address, locale);
        }
        catch (MessagingException ex)
        {
            throw new AlfrescoRuntimeException("Failed to resolve sender mail address");
        }
    }
    
    private List<Pair<String, Locale>> getRecipients(Action ruleAction) 
    {
    	
    	List<Pair<String, Locale>> recipients = new LinkedList<Pair<String,Locale>>();
    	
        // set recipient
        String to = (String)ruleAction.getParameterValue(PARAM_TO);
        if (to != null && to.length() != 0)
        {
        	Locale locale = null;
        	if (personExists(to))
        	{
        		locale = getLocaleForUser(to);
        	}
        	recipients.add(new Pair<String, Locale>(to, locale));
        }
        else
        {
            // see if multiple recipients have been supplied - as a list of authorities
            Serializable authoritiesValue = ruleAction.getParameterValue(PARAM_TO_MANY);
            List<String> authorities = null;
            if (authoritiesValue != null)
            {
                if (authoritiesValue instanceof String)
                {
                    authorities = new ArrayList<String>(1);
                    authorities.add((String)authoritiesValue);
                }
                else
                {
                    authorities = (List<String>)authoritiesValue;
                }
            }
            
            if (authorities != null && authorities.size() != 0)
            {
                for (String authority : authorities)
                {
                    AuthorityType authType = AuthorityType.getAuthorityType(authority);
                    if (authType.equals(AuthorityType.USER))
                    {
                        if (personExists(authority))
                        {
                            if (authority != null && authority.length() != 0 && validateAddress(authority))
                            {
                            	Locale locale = getLocaleForUser(authority);
                                recipients.add(new Pair<String, Locale>(authority, locale));
                            }
                        }
                    }
                    else if (authType.equals(AuthorityType.GROUP) || authType.equals(AuthorityType.EVERYONE))
                    {
                        // Notify all members of the group
                        Set<String> users;
                        if (authType.equals(AuthorityType.GROUP))
                        {        
                            users = authorityService.getContainedAuthorities(AuthorityType.USER, authority, false);
                        }
                        else
                        {
                            users = authorityService.getAllAuthorities(AuthorityType.USER);
                        }
                        
                        for (String userAuth : users)
                        {
                            if (personExists(userAuth))
                            {
                                if (userAuth != null && userAuth.length() != 0  && validateAddress(userAuth))
                                {
                                	Locale locale = getLocaleForUser(userAuth);
                                    recipients.add(new Pair<String, Locale>(userAuth, locale));
                                }
                            }
                        }
                    }
                }
                if(recipients.size() <= 0)
                {
                    // All recipients were invalid
                    throw new MailPreparationException(
                            "All recipients for the mail action were invalid"
                    );
                }
            }
            else
            {
                // No recipients have been specified
                throw new MailPreparationException(
                        "No recipient has been specified for the mail action"
                );
            }
        }
        return recipients;
    }
    
    @SuppressWarnings("deprecation")
    public boolean personExists(final String user)
    {
        boolean exists = false;
        String domain = getDomain(user);
        if (tenantService.getTenant(domain) != null)
        {
            exists = TenantUtil.runAsTenant(new TenantRunAsWork<Boolean>()
            {
                public Boolean doWork() throws Exception
                {
                    return personService.personExists(user);
                }
            }, domain);
        }
        else
        {
            exists = personService.personExists(user);
        }
        return exists;
    }
    
    @SuppressWarnings("deprecation")
    public NodeRef getPerson(final String user)
    {
        NodeRef person = null;
        String domain = getDomain(user);
        if (tenantService.getTenant(domain) != null)
        {
            person = TenantUtil.runAsTenant(new TenantRunAsWork<NodeRef>()
            {
                public NodeRef doWork() throws Exception
                {
                    return personService.getPerson(user);
                }
            }, domain);
        }
        else
        {
            person = personService.getPerson(user);
        }
        return person;
    }
    
    @SuppressWarnings("deprecation")
    private Locale getLocaleForUser(final String user)
    {
        Locale locale = null;
        String domain = getDomain(user);
        if (tenantService.getTenant(domain) != null)
        {
            locale = TenantUtil.runAsTenant(new TenantRunAsWork<Locale>()
            {
                public Locale doWork() throws Exception
                {
                    return getLocaleForUserImpl(user);
                }
            }, domain);
        }
        else
        {
            return getLocaleForUserImpl(user);
        }
        return locale;
    }

    private Locale getLocaleForUserImpl(String user)
    {
        Locale locale = null;
        String localeString = (String)preferenceService.getPreference(user, "locale");
        if (localeString != null)
        {
            locale = StringUtils.parseLocaleString(localeString);
        }
        return locale;
    }

    private String getDomain(String user)
    {
        String[] parts = user.split("@");
        return parts.length == 1 ? "" : parts[1].toLowerCase(I18NUtil.getLocale());
    }
    
    /**
     * Return true if address has valid format
     * @param address
     * @return
     */
    private boolean validateAddress(String address)
    {
        boolean result = false;
        
        // Validate the email, allowing for local email addresses
        EmailValidator emailValidator = EmailValidator.getInstance(true);
        if (!validateAddresses || emailValidator.isValid(address))
        {
            result = true;
        }
        else 
        {
            logger.error("Failed to send email to '" + address + "' as the address is incorrectly formatted" );
        }
      
        return result;
    }

   /**
    * @param ref    The node representing the current document ref (or null)
    * 
    * @return Model map for email templates
    */
   private Map<String, Object> createEmailTemplateModel(NodeRef ref, Map<String, Object> suppliedModel, NodeRef fromPerson)
   {
      Map<String, Object> model = new HashMap<String, Object>(8, 1.0f);
      
      if (fromPerson != null)
      {
          model.put("person", new TemplateNode(fromPerson, serviceRegistry, null));
      }      
      
      if (ref != null)
      {
          model.put("document", new TemplateNode(ref, serviceRegistry, null));
          NodeRef parent = serviceRegistry.getNodeService().getPrimaryParent(ref).getParentRef();
          model.put("space", new TemplateNode(parent, serviceRegistry, null));
      }
      
      // current date/time is useful to have and isn't supplied by FreeMarker by default
      model.put("date", new Date());
      
      // add custom method objects
      model.put("hasAspect", new HasAspectMethod());
      model.put("message", new I18NMessageMethod());
      model.put("dateCompare", new DateCompareMethod());
      
      // add URLs
      model.put("url", new URLHelper(sysAdminParams));
      model.put(TemplateService.KEY_SHARE_URL, UrlUtil.getShareUrl(this.serviceRegistry.getSysAdminParams()));
      
      // if the caller specified a model, use it without overriding
      if(suppliedModel != null && suppliedModel.size() > 0)
      {
          for(String key : suppliedModel.keySet())
          {
              if(model.containsKey(key))
              {
                  if(logger.isDebugEnabled())
                  {
                      logger.debug("Not allowing overwriting of built in model parameter " + key);
                  }
              }
              else
              {
                  model.put(key, suppliedModel.get(key));
              }
          }
      }
      
      // all done
      return model;
   }
    
    /**
     * Add the parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_TO, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TO)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TO_MANY, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_TO_MANY), true));
        paramList.add(new ParameterDefinitionImpl(PARAM_SUBJECT, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_SUBJECT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEXT, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_TEXT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_FROM, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_FROM)));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE, DataTypeDefinition.NODE_REF, false, getParamDisplayLabel(PARAM_TEMPLATE), false, "ac-email-templates"));
        paramList.add(new ParameterDefinitionImpl(PARAM_TEMPLATE_MODEL, DataTypeDefinition.ANY, false, getParamDisplayLabel(PARAM_TEMPLATE_MODEL), true));
        paramList.add(new ParameterDefinitionImpl(PARAM_IGNORE_SEND_FAILURE, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_IGNORE_SEND_FAILURE)));
    }

    public void setTestMode(boolean testMode)
    {
        this.testMode = testMode;
    }

    public boolean isTestMode()
    {
        return testMode;
    }

    /**
     * Returns the most recent message that wasn't sent
     *  because TestMode had been enabled.
     */
    public MimeMessage retrieveLastTestMessage()
    {
        return lastTestMessage; 
    }
    
    /**
     * Used when test mode is enabled.
     * Clears the record of the last message that was sent. 
     */
    public void clearLastTestMessage()
    {
        lastTestMessage = null;
    }

    public void setFromEnabled(boolean fromEnabled)
    {
        this.fromEnabled = fromEnabled;
    }

    public boolean isFromEnabled()
    {
        return fromEnabled;
    }

    public static class URLHelper
    {
        private final SysAdminParams sysAdminParams;
        
        public URLHelper(SysAdminParams sysAdminParams)
        {
            this.sysAdminParams = sysAdminParams;
        }
        
        public String getContext()
        {
           return "/" + sysAdminParams.getAlfrescoContext();
        }

        public String getServerPath()
        {
            return sysAdminParams.getAlfrescoProtocol() + "://" + sysAdminParams.getAlfrescoHost() + ":"
                    + sysAdminParams.getAlfrescoPort();
        }
    }
    
    static AtomicInteger numberSuccessfulSends = new AtomicInteger(0);
    static AtomicInteger numberFailedSends = new AtomicInteger(0);
    protected void onSend()
    {
        numberSuccessfulSends.getAndIncrement();
    }
    
    protected void onFail()
    {
        numberFailedSends.getAndIncrement();
    }
    
    public int getNumberSuccessfulSends()
    {
        return numberSuccessfulSends.get();
    }
        
    public int getNumberFailedSends()
    {   
        return numberFailedSends.get();
    }
}
