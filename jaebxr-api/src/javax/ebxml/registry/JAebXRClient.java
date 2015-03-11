package javax.ebxml.registry;

import javax.xml.registry.JAXRException;

import org.cache2k.Cache;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

public class JAebXRClient {

	private static JAebXRClient instance = null;
	
	private static Connection connection = null;
	private static RegistryService service = null;
	private static BusinessLifeCycleManager lcm = null;
	private static DeclarativeQueryManager dqm = null;
	private static BusinessQueryManager bqm = null;

	private static org.oasis.ebxml.registry.bindings.rim.ObjectFactory rimOF = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
	private static org.oasis.ebxml.registry.bindings.lcm.ObjectFactory lcmOF = new org.oasis.ebxml.registry.bindings.lcm.ObjectFactory();
	private static org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsOF = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();

	private static Cache<String, RegistryObjectType> cache = null;
	
    protected static String idPrefix = "urn:uuid:";

    public static JAebXRClient getInstance() throws JAebXRException {
    	if (instance == null)
    		instance = new JAebXRClient();
    	return instance;
    }
    
    private JAebXRClient() throws JAebXRException {
    	try {
    		ConfigurationFactory.getInstance();
    		service = new RegistryService(null);
    		cache = service.getCache();
			lcm = service.getBusinessLifeCycleManager();
			bqm = service.getBusinessQueryManager();
			dqm = service.getDeclarativeQueryManager();
//			lcm = new BusinessLifeCycleManager(null);
//			dqm = new DeclarativeQueryManager(null);
//			bqm = new BusinessQueryManager(dqm);
		} catch (JAXRException e) {
			throw new JAebXRException(e);
		}
	}
    
    public final void openConnection() throws JAebXRException {
    	if (service == null) {
	        ConnectionFactory connFactory;
			try {
				connFactory = ConnectionFactory.newInstance();
		        connection = connFactory.createConnection();
				service = connection.getRegistryService();
				lcm = service.getBusinessLifeCycleManager();
				bqm = service.getBusinessQueryManager();
				dqm = service.getDeclarativeQueryManager();
			} catch (JAXRException e) {
				throw new JAebXRException(e);
			}
    	}
    }
    
    public Cache<String, RegistryObjectType> getCache() {
    	return cache;
    }
    
    public Connection getConnection() {
    	return connection;
    }
    
    public RegistryService getRegistryService() {
    	return service;
    }
    
    public BusinessLifeCycleManager getLifeCycleManager() {
     	return lcm;
    }
   
    public BusinessQueryManager getBusinessQueryManager() {
    	return bqm;
    }
    
    public DeclarativeQueryManager getDeclarativeQueryManager() {
    	return dqm;
    }
    
    public boolean closeConnection() throws JAXRException {
        if (connection != null) {
            if (!connection.isClosed()) {
                connection.close();
            }
            return connection.isClosed();
        }
        return true;
    }
    
    public org.oasis.ebxml.registry.bindings.rim.ObjectFactory getRimOF() {
    	return rimOF;
    }
    
    public org.oasis.ebxml.registry.bindings.lcm.ObjectFactory getLcmOF() {
    	return lcmOF;
    }
    
    public org.oasis.ebxml.registry.bindings.rs.ObjectFactory getRsOF() {
    	return rsOF;
    }
}
