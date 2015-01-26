package javax.ebxml.registry;

import java.util.Collection;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Association;

public class BusinessLifeCycleManager extends LifeCycleManager implements javax.xml.registry.BusinessLifeCycleManager {

	private javax.xml.registry.BusinessLifeCycleManager blcm = null;
	
	public BusinessLifeCycleManager(javax.xml.registry.RegistryService rs) throws JAXRException {
		super();
		this.blcm = rs.getBusinessLifeCycleManager();
		this.setRegistryService(rs);
		this.setLifeCycleManager(blcm);
		this.setSOAPMessenger(ConfigurationFactory.getInstance().getSOAPMessenger());
	}
	
	public javax.xml.registry.BusinessLifeCycleManager getBusinessLifeCycleManager() {
		return blcm;
	}

	@Override
	public void confirmAssociation(Association arg0) throws JAXRException,
			InvalidRequestException {
		blcm.confirmAssociation(arg0);	
	}

	@Override
	public BulkResponse deleteAssociations(@SuppressWarnings("rawtypes") Collection arg0)
			throws JAXRException {
		return blcm.deleteAssociations(arg0);
	}

	@Override
	public BulkResponse deleteClassificationSchemes(@SuppressWarnings("rawtypes") Collection arg0)
			throws JAXRException {
		return blcm.deleteClassificationSchemes(arg0);
	}

	@Override
	public BulkResponse deleteConcepts(@SuppressWarnings("rawtypes") Collection arg0) throws JAXRException {
		return blcm.deleteConcepts(arg0);
	}

	@Override
	public BulkResponse deleteOrganizations(@SuppressWarnings("rawtypes") Collection arg0)
			throws JAXRException {
		return blcm.deleteOrganizations(arg0);
	}

	@Override
	public BulkResponse deleteServiceBindings(@SuppressWarnings("rawtypes") Collection arg0)
			throws JAXRException {
		return blcm.deleteServiceBindings(arg0);
	}

	@Override
	public BulkResponse deleteServices(@SuppressWarnings("rawtypes") Collection arg0) throws JAXRException {
		return blcm.deleteServices(arg0);
	}

	@Override
	public BulkResponse saveAssociations(@SuppressWarnings("rawtypes") Collection arg0, boolean arg1)
			throws JAXRException {
		return blcm.saveAssociations(arg0, arg1);
	}

	@Override
	public BulkResponse saveClassificationSchemes(@SuppressWarnings("rawtypes") Collection arg0)
			throws JAXRException {
		return blcm.saveClassificationSchemes(arg0);
	}

	@Override
	public BulkResponse saveConcepts(@SuppressWarnings("rawtypes") Collection arg0) throws JAXRException {
		return blcm.saveConcepts(arg0);
	}

	@Override
	public BulkResponse saveOrganizations(@SuppressWarnings("rawtypes") Collection arg0) throws JAXRException {
		return blcm.saveOrganizations(arg0);
	}

	@Override
	public BulkResponse saveServiceBindings(@SuppressWarnings("rawtypes") Collection arg0)
			throws JAXRException {
		return blcm.saveServiceBindings(arg0);
	}

	@Override
	public BulkResponse saveServices(@SuppressWarnings("rawtypes") Collection arg0) throws JAXRException {
		return blcm.saveServices(arg0);
	}

	@Override
	public void unConfirmAssociation(Association arg0) throws JAXRException,
			InvalidRequestException {
		blcm.unConfirmAssociation(arg0);
	}

}
