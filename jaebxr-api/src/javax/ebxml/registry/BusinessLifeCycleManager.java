package javax.ebxml.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.JAXBElement;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.Association;

import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

public class BusinessLifeCycleManager extends LifeCycleManager implements javax.xml.registry.BusinessLifeCycleManager {

	private javax.xml.registry.BusinessLifeCycleManager blcm = null;
	
	public BusinessLifeCycleManager(javax.xml.registry.RegistryService rs) throws JAXRException {
		super();
		if (rs != null) {
			this.blcm = rs.getBusinessLifeCycleManager();
			this.setRegistryService(rs);
		}
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

    public RegistryResponseType saveAssociationType(AssociationType1 a) throws JAebXRException {
    	JAXBElement<AssociationType1> eb = createAssociation(a);
    	return saveObjectType(eb);
    }
    
	@Override
	public BulkResponse saveClassificationSchemes(@SuppressWarnings("rawtypes") Collection arg0)
			throws JAXRException {
		return blcm.saveClassificationSchemes(arg0);
	}
	
    public RegistryResponseType saveClassificationSchemeType(ClassificationSchemeType cs) throws JAebXRException {
    	JAXBElement<ClassificationSchemeType> eb = createClassificationScheme(cs);
    	return saveObjectType(eb);
    }
    
    
	@Override
	public BulkResponse saveConcepts(@SuppressWarnings("rawtypes") Collection arg0) throws JAXRException {
		return blcm.saveConcepts(arg0);
	}
    
    public RegistryResponseType saveClassificationNodeType(ClassificationNodeType cn) throws JAebXRException {
    	JAXBElement<ClassificationNodeType> eb = createClassificationNodeType(cn);
    	return saveObjectType(eb);
    }
    
    public RegistryResponseType saveClassificationNodes(Collection<ClassificationNodeType> ccn) throws JAebXRException {
    	Collection <JAXBElement<? extends IdentifiableType>> list = new ArrayList<JAXBElement<? extends IdentifiableType>>();
    	Iterator<ClassificationNodeType> i = ccn.iterator();
    	while (i.hasNext()) {
    		list.add(this.createClassificationNodeType((ClassificationNodeType) i.next()));
    	};
    	SubmitObjectsRequest sreq = createSubmitObjectsRequest(list);
    	RegistryResponseType resp = saveObjectTypes(sreq);
    	return resp;    	
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

	public RegistryResponseType setStatusOnObjects(RegistryObjectType ro, String statusTypeId) throws JAebXRException {
		ro.setStatus(statusTypeId);
		return updateObjectType(ro);
	}


}
