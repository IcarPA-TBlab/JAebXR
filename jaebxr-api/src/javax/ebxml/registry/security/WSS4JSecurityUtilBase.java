package javax.ebxml.registry.security;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.registry.RegistryException;
import javax.xml.soap.SOAPEnvelope;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.apache.ws.security.components.crypto.CryptoFactory;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecTimestamp;
import org.apache.ws.security.util.WSSecurityUtil;
import org.w3c.dom.Document;

public class WSS4JSecurityUtilBase {

	   public final static String CANONICAL_URI_SENDER_CERT =
		        "urn:oasis:names:tc:ebxml-regrep:rs:security:SenderCert";
		 
	/** Key for configuration property 'max clock skew', passed to wss4j-security API.
	 *  "The assumed maximum skew (milliseconds) between the local times of any two systems."
	 */
	private static String getKeyMaxClockSkew() {
		return "1800";
	}

	/**
	 * Convenience method to extract an UUID/URN from a Content ID (CID). CIDs
	 * are used for identifying attachments in signed SOAP messages.
	 * 
	 * @param cid
	 *            The attachment's Content-ID.
	 * @return The corresponding UUID/URN.
	 */
	public static String convertContentIdToUUID(String cid) throws RegistryException {
		if (!(cid.charAt(0) == '<' && cid.charAt(cid.length() - 1) == '>')) {
			// error, not a cid URI Scheme id.
			throw new RegistryException("message.CIDURIExpected: " +  cid );
		}
	
		String uuid = cid.substring(1, cid.length() - 1);
		return uuid;
	}

	/**
	 * Convenience method to turn an UUID/URN into a Content ID (CID). CIDs are
	 * used for identifying attachments in signed SOAP messages.
	 * 
	 * @param uuid
	 *            The original UUID/URN.
	 * @return The generated CID to be set as attachment Content-ID.
	 */
	public static String convertUUIDToContentId(String uuid) {
		String cid = "<" + uuid + ">";
		return cid;
	}

	protected static List<WSEncryptionPart> createReferences(String soapNamespace) {
		List<WSEncryptionPart> parts = new ArrayList<WSEncryptionPart>();
        WSEncryptionPart encP1 =
                new WSEncryptionPart(
                    WSConstants.TIMESTAMP_TOKEN_LN,
                    WSConstants.WSU_NS,
                    "Element");
            parts.add(encP1);
        WSEncryptionPart encP2 =
            new WSEncryptionPart(
                    WSConstants.ELEM_BODY, 
                    soapNamespace, 
                    "Content"
        			);
        parts.add(encP2);
		return parts;
	}


	protected static WSSecTimestamp createTimestamp() {
        
        WSSecTimestamp timestamp = new WSSecTimestamp();
        
        int timeToLive = 300; // default
        String maxClockSkew = getKeyMaxClockSkew();
        
        if (maxClockSkew != null) {
            try {
            	timeToLive = Integer.parseInt(maxClockSkew);
            } catch (NumberFormatException e) {
            }
        }
        
        timestamp.setTimeToLive(timeToLive);
		return timestamp;
	}
	
	
	/*
	 * Client has role SOAP receiver <-- signSOAPEnvelopeOnServerBST()
	 */
	public static void verifySOAPEnvelopeOnClientBST(SOAPEnvelope se, CredentialInfo credentialInfo) {
		// remark: this method is not used since OMAR v3.0
		// client side verification of signatures is not implemented  
	    try {
			Crypto crypto = CryptoFactory.getInstance("crypto-client.properties");
			verifySOAPEnvelopeBST(se, credentialInfo, crypto);
		} catch (WSSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // default crypto

	}

	
	/*
	 * Server has role SOAP receiver <-- signSOAPEnvelopeOnClientBST()
	 * 
	 * 		certificate branch: RegistryBSTServlet
	 */
	public static void verifySOAPEnvelopeOnServerBST(SOAPEnvelope se, CredentialInfo credentialInfo) {
	    try {
			Crypto crypto = CryptoFactory.getInstance("crypto-server.properties");
			verifySOAPEnvelopeBST(se, credentialInfo, crypto);
		} catch (WSSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // default crypto

	}


	protected static void verifySOAPEnvelopeBST(SOAPEnvelope se, CredentialInfo credentialInfo, Crypto crypto) throws WSSecurityException {
		
		WSSecurityEngine secEngine = new WSSecurityEngine();
		
	    WSSConfig.init();
		
		Document doc = se.getOwnerDocument();
	    
		List<WSSecurityEngineResult> results = 
	            secEngine.processSecurityHeader(doc, null, null, crypto);
	
		if (results != null) {
			    				
	        WSSecurityEngineResult actionResult =
	                WSSecurityUtil.fetchActionResult(results, WSConstants.SIGN);
	        X509Certificate cert = 
	                (X509Certificate)actionResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE);
			
			// inject certificate for further processing
	        credentialInfo.cert = cert;
		}
	}

	
	/*
	 * Server has role SOAP sender --> verifySOAPEnvelopeOnClientBST()
	 */
    public static void signSOAPEnvelopeOnServerBST(SOAPEnvelope se, CredentialInfo credentialInfo) {
		try {
			signSOAPEnvelopeBST(se, credentialInfo, CryptoFactory.getInstance("crypto-server.properties"));
		} catch (WSSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    /*
     * Signing SOAP message with certificate
     */
	protected static void signSOAPEnvelopeBST(SOAPEnvelope se, CredentialInfo credentialInfo, Crypto userCrypto)
			throws WSSecurityException {
		
		WSSConfig.init();

		// inject empty SecHeader into Document
		Document doc = se.getOwnerDocument();
		WSSecHeader secHeader = new WSSecHeader();
		secHeader.insertSecurityHeader(doc);

		WSSecTimestamp timestamp = createTimestamp();
		timestamp.build(doc, secHeader);

		// overridden WSS4J path for explicit setPrivateKey()
		// <ds:Signature>
		WSS4JSignatureBST sign = new WSS4JSignatureBST();

		// <wsse:BinarySecurityToken ...>
		sign.setX509Certificate(credentialInfo.cert);
		sign.setKeyIdentifierType(WSConstants.BST_DIRECT_REFERENCE);

		sign.setPrivateKey(credentialInfo.privateKey);

		// <ds:KeyInfo>
		sign.setCertUri(CANONICAL_URI_SENDER_CERT);

		String soapNamespace = WSSecurityUtil.getSOAPNamespace(doc.getDocumentElement());

		// signature references
		// <ds:Reference>
		List<WSEncryptionPart> parts = createReferences(soapNamespace);
		sign.setParts(parts);

		sign.build(doc, 
				userCrypto, 
				secHeader);
	}
	
}