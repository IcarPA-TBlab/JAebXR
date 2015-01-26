package javax.ebxml.registry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.ebxml.registry.security.CredentialInfo;
import javax.ebxml.registry.soap.SOAPMessenger;
import javax.xml.registry.JAXRException;

public class ConfigurationFactory {
    public static boolean EXTERNALFILE = true;
    public static String CONFIG_PATH = "/ebxml.properties";
    private static ConfigurationFactory instance = null;
    private static String clientAlias;
    private static String clientAliasPass;
    private static String clientRegistryUrl;
    private static String clientKeystorePath;
    private static String clientKeystorePass;
    private static String clientCertificateType;
    private static String registryName;
    private static String registryUUID;
    private static String registryDriverName;
    private static String connectionFactoryClass;
    private static Properties properties = new Properties();
	private static KeyStore keyStore;
	private static java.security.cert.Certificate[] certs;
	private static java.security.PrivateKey privateKey;
	private static CredentialInfo credentialInfo;
	private static SOAPMessenger msgr;

    public static ConfigurationFactory getInstance() throws JAXRException {
        if (instance == null) {
            instance = new ConfigurationFactory();
        }
        return instance;
    }

    private ConfigurationFactory() throws JAXRException {
        if (EXTERNALFILE == true) {
            String percorso;

            percorso = System.getProperty("user.dir");
            percorso += CONFIG_PATH;
            CONFIG_PATH = percorso;
        }
        System.out.print("ebjaxr-api config path: ");
        System.out.println(CONFIG_PATH);
        loadProperties();
    }

    public String getClientAlias() {
        return clientAlias;
    }

    public String getClientAliasPass() {
        return clientAliasPass;
    }

    public String getClientCertificateType() {
        return clientCertificateType;
    }

    public String getClientKeystorePass() {
        return clientKeystorePass;
    }

    public String getClientKeystorePath() {
        return clientKeystorePath;
    }

    public String getClientRegistryUrl() {
        return clientRegistryUrl;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getRegistryDriverName() {
        return registryDriverName;
    }

    public String getRegistryName() {
        return registryName;
    }

    public String getRegistryUUID() {
        return registryUUID;
    }
    
    public String getConnectionFactoryClass() {
    	return connectionFactoryClass;
    }

    public CredentialInfo getCredentialInfo() {
    	return credentialInfo;
    }

    public SOAPMessenger getSOAPMessenger() {
    	return msgr;
    }
    
    private void loadProperties() throws JAXRException {
        synchronized (properties) {
            properties = new Properties();
            try {
                if (EXTERNALFILE) {
                    properties.load(new FileInputStream(new File(CONFIG_PATH)));
                } else {
                    properties.load(this.getClass().getResourceAsStream(CONFIG_PATH));
                }
            } catch (IOException e) {
            	throw new JAXRException(e);
            }
        }

        this.setClientAlias(properties.getProperty("aliasName"));
        this.setClientAliasPass(properties.getProperty("aliasPass"));
        
        this.setClientKeystorePath(properties.getProperty("keystorePath"));
        this.setClientKeystorePass(properties.getProperty("keystorePass"));
        this.setClientCertificateType(properties.getProperty("certificateType"));

        //this.setRegistryDriverName(properties.getProperty("registry.driver"));
        this.setClientRegistryUrl(properties.getProperty("registryURL"));
        this.setRegistryName(properties.getProperty("registryName"));
        this.setRegistryUUID(properties.getProperty("registryUUID"));
        
        this.setConnectionFactoryClass(properties.getProperty("connectionFactoryClass"));
        
        try {
			keyStore = KeyStore.getInstance("JKS");
	        keyStore.load(new FileInputStream(properties.getProperty("client.keystore_path")), stringToCharArray(properties.getProperty("client.keystore_pass")));
			certs = keyStore.getCertificateChain(properties.getProperty("client.alias"));
			privateKey = (PrivateKey) keyStore.getKey(properties.getProperty("client.alias"), stringToCharArray(properties.getProperty("client.alias_pass")));
			
			credentialInfo = new CredentialInfo(null, (X509Certificate) certs[0], certs, privateKey);
			
			msgr = new SOAPMessenger(properties.getProperty("client.registry_url"), credentialInfo);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableKeyException e) {
			throw new JAXRException(e);
		}
    }

	private char[] stringToCharArray(String str) {
		char[] arr = null;
		if (str != null) {
			arr = str.toCharArray();
		}
		return arr;
	}

    public synchronized void setClientAlias(String clientAlias) {
        ConfigurationFactory.clientAlias = clientAlias;
    }

    public synchronized void setClientAliasPass(String clientAliasPass) {
        ConfigurationFactory.clientAliasPass = clientAliasPass;
    }

    public synchronized void setClientCertificateType(String clientCertificateType) {
        ConfigurationFactory.clientCertificateType = clientCertificateType;
    }

    public synchronized void setClientKeystorePass(String clientKeystorePass) {
        ConfigurationFactory.clientKeystorePass = clientKeystorePass;
    }

    public synchronized void setClientKeystorePath(String clientKeystorePath) {
        ConfigurationFactory.clientKeystorePath = clientKeystorePath;
    }

    public synchronized void setClientRegistryUrl(String clientRegistryUrl) {
        ConfigurationFactory.clientRegistryUrl = clientRegistryUrl;
    }

    public synchronized void setProperties(Properties properties) {
        ConfigurationFactory.properties = properties;
    }

    public synchronized void setRegistryDriverName(String registryDriverName) {
        ConfigurationFactory.registryDriverName = registryDriverName;
    }

    public synchronized void setRegistryName(String registryName) {
        ConfigurationFactory.registryName = registryName;
    }

    public synchronized void setRegistryUUID(String registryUUID) {
        ConfigurationFactory.registryUUID = registryUUID;
    }
    
    public synchronized void setConnectionFactoryClass(String connectionFactoryClass) {
    	ConfigurationFactory.connectionFactoryClass = connectionFactoryClass;
    }
}