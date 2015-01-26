package javax.ebxml.registry;

import javax.ebxml.registry.soap.SOAPMessenger;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.UnsupportedCapabilityException;

public class DeclarativeQueryManager extends QueryManager implements javax.xml.registry.DeclarativeQueryManager {

	private javax.xml.registry.DeclarativeQueryManager dqm = null;
	@SuppressWarnings("unused")
	private SOAPMessenger msgr = null;
	
	public DeclarativeQueryManager(javax.xml.registry.RegistryService rs) throws UnsupportedCapabilityException, JAXRException {
		super();
		this.dqm = rs.getDeclarativeQueryManager();
		this.setRegistryService(rs);
		this.setSOAPMessenger(ConfigurationFactory.getInstance().getSOAPMessenger());
	}

	protected void setSOAPMessenger(SOAPMessenger msgr) {
		this.msgr = msgr;
	}

	public void setDeclarativeQueryManager(javax.xml.registry.DeclarativeQueryManager dqm) {
		this.dqm = dqm;
	}
	
	@Override
	public Query createQuery(int arg0, String arg1)
			throws InvalidRequestException, JAXRException {
		return dqm.createQuery(arg0, arg1);
	}

	@Override
	public BulkResponse executeQuery(Query arg0) throws JAXRException {
		return dqm.executeQuery(arg0);
	}

}
