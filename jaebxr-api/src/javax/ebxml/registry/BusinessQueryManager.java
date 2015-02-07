package javax.ebxml.registry;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.ebxml.registry.soap.BindingUtility;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;

import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.oasis.ebxml.registry.bindings.rim.SlotListType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

public class BusinessQueryManager extends QueryManager implements javax.xml.registry.BusinessQueryManager {

	private javax.xml.registry.BusinessQueryManager bqm = null;
	
	private org.oasis.ebxml.registry.bindings.rim.ObjectFactory rimFac = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
	private org.oasis.ebxml.registry.bindings.query.ObjectFactory queryFac = new org.oasis.ebxml.registry.bindings.query.ObjectFactory();
	
	public BusinessQueryManager(javax.xml.registry.RegistryService rs) throws JAXRException {
		super();
		this.bqm = rs.getBusinessQueryManager();
		this.setRegistryService(rs);
		this.setQueryManager(bqm);
		this.setSOAPMessenger(ConfigurationFactory.getInstance().getSOAPMessenger());
	}

	public javax.xml.registry.BusinessQueryManager getBusinessQueryManager() {
		return bqm;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public BulkResponse findAssociations(Collection arg0, String arg1, String arg2, Collection arg3) throws JAXRException {
		return bqm.findAssociations(arg0, arg1, arg2, arg3);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public BulkResponse findCallerAssociations(Collection arg0, Boolean arg1, Boolean arg2, Collection arg3) throws JAXRException {
		return bqm.findCallerAssociations(arg0, arg1, arg2, arg3);
	}

	@Override
	public ClassificationScheme findClassificationSchemeByName(@SuppressWarnings("rawtypes") Collection arg0, String arg1) throws JAXRException {
		return bqm.findClassificationSchemeByName(arg0, arg1);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public BulkResponse findClassificationSchemes(Collection arg0, Collection arg1, Collection arg2, Collection arg3)
			throws JAXRException {
		return bqm.findClassificationSchemes(arg0, arg1, arg2, arg3);
	}

	@Override
	public Concept findConceptByPath(String arg0) throws JAXRException {
		return bqm.findConceptByPath(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public BulkResponse findConcepts(Collection arg0, Collection arg1, Collection arg2, Collection arg3, Collection arg4) throws JAXRException {
		return bqm.findConcepts(arg0, arg1, arg2, arg3, arg4);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public BulkResponse findOrganizations(Collection arg0, Collection arg1, Collection arg2, Collection arg3, Collection arg4, Collection arg5) throws JAXRException {
		return bqm.findOrganizations(arg0, arg1, arg2, arg3, arg4, arg5);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public BulkResponse findRegistryPackages(Collection arg0, Collection arg1, Collection arg2, Collection arg3) throws JAXRException {
		return bqm.findRegistryPackages(arg0, arg1, arg2, arg3);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public BulkResponse findServiceBindings(Key arg0, Collection arg1, Collection arg2, Collection arg3) throws JAXRException {
		return bqm.findServiceBindings(arg0, arg1, arg2, arg3);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public BulkResponse findServices(Key arg0, Collection arg1, Collection arg2, Collection arg3, Collection arg4) throws JAXRException {
		return bqm.findServices(arg0, arg1, arg2, arg3, arg4);
	}

	public ClassificationSchemeType findClassificationSchemeByName(String name) throws JAebXRException {
		String sqlQuery = "SELECT obj.* from ClassificationScheme obj , Name_ nm WHERE ((nm.parent = obj.id) AND (nm.value LIKE '" + name + "'))";
	
		AdhocQueryResponse rr = (AdhocQueryResponse)submitSqlAdhocQuery(sqlQuery);		
		ClassificationSchemeType res = null;

		if (rr.getStatus().equals(CanonicalConstants.CANONICAL_RESPONSE_STATUS_TYPE_LID_Success)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			if (i.hasNext()) {
				res = (ClassificationSchemeType) i.next().getValue();
			}
			
	        if (i.hasNext()) {
	            throw new JAebXRException("ClassificationScheme multiple match");
	        }
		}
		
		return res;
	}

	public ClassificationNodeType findClassificationNodeByPath(String path) throws JAebXRException {
		StringTokenizer st = new StringTokenizer(path, "/");
		
		String parent = st.nextToken();
		String code = st.nextToken();
		
		String sqlQuery = "SELECT cn.* FROM ClassificationNode cn WHERE parent = '" + parent + "' AND code = '" + code +"'";
		
		AdhocQueryResponse rr = (AdhocQueryResponse)submitSqlAdhocQuery(sqlQuery);
		ClassificationNodeType res = null;
		
		if (rr.getStatus().equals(CanonicalConstants.CANONICAL_RESPONSE_STATUS_TYPE_LID_Success)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			if (i.hasNext()) {
				res = (ClassificationNodeType) i.next().getValue();
			}
		}

		return res;
	}
	
	private RegistryResponseType submitSqlAdhocQuery(String sqlQuery) throws JAebXRException {
		QueryExpressionType qet = rimFac.createQueryExpressionType();
		qet.setQueryLanguage(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_SQL_92);
		qet.getContent().add(sqlQuery);

		AdhocQueryType aqt = rimFac.createAdhocQueryType();
		aqt.setId(createUUID());		
		aqt.setQueryExpression(qet);

		ResponseOptionType rot = queryFac.createResponseOptionType();
		rot.setReturnComposedObjects(true);
		rot.setReturnType(ReturnType.LEAF_CLASS);
		
		ValueListType vlt = rimFac.createValueListType();
		vlt.getValue().add("true");
		
		SlotType1 st = rimFac.createSlotType1();
		st.setName(CanonicalConstants.IMPL_SLOT_CREATE_HTTP_SESSION);
		st.setValueList(vlt);
		
		SlotListType slt = rimFac.createSlotListType();
		slt.getSlot().add(st);

		AdhocQueryRequest aqr = queryFac.createAdhocQueryRequest();
		
		aqr.setId(createUUID());
		
		aqr.setMaxResults(BigInteger.valueOf(-1));
		aqr.setStartIndex(BigInteger.valueOf(0));
		aqr.setFederated(false);
		
		aqr.setAdhocQuery(aqt);
		aqr.setResponseOption(rot);
		aqr.setRequestSlotList(slt);
		
		try {
			StringWriter sw = new StringWriter();

			Marshaller marshaller = BindingUtility.getInstance().getJAXBContext().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(aqr, sw);
			
			RegistryResponseHolder resp = msgr.sendSoapRequest(sw.toString());
			RegistryResponseType ebResp = resp.getRegistryResponseType();

			return ebResp;
		} catch (JAXBException e) {
			throw new JAebXRException(e);
		} catch (RegistryException e) {
			throw new JAebXRException(e);
		} catch (JAXRException e) {
			throw new JAebXRException(e);
		}
	}
	
    String createUUID() {
        String id = "urn:uuid:" + UUIDFactory.getInstance().newUUID().toString();
        return id;
    }
}
