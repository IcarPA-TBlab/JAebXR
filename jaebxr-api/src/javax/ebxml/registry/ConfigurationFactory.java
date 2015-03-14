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
import java.util.concurrent.TimeUnit;

import javax.ebxml.registry.security.CredentialInfo;
import javax.ebxml.registry.soap.SOAPMessenger;
import javax.xml.registry.JAXRException;

import org.cache2k.Cache;
import org.cache2k.CacheBuilder;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

public class ConfigurationFactory {
    public static boolean EXTERNALFILE = true;
    public static String CONFIG_PATH = "/jaebxml.properties";
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
    private static boolean sqlQueries;
    private static int cacheExpirationTime;
    private static int cacheMaxSize;
    private static Properties properties = new Properties();
	private static KeyStore keyStore;
	private static java.security.cert.Certificate[] certs;
	private static java.security.PrivateKey privateKey;
	private static CredentialInfo credentialInfo;
	private static SOAPMessenger msgr;
	private static Cache<String, RegistryObjectType> cache;

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
        //System.out.print("jaebxr config path: ");
        //System.out.println(CONFIG_PATH);
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
    
    public PrivateKey getClientPrivateKey() {
    	return privateKey;
    }
    
    public X509Certificate getClientCertificate() {
    	return (X509Certificate) certs[0];
    }
    
    public Cache<String, RegistryObjectType> getCache() {
    	return cache;
    }
    
    public int getCacheExpirationTime() {
    	return cacheExpirationTime;
    }
    
    public int getCacheMaxSize() {
    	return cacheMaxSize;
    }
    
    public boolean areSqlQueriesEnabled() {
    	return sqlQueries;
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

        this.setClientRegistryUrl(properties.getProperty("registryURL"));
        this.setRegistryName(properties.getProperty("registryName"));
        this.setRegistryUUID(properties.getProperty("registryUUID"));
        
        this.setConnectionFactoryClass(properties.getProperty("connectionFactoryClass"));
        
        this.setCacheExpirationTime(properties.getProperty("cacheExpirationTime"));
        this.setCacheMaxSize(properties.getProperty("cacheMaxSize"));
        
        this.setSqlQueries(properties.getProperty("sqlQueries"));
        
        try {
			keyStore = KeyStore.getInstance("JKS");
	        keyStore.load(new FileInputStream(getClientKeystorePath()), stringToCharArray(getClientKeystorePass()));
			certs = keyStore.getCertificateChain(getClientAlias());
			privateKey = (PrivateKey) keyStore.getKey(getClientAlias(), stringToCharArray(getClientAliasPass()));
			
			credentialInfo = new CredentialInfo(getClientAlias(), (X509Certificate) certs[0], certs, privateKey);
			
			msgr = new SOAPMessenger(getClientRegistryUrl(), credentialInfo);
			
			cache = CacheBuilder.newCache(String.class, RegistryObjectType.class)
					.name("JAebXRClient")
					.expiryDuration(getCacheExpirationTime(), TimeUnit.MINUTES)
					.maxSize(getCacheMaxSize()*1024).build();
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
    
    public synchronized void setCacheExpirationTime(String s_cacheExpirationTime) { 	
    	try {
    		ConfigurationFactory.cacheExpirationTime = Integer.parseInt(s_cacheExpirationTime);    		
    	} catch (NumberFormatException e) {
    		ConfigurationFactory.cacheExpirationTime = 15; //default 15 minutes
    	}
    }

    public synchronized void setCacheMaxSize(String s_cacheMaxSize) { 	
    	try {
    		ConfigurationFactory.cacheMaxSize = Integer.parseInt(s_cacheMaxSize);    		
    	} catch (NumberFormatException e) {
    		ConfigurationFactory.cacheMaxSize = 512; //default 512 KB
    	}
    }
    
    public synchronized void setSqlQueries(String flag) {
    	ConfigurationFactory.sqlQueries = Boolean.parseBoolean(flag);
    }
}