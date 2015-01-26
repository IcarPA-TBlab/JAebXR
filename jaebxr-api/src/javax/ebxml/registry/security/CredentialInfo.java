package javax.ebxml.registry.security;

import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;


/**
 * Class to encapsulate Client Credential information.
 * 
 */
public class CredentialInfo {
    
    public String alias;
    public X509Certificate cert;
    public Certificate[] certChain;
    public PrivateKey privateKey;
    public String sessionId;

    public CredentialInfo() {
        
    }
    
    public CredentialInfo(
    String alias,
    X509Certificate cert,
    Certificate[] certChain,
    PrivateKey privateKey) {
        this.alias = alias;
        this.cert = cert;
        this.certChain = certChain;
        this.privateKey = privateKey;
    }
    
}
