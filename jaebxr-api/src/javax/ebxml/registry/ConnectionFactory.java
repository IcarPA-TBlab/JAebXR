package javax.ebxml.registry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import javax.security.auth.x500.X500PrivateCredential;
import javax.xml.registry.FederatedConnection;
import javax.xml.registry.JAXRException;

public class ConnectionFactory extends javax.xml.registry.ConnectionFactory {
	
	private static ConnectionFactory ebcf = null;
	private javax.xml.registry.ConnectionFactory cf = null;
	private ConfigurationFactory conf = null;
	private Connection ebc = null;

	ConnectionFactory() throws JAXRException {
		conf = ConfigurationFactory.getInstance();
        System.setProperty("javax.xml.registry.ConnectionFactoryClass", conf.getConnectionFactoryClass());
		cf = javax.xml.registry.ConnectionFactory.newInstance();
	}

	public static ConnectionFactory newInstance() throws JAXRException {
		if (ebcf == null) {
			ebcf = new ConnectionFactory();
		}
		return ebcf;
	}

	@Override
	public Connection createConnection() throws JAXRException {
		if (ebc == null) {
			Properties props = new Properties();
	        props.put("javax.xml.registry.queryManagerURL", conf.getClientRegistryUrl());
	        props.put("javax.xml.registry.lifeCycleManagerURL", conf.getClientRegistryUrl());
	        setProperties(props);
	        
	        javax.xml.registry.Connection c = cf.createConnection();

	        /*
            Set<X500PrivateCredential> credentials;
			try {
				credentials = getCredentialsFromKeystore(conf.getClientKeystorePath(), conf.getClientKeystorePass(), conf.getClientAlias(), conf.getClientAliasPass());
			} catch (UnrecoverableKeyException | KeyStoreException
					| NoSuchAlgorithmException | CertificateException
					| IOException e) {
				throw new JAXRException(e);
			}
			*/
			
	        HashSet<X500PrivateCredential> credentials = new HashSet<X500PrivateCredential>();
	        credentials.add(new X500PrivateCredential(conf.getClientCertificate(), conf.getClientPrivateKey(), conf.getClientAlias()));
			
			c.setCredentials(credentials);
            
            ebc = new Connection(c);
		}
		return ebc;
	}
	
	/*
    private Set<X500PrivateCredential> getCredentialsFromKeystore(String keystorePath, String storepass, String alias, String keypass) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, UnrecoverableKeyException {
        HashSet<X500PrivateCredential> credentials = new HashSet<X500PrivateCredential>();
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new BufferedInputStream(new FileInputStream(keystorePath)), storepass.toCharArray());
        X509Certificate cert = (X509Certificate)keyStore.getCertificate(alias);
        PrivateKey privateKey = (PrivateKey)keyStore.getKey(alias, keypass.toCharArray());
        credentials.add(new X500PrivateCredential(cert, privateKey, alias));
        return credentials;
    }
    */
 
	@Override
	public FederatedConnection createFederatedConnection(@SuppressWarnings("rawtypes") Collection arg0)
			throws JAXRException {
		return cf.createFederatedConnection(arg0);
	}

	@Override
	public Properties getProperties() throws JAXRException {
		return cf.getProperties();
	}

	@Override
	public void setProperties(Properties arg0) throws JAXRException {
		cf.setProperties(arg0);
	}

}
