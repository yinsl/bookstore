/**
 * Copyright 2005 Jasper Systems, Inc. All rights reserved.
 *
 * This software code is the confidential and proprietary information of
 * Jasper Systems, Inc. ("Confidential Information"). Any unauthorized
 * review, use, copy, disclosure or distribution of such Confidential
 * Information is strictly prohibited.
 */
package com.maxus.tsp.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.maxus.tsp.common.util.conf.LiantongSMSProperties;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSProcessor;
import com.sun.xml.wss.XWSSProcessorFactory;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;

@Component
public class LianTongSMSUtil {
	
	private static final Logger logger = LogManager.getLogger(LianTongSMSUtil.class);
	
	private LiantongSMSProperties liantongSMSProperties;
	
    private SOAPConnectionFactory connectionFactory;
    
    private MessageFactory messageFactory;
    
    private XWSSProcessorFactory processorFactory;
    
    public void setLiantongSMSProperties(LiantongSMSProperties liantongSMSProperties) {
		this.liantongSMSProperties = liantongSMSProperties;
	}

	public LianTongSMSUtil() throws SOAPException, MalformedURLException, XWSSecurityException {
    	connectionFactory = SOAPConnectionFactory.newInstance();
    	messageFactory = MessageFactory.newInstance();
    	processorFactory = XWSSProcessorFactory.newInstance();
    }
    
    private SOAPMessage createSMSRequest(String iccid, String msg) throws SOAPException {
        SOAPMessage message = messageFactory.createMessage();
        message.getMimeHeaders().addHeader("SOAPAction", liantongSMSProperties.getSoapAction());
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        
        Name sendSMSRequestName = envelope.createName("SendSMSRequest", liantongSMSProperties.getPrefix(), liantongSMSProperties.getNamespaceUrl());
        
        SOAPBodyElement sendSMSRequestElement = message.getSOAPBody()
                .addBodyElement(sendSMSRequestName);
        
        Name msgId = envelope.createName("messageId", liantongSMSProperties.getPrefix(), liantongSMSProperties.getNamespaceUrl());
        logger.info("msgId::: name: " + msgId.getQualifiedName() + ", uri:::" + msgId.getURI());
        SOAPElement msgElement = sendSMSRequestElement.addChildElement(msgId);
        msgElement.setValue(UUID.randomUUID().toString());
        
        Name version = envelope.createName("version", liantongSMSProperties.getPrefix(), liantongSMSProperties.getNamespaceUrl());
        SOAPElement versionElement = sendSMSRequestElement.addChildElement(version);
        versionElement.setValue("1.0");
        
        Name license = envelope.createName("licenseKey", liantongSMSProperties.getPrefix(), liantongSMSProperties.getNamespaceUrl());
        SOAPElement licenseElement = sendSMSRequestElement.addChildElement(license);
        licenseElement.setValue(liantongSMSProperties.getLicenseKey());
        
        Name sentToIccid = envelope.createName("sentToIccid", liantongSMSProperties.getPrefix(), liantongSMSProperties.getNamespaceUrl());
        SOAPElement sentToIccidElement = sendSMSRequestElement.addChildElement(sentToIccid);
        sentToIccidElement.setValue(iccid);
        
        Name messageText = envelope.createName("messageText", liantongSMSProperties.getPrefix(), liantongSMSProperties.getNamespaceUrl());
        SOAPElement messageTextElement = sendSMSRequestElement.addChildElement(messageText);
        messageTextElement.setValue(msg);
        
        Name tpvp = envelope.createName("tpvp", liantongSMSProperties.getPrefix(), liantongSMSProperties.getNamespaceUrl());
        SOAPElement tpvpElement = sendSMSRequestElement.addChildElement(tpvp);
        tpvpElement.setValue("5");
        
        return message;
    }

    /**
     * 
    * @Title:        callWebService    
    * @Description:  调用web service   
    * @param:        @param username 用户名
    * @param:        @param password 密码
    * @param:        @param iccid 
    * @param:        @param msg 指令
    * @param:        @throws SOAPException
    * @param:        @throws IOException
    * @param:        @throws XWSSecurityException       
    * @return:       void      
    * @throws    
    * @author        zekym   
    * @Date          2017年8月5日 上午9:42:45
     */
    public SOAPMessage callWebService(String username, String password, String iccid, String msg) throws SOAPException, IOException, XWSSecurityException {
        SOAPMessage request = createSMSRequest(iccid, msg);
        request = secureMessage(request, username, password);
        SOAPConnection connection = connectionFactory.createConnection();
        SOAPMessage response = connection.call(request, liantongSMSProperties.getSmsUrl());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = df.format(new Date());
        logger.info("ICCID: " + iccid + ", LianTong SMS call webservice response: " + writeSMSResponse(response) + ", time is " + date);
//        System.out.println("ICCID: " + iccid + ", LianTong SMS call webservice response: " + writeSMSResponse(response) + ", time is " + date);
        return response;
    }
    
    public SOAPMessage callWebService(String iccid, String msg) throws SOAPException, IOException, XWSSecurityException {
        logger.info("使用联通接口向联通卡ICCID:{}发送短信", iccid);
    	
    	logger.info("username: " + liantongSMSProperties.getUsername() + ", password: " + liantongSMSProperties.getPassword());
    	return callWebService(liantongSMSProperties.getUsername(), liantongSMSProperties.getPassword(), iccid, msg);
    }

    private String writeSMSResponse(SOAPMessage message) throws SOAPException {
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        Name sendSMSResponseName = envelope.createName("SendSMSResponse", liantongSMSProperties.getPrefix(), liantongSMSProperties.getNamespaceUrl());
        SOAPBodyElement sendSMSResponseElement = (SOAPBodyElement) message
                .getSOAPBody().getChildElements(sendSMSResponseName).next();
        Name smsMsgId = envelope.createName("smsMsgId", liantongSMSProperties.getPrefix(), liantongSMSProperties.getNamespaceUrl());
        SOAPBodyElement sendSMSElement = (SOAPBodyElement) sendSMSResponseElement.getChildElements(smsMsgId).next();
        return sendSMSElement.getTextContent();
    }
    
    private SOAPMessage secureMessage(SOAPMessage message, final String username, final String password)
            throws IOException, XWSSecurityException {
        CallbackHandler callbackHandler = new CallbackHandler() {
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof UsernameCallback) {
                        UsernameCallback callback = (UsernameCallback) callbacks[i];
                        callback.setUsername(username);
                    } else if (callbacks[i] instanceof PasswordCallback) {
                        PasswordCallback callback = (PasswordCallback) callbacks[i];
                        callback.setPassword(password);
                    } else {
                        throw new UnsupportedCallbackException(callbacks[i]);
                    }
                }
            }
        };
        InputStream policyStream = null;
        XWSSProcessor processor = null;
        try {
            policyStream = getClass().getResourceAsStream("securityPolicy.xml");
            processor = processorFactory.createProcessorForSecurityConfiguration(policyStream, callbackHandler);
        }
        finally {
            if (policyStream != null) {
                policyStream.close();
            }
        }
        ProcessingContext context = processor.createProcessingContext(message);
        return processor.secureOutboundMessage(context);
    }

}
