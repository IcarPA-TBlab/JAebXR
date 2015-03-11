package javax.ebxml.registry;

import java.util.concurrent.TimeUnit;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.CapabilityProfile;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.ClassificationScheme;

import org.cache2k.Cache;
import org.cache2k.CacheBuilder;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

public class RegistryService implements javax.xml.registry.RegistryService {

	private javax.xml.registry.RegistryService rs = null;
	private BusinessLifeCycleManager blcm = null;
	private BusinessQueryManager bqm = null;
	private DeclarativeQueryManager dqm = null;
	private Cache<String, RegistryObjectType> cache = null;

	public RegistryService(javax.xml.registry.RegistryService rs) throws JAXRException {
		this.rs = rs;
		//if (rs != null) {
		this.cache = CacheBuilder.newCache(String.class, RegistryObjectType.class)
				.name("JAebXRClient")
				.expiryDuration(15, TimeUnit.MINUTES)
				.maxSize(500000).build();
			this.blcm = new BusinessLifeCycleManager(this, this.cache);
			this.dqm = new DeclarativeQueryManager(this);
			this.bqm = new BusinessQueryManager(this.dqm, this.cache);
		//}
	}
	
	public Cache<String, RegistryObjectType> getCache() {
		return cache;
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
