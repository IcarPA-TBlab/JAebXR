package javax.ebxml.registry;

import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBElement;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.JAXRException;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;

import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.query.ClassificationNodeQueryType;
import org.oasis.ebxml.registry.bindings.query.ClassificationSchemeQueryType;
import org.oasis.ebxml.registry.bindings.query.CompoundFilterType;
import org.oasis.ebxml.registry.bindings.query.RegistryObjectQueryType;
import org.oasis.ebxml.registry.bindings.query.SimpleFilterType;
import org.oasis.ebxml.registry.bindings.query.StringFilterType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;

public class BusinessQueryManager extends QueryManager implements javax.xml.registry.BusinessQueryManager {

	private javax.xml.registry.BusinessQueryManager bqm = null;
	private DeclarativeQueryManager dqm = null;

	public BusinessQueryManager(DeclarativeQueryManager qm) throws JAXRException {
		super();
		this.dqm = qm;
		this.setSOAPMessenger(ConfigurationFactory.getInstance().getSOAPMessenger());
	}
	
	public BusinessQueryManager(javax.xml.registry.RegistryService rs) throws JAXRException {
		super();
		if (rs != null) {
			this.bqm = rs.getBusinessQueryManager();
	 		this.dqm = (DeclarativeQueryManager) rs.getDeclarativeQueryManager();
			this.setRegistryService(rs);
			this.setQueryManager(bqm);
		}
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
		StringFilterType f = queryFac.createStringFilterType();		
		f.setComparator(SimpleFilterType.Comparator.EQ);
		f.setDomainAttribute("name");
		f.setValue(name);

		ClassificationSchemeQueryType q = queryFac.createClassificationSchemeQueryType();
		q.setPrimaryFilter(f);
		JAXBElement<ClassificationSchemeQueryType> ebq = queryFac.createClassificationSchemeQuery(q);
				
		//AdhocQueryResponse rr = (AdhocQueryResponse)submitRSFilterQuery(ebq);

		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery, ebq);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		/*
		String sqlQuery = "SELECT obj.* from ClassificationScheme obj , Name_ nm WHERE ((nm.parent = obj.id) AND (nm.value = '" + name + "'))";	
		AdhocQueryResponse rr = (AdhocQueryResponse)submitSqlAdhocQuery(sqlQuery);		
		*/

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

		StringFilterType lf = queryFac.createStringFilterType();
		lf.setComparator(SimpleFilterType.Comparator.EQ);
		lf.setDomainAttribute("parent");
		lf.setValue(parent);
		
		StringFilterType rf = queryFac.createStringFilterType();
		rf.setComparator(SimpleFilterType.Comparator.EQ);
		rf.setDomainAttribute("code");
		rf.setValue(code);

		CompoundFilterType cf = queryFac.createCompoundFilterType();
		cf.setLeftFilter(lf);
		cf.setRightFilter(rf);
		cf.setLogicalOperator(CompoundFilterType.LogicalOperator.AND);
		
		ClassificationNodeQueryType q = queryFac.createClassificationNodeQueryType();
		q.setPrimaryFilter(cf);
		JAXBElement<ClassificationNodeQueryType> ebq = queryFac.createClassificationNodeQuery(q);

		// AdhocQueryResponse rr = (AdhocQueryResponse)submitRSFilterQuery(ebq);

		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery, ebq);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		/*
		String sqlQuery = "SELECT cn.* FROM ClassificationNode cn WHERE parent = '" + parent + "' AND code = '" + code +"'";		
		AdhocQueryResponse rr = (AdhocQueryResponse)submitSqlAdhocQuery(sqlQuery);
		*/
		
		ClassificationNodeType res = null;
		
		if (rr.getStatus().equals(CanonicalConstants.CANONICAL_RESPONSE_STATUS_TYPE_LID_Success)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			if (i.hasNext()) {
				res = (ClassificationNodeType) i.next().getValue();
			}
		}

		return res;
	}
	
	public RegistryObjectType getRegistryObjectType(String id) throws JAebXRException {
		StringFilterType f = queryFac.createStringFilterType();		
		f.setComparator(SimpleFilterType.Comparator.EQ);
		f.setDomainAttribute("id");
		f.setValue(id);

		RegistryObjectQueryType roq = queryFac.createRegistryObjectQueryType();
		roq.setPrimaryFilter(f);		
		JAXBElement<RegistryObjectQueryType> ebq = queryFac.createRegistryObjectQuery(roq);
		
		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery, ebq);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		RegistryObjectType res = null;
		
		if (rr.getStatus().equals(CanonicalConstants.CANONICAL_RESPONSE_STATUS_TYPE_LID_Success)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			if (i.hasNext()) {
				res = (RegistryObjectType) i.next().getValue();
			}
			
	        if (i.hasNext()) {
	            throw new JAebXRException("ClassificationScheme multiple match");
	        }
		}
		
		return res;
	}
	
	// TODO
	public RegistryObjectType getRegistryObjectType(String id, String objectType) throws JAebXRException {
		throw new JAebXRException("Not yet implemented!");
	}
	
	// TODO
	public Collection<RegistryObjectType> getRegistryObjectTypes() throws JAebXRException {
		throw new JAebXRException("Not yet implemented!");
	}
	
	// TODO
	public Collection<RegistryObjectType> getRegistryObjectTypes(String objectType) throws JAebXRException {
		throw new JAebXRException("Not yet implemented!");
	}
	
	// TODO
	public Collection<RegistryObjectType> getRegistryObjectTypes(Collection<String> objectKeys) throws JAebXRException {
		throw new JAebXRException("Not yet implemented!");
	}
	
	// TODO
	public Collection<RegistryObjectType> getRegistryObjectTypes(Collection<String> objectKeys, String objectType) throws JAebXRException {
		throw new JAebXRException("Not yet implemented!");
	}
	
	/*
	private RegistryResponseType submitRSFilterQuery(JAXBElement<? extends FilterQueryType> ebq) throws JAebXRException {
		QueryExpressionType qet = rimFac.createQueryExpressionType();
		qet.setQueryLanguage(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery);
		
		try {
			qet.getContent().add(BindingUtility.getInstance().marshalObject(ebq));
		} catch (JAXBException e1) {
			throw new JAebXRException(e1);
		}

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

		RegistryResponseType ebResp = null;

		try {
			RegistryResponseHolder resp = msgr.sendSoapRequest(BindingUtility.getInstance().marshalObject(aqr));
			ebResp = resp.getRegistryResponseType();
		} catch (JAXBException e) {
			throw new JAebXRException(e);
		} catch (RegistryException e) {
			throw new JAebXRException(e);
		} catch (JAXRException e) {
			throw new JAebXRException(e);
		}

		return ebResp;
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
	*/
}
