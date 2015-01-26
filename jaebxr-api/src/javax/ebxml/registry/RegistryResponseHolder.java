package javax.ebxml.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.registry.RegistryException;

import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryError;
import org.oasis.ebxml.registry.bindings.rs.RegistryErrorList;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;


public class RegistryResponseHolder {
    @SuppressWarnings("unused")
	private ArrayList<Object> collection = new ArrayList<Object>();
    private RegistryResponseType ebRegistryResponseType = null;
    private HashMap<String, Object> responseAttachments = null;

    /**
     * Construct an empty successful BulkResponse
     */
    @SuppressWarnings("unused")
	private RegistryResponseHolder()  {
    }

    public RegistryResponseHolder(RegistryResponseType ebRegistryResponseType, HashMap<String, Object> responseAttachments) {
        this.ebRegistryResponseType = ebRegistryResponseType;
        this.responseAttachments = responseAttachments;
    }

    /**
     * Get the RegistryException(s) Collection in case of partial commit.
     * Caller thread will block here if result is not yet available.
     * Return null if result is available and there is no RegistryException(s).
     *
     * <p><DL><DT><B>Capability Level: 0 </B></DL>
     *
     */
    public List<RegistryException> getExceptions() throws RegistryException {
        ArrayList<RegistryException> registryExceptions = new ArrayList<RegistryException>();
        
        RegistryErrorList ebRegistryErrorList = ebRegistryResponseType.getRegistryErrorList();

        if (ebRegistryErrorList != null) {
            List<RegistryError> errs = ebRegistryErrorList.getRegistryError();
            Iterator<RegistryError> iter = errs.iterator();

            while (iter.hasNext()) {
                RegistryError error = iter.next();

                //TODO: Need to add additional error info to exception somehow
                registryExceptions.add(new RegistryException(error.getValue()));
            }
        }
        return registryExceptions;
    }

    public List<?> getCollection() throws RegistryException {
        List<?> roList = null;
        
        if (ebRegistryResponseType instanceof AdhocQueryResponse) {
            AdhocQueryResponse ebAdhocQueryResponse = (AdhocQueryResponse)ebRegistryResponseType;
            RegistryObjectListType ebRegistryObjectListType = ebAdhocQueryResponse.getRegistryObjectList();
            roList = ebRegistryObjectListType.getIdentifiable();
        }

        return roList;
    }
    
    public RegistryResponseType getRegistryResponseType() {
        return ebRegistryResponseType;
    }
    
    
    public HashMap<String, Object> getAttachmentsMap() {
        return responseAttachments;
    }
    
}
