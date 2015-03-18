package javax.ebxml.registry;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.CapabilityProfile;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.ClassificationScheme;

import org.cache2k.Cache;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

public class RegistryService implements javax.xml.registry.RegistryService {

	private javax.xml.registry.RegistryService rs = null;
	private BusinessLifeCycleManager blcm = null;
	private BusinessQueryManager bqm = null;
	private DeclarativeQueryManager dqm = null;

	public RegistryService(Cache<String, RegistryObjectType> c, boolean sqlQueriesEnabled) throws JAXRException {
		blcm = new BusinessLifeCycleManager(this);
		blcm.setCache(c);
		
		dqm = new DeclarativeQueryManager(this);
		
		if (sqlQueriesEnabled)
			bqm = new SQLBusinessQueryManager(dqm, blcm);
		else
			bqm = new BusinessQueryManager(dqm, blcm);
		bqm.setCache(c);
	}

	public RegistryService(javax.xml.registry.RegistryService rs) throws JAXRException {
		this.rs = rs;
		blcm = new BusinessLifeCycleManager(this);
		this.dqm = new DeclarativeQueryManager(this);
		this.bqm = new BusinessQueryManager(this);
	}
	
	@Override
	public BulkResponse getBulkResponse(String arg0)
			throws InvalidRequestException, JAXRException {
		return rs.getBulkResponse(arg0);
	}

	@Override
	public BusinessLifeCycleManager getBusinessLifeCycleManager()
			throws JAXRException {
		return blcm;
	}

	@Override
	public BusinessQueryManager getBusinessQueryManager() throws JAXRException {
		return bqm;
	}

	@Override
	public CapabilityProfile getCapabilityProfile() throws JAXRException {
		return rs.getCapabilityProfile();
	}

	@Override
	public DeclarativeQueryManager getDeclarativeQueryManager()
			throws JAXRException, UnsupportedCapabilityException {
		return dqm;
	}

	@Override
	public ClassificationScheme getDefaultPostalScheme() throws JAXRException {
		return rs.getDefaultPostalScheme();
	}

	@Override
	public String makeRegistrySpecificRequest(String arg0) throws JAXRException {
		return rs.makeRegistrySpecificRequest(arg0);
	}

}
