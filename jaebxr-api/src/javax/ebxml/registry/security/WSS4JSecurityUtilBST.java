package javax.ebxml.registry.security;


import javax.xml.soap.SOAPEnvelope;

import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.CryptoFactory;

public class WSS4JSecurityUtilBST extends WSS4JSecurityUtilBase {
	
	
	/*
	 * Client has role SOAP sender --> verifySOAPEnvelopeOnServerBST()
	 */
    public static void signSOAPEnvelopeOnClient(SOAPEnvelope se, CredentialInfo credentialInfo) {
		try {
			signSOAPEnvelopeBST(se, credentialInfo, CryptoFactory.getInstance("crypto-client.properties"));
		} catch (WSSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
