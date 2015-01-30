package javax.ebxml.registry;

import java.util.Collection;

import javax.ebxml.registry.soap.SOAPMessenger;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;
import javax.xml.registry.infomodel.RegistryObject;


public class QueryManager implements javax.xml.registry.QueryManager {

    private javax.xml.registry.QueryManager qm = null;
	private javax.xml.registry.RegistryService rs = null;
	private SOAPMessenger msgr = null;

	public QueryManager() {
	}

    protected void setQueryManager(javax.xml.registry.QueryManager qm) {
    	this.qm = qm;
    }
    
    protected void setRegistryService(javax.xml.registry.RegistryService rs) {
    	this.rs = rs;
    }

	
	protected void setSOAPMessenger(SOAPMessenger msr) {
		this.msgr = msr;
	}
	
	@Override
	public RegistryObject getRegistryObject(String arg0) throws JAXRException {
		return qm.getRegistryObject(arg0);
	}

	@Override
	public RegistryObject getRegistryObject(String arg0, String arg1)
			throws JAXRException {
		return qm.getRegistryObject(arg0, arg1);
	}

	@Override
	public BulkResponse getRegistryObjects() throws JAXRException {
		return qm.getRegistryObjects();
	}

	@Override
	public BulkResponse getRegistryObjects(@SuppressWarnings("rawtypes") Collection arg0)
			throws JAXRException {
		return qm.getRegistryObjects(arg0);
	}

	@Override
	public BulkResponse getRegistryObjects(String arg0) throws JAXRException {
		return qm.getRegistryObjects(arg0);
	}

	@Override
	public BulkResponse getRegistryObjects(@SuppressWarnings("rawtypes") Collection arg0, String arg1)
			throws JAXRException {
		return qm.getRegistryObjects(arg0, arg1);
	}

	@Override
	public RegistryService getRegistryService() throws JAXRException {
		return rs;
	}
}
