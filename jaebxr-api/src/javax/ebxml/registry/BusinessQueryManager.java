package javax.ebxml.registry;

import java.util.ArrayList;
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
import org.oasis.ebxml.registry.bindings.query.AssociationQueryType;
import org.oasis.ebxml.registry.bindings.query.ClassificationNodeQueryType;
import org.oasis.ebxml.registry.bindings.query.ClassificationSchemeQueryType;
import org.oasis.ebxml.registry.bindings.query.CompoundFilterType;
import org.oasis.ebxml.registry.bindings.query.OrganizationQueryType;
import org.oasis.ebxml.registry.bindings.query.RegistryObjectQueryType;
import org.oasis.ebxml.registry.bindings.query.ServiceQueryType;
import org.oasis.ebxml.registry.bindings.query.SimpleFilterType;
import org.oasis.ebxml.registry.bindings.query.StringFilterType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.AuditableEventType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.OrganizationType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

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

		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery, ebq);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		ClassificationSchemeType res = null;
		
		if (isStatusSuccess(rr)) {
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

	public ClassificationSchemeType findClassificationSchemeByPattern(String namePattern) throws JAebXRException {
		StringFilterType f = queryFac.createStringFilterType();		
		f.setComparator(SimpleFilterType.Comparator.LIKE);
		f.setDomainAttribute("name");
		f.setValue(namePattern);

		ClassificationSchemeQueryType q = queryFac.createClassificationSchemeQueryType();
		q.setPrimaryFilter(f);
		JAXBElement<ClassificationSchemeQueryType> ebq = queryFac.createClassificationSchemeQuery(q);

		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery, ebq);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		ClassificationSchemeType res = null;
		
		if (isStatusSuccess(rr)) {
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
	
	public Collection<ClassificationNodeType> findClassificationNodesByNamePattern(String namePattern) throws JAebXRException {
		StringFilterType rf = queryFac.createStringFilterType();
		rf.setComparator(SimpleFilterType.Comparator.LIKE);
		rf.setDomainAttribute("code");
		rf.setValue(namePattern);
		
		ClassificationNodeQueryType q = queryFac.createClassificationNodeQueryType();
		q.setPrimaryFilter(rf);
		JAXBElement<ClassificationNodeQueryType> ebq = queryFac.createClassificationNodeQuery(q);

		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery, ebq);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		Collection<ClassificationNodeType> res = new ArrayList<ClassificationNodeType>();
		
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			while (i.hasNext()) {
				res.add((ClassificationNodeType) i.next().getValue());
			}
		}

		if (res.size() == 0)
			res = null;

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

		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery, ebq);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);
		
		ClassificationNodeType res = null;
		
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			if (i.hasNext()) {
				res = (ClassificationNodeType) i.next().getValue();
			}
		}

		return res;
	}
	
	public Collection<RegistryObjectType> findAllMyObjects() throws JAebXRException {
		ValueListType vl1 = rimFac.createValueListType();
		vl1.getValue().add(CanonicalConstants.CANONICAL_QUERY_FindAllMyObjects);
		
		SlotType1 s1 = rimFac.createSlotType1();
		s1.setName("urn:oasis:names:tc:ebxml-regrep:rs:AdhocQueryRequest:queryId");
		s1.setValueList(vl1);
		
		Collection<SlotType1> params = new ArrayList<SlotType1>();
		params.add(s1);
		
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeStoredQuery(params);		
		Collection<RegistryObjectType> roList = new ArrayList<RegistryObjectType>();
		
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();			
			while (i.hasNext()) {
				roList.add((RegistryObjectType) i.next().getValue());
			}
		}
		
		if (roList.size() == 0)
			roList = null;
		
		return roList;
	}

	public Collection<AssociationType1> findCallerAssociations(String id) throws JAebXRException {
        String sqlQuery =
                "SELECT DISTINCT a.* FROM Association a, AuditableEvent e, AffectedObject o, Slot s1, Slot s2 WHERE " +
                "e.user_ = $currentUser AND ( e.eventType = '" +
                CanonicalConstants.CANONICAL_EVENT_TYPE_ID_Created +
                "' OR e.eventType = '" +
                CanonicalConstants.CANONICAL_EVENT_TYPE_ID_Versioned +
                "' OR e.eventType = '" +
                CanonicalConstants.CANONICAL_EVENT_TYPE_ID_Relocated +
                "') AND o.eventId = e.id AND (o.id = a.sourceObject OR o.id = a.targetObject)";
        
        if (id != null)
                sqlQuery += " AND a.associationType = '" + id + "'";
        
        AdhocQueryType q = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_SQL_92, sqlQuery);
        AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(q);
        
        Collection<AssociationType1> res = new ArrayList<AssociationType1>();
        
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();			
			while (i.hasNext()) {
				res.add((AssociationType1) i.next().getValue());
			}
		}
       
		return res;
	}
	
	public Collection<String> findRegistryObjectsByNamePattern(String namePattern) throws JAebXRException {
        String sqlQuery = "SELECT ro.* FROM RegistryObject ro, Name_ nm WHERE nm.value LIKE '" + namePattern + "' AND ro.id = nm.parent";
        
        AdhocQueryType q = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_SQL_92, sqlQuery);
        AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(q);
        
        Collection<String> res = new ArrayList<String>();
        
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();			
			while (i.hasNext()) {
				res.add((String) i.next().getValue().getId());
			}
		}
       
		return res;
	}
	
	public RegistryObjectType findRegistryObjectByNameAndType(String roName, String roType) throws JAebXRException {
		String sqlQuery = "select ro.* from registryobject ro, name_ nm where ro.id = nm.parent and nm.value = '" + roName + "'" +
		        " and objecttype = '" + roType + "'";
        
        AdhocQueryType q = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_SQL_92, sqlQuery);
        AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(q);
        
        RegistryObjectType res = null;
        
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();			
			while (i.hasNext()) {
				res = (RegistryObjectType) i.next().getValue();
			}
		}
       
		return res;
	}
	
	public Collection<ServiceBindingType> findServiceBindingsByClassificationNode(String id) throws JAebXRException {
        String sqlQuery = "SELECT sb.* FROM ServiceBinding sb WHERE id IN (SELECT servicebinding FROM SpecificationLink WHERE specificationobject = '" + id + "')";
        
        AdhocQueryType q = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_SQL_92, sqlQuery);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(q);

		Collection<ServiceBindingType> sList = new ArrayList<ServiceBindingType>();

		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();			
			while (i.hasNext()) {
				sList.add((ServiceBindingType)i.next().getValue());
			}
		}

		return sList;
	}

	public Collection<OrganizationType> findOrganizations() throws JAebXRException {
		OrganizationQueryType q = queryFac.createOrganizationQueryType();
		JAXBElement<OrganizationQueryType> ebq = queryFac.createOrganizationQuery(q);

		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery, ebq);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		Collection<OrganizationType> sList = new ArrayList<OrganizationType>();

		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();			
			while (i.hasNext()) {
				sList.add((OrganizationType)i.next().getValue());
			}
		}

		return sList;		
	}
	
	public Collection<ServiceType> findServices() throws JAebXRException {
		ServiceQueryType q = queryFac.createServiceQueryType();
		JAXBElement<ServiceQueryType> ebq = queryFac.createServiceQuery(q);

		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery, ebq);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		Collection<ServiceType> sList = new ArrayList<ServiceType>();

		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();			
			while (i.hasNext()) {
				sList.add((ServiceType)i.next().getValue());
			}
		}

		return sList;		
	}
	
	public Collection<ServiceType> findServices(String orgId, String namePattern) throws JAebXRException {
        String sqlQuery = "SELECT ss.* FROM Service ss WHERE (name LIKE '" + namePattern + "') AND (id IN (SELECT s.id FROM Service s, Association a, Organization o WHERE " +
                "s.id = a.targetobject and a.associationtype = '" +
                CanonicalConstants.CANONICAL_ASSOCIATION_TYPE_ID_OffersService +
                "' and a.sourceobject = '" + orgId + "'))";
        
        AdhocQueryType q = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_SQL_92, sqlQuery);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(q);

		Collection<ServiceType> sList = new ArrayList<ServiceType>();

		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();			
			while (i.hasNext()) {
				sList.add((ServiceType)i.next().getValue());
			}
		}

		return sList;		
	}
	
	public Collection<ServiceType> findServicesByOrganization(String orgId) throws JAebXRException {
        String sqlQuery = "SELECT ss.* FROM Service ss WHERE id IN (SELECT s.id FROM Service s, Association a, Organization o WHERE " +
                "s.id = a.targetobject and a.associationtype = '" +
                CanonicalConstants.CANONICAL_ASSOCIATION_TYPE_ID_OffersService +
                "' and a.sourceobject = '" + orgId + "')";
        
        AdhocQueryType q = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_SQL_92, sqlQuery);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(q);

		Collection<ServiceType> sList = new ArrayList<ServiceType>();

		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();			
			while (i.hasNext()) {
				sList.add((ServiceType)i.next().getValue());
			}
		}

		return sList;		
	}
	
	public Collection<ServiceType> findServicesByClassificationNode(String id) throws JAebXRException {
        String sqlQuery = "SELECT s.* FROM Service s WHERE id IN (SELECT service FROM ServiceBinding WHERE id IN (SELECT servicebinding FROM SpecificationLink WHERE specificationobject = '" + id + "'))";
        
        AdhocQueryType q = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_SQL_92, sqlQuery);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(q);

		Collection<ServiceType> sList = new ArrayList<ServiceType>();

		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();			
			while (i.hasNext()) {
				sList.add((ServiceType)i.next().getValue());
			}
		}

		return sList;
	}

	public Collection<ServiceType> findServicesByNamePattern(String namePattern) throws JAebXRException {
		StringFilterType rf = queryFac.createStringFilterType();
		rf.setComparator(SimpleFilterType.Comparator.LIKE);
		rf.setDomainAttribute("name");
		rf.setValue(namePattern);
		
		ServiceQueryType q = queryFac.createServiceQueryType();
		q.setPrimaryFilter(rf);
		JAXBElement<ServiceQueryType> ebq = queryFac.createServiceQuery(q);

		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery, ebq);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		Collection<ServiceType> res = new ArrayList<ServiceType>();
		
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			while (i.hasNext()) {
				res.add((ServiceType) i.next().getValue());
			}
		}

		return res;
	}
	
	public Collection<AuditableEventType> getAuditTrailForRegistryObject(String id) throws JAebXRException {
		ValueListType vl1 = rimFac.createValueListType();
		vl1.getValue().add(CanonicalConstants.CANONICAL_QUERY_GetAuditTrailForRegistryObject);
		
		SlotType1 s1 = rimFac.createSlotType1();
		s1.setName("urn:oasis:names:tc:ebxml-regrep:rs:AdhocQueryRequest:queryId");
		s1.setValueList(vl1);

		ValueListType vl2 = rimFac.createValueListType();
		vl2.getValue().add(id);

		SlotType1 s2 = rimFac.createSlotType1();
		s2.setName("$lid");
		s2.setValueList(vl2);
		
		Collection<SlotType1> params = new ArrayList<SlotType1>();
		params.add(s1);
		params.add(s2);
		
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeStoredQuery(params);
			
		Collection<AuditableEventType> aeList = new ArrayList<AuditableEventType>();
		
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();			
			while (i.hasNext()) {
				aeList.add((AuditableEventType) i.next().getValue());
			}
		}
		
		return aeList;
	}
	
	public UserType getCallersUser() throws JAebXRException {
		ValueListType vl1 = rimFac.createValueListType();
		vl1.getValue().add(CanonicalConstants.CANONICAL_QUERY_GetCallersUser);
		
		SlotType1 s1 = rimFac.createSlotType1();
		s1.setName("urn:oasis:names:tc:ebxml-regrep:rs:AdhocQueryRequest:queryId");
		s1.setValueList(vl1);
		
		Collection<SlotType1> params = new ArrayList<SlotType1>();
		params.add(s1);
		
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeStoredQuery(params);
		
		UserType res = null;
		
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			if (i.hasNext()) {
				res = (UserType) i.next().getValue();
			}
			
	        if (i.hasNext()) {
	            throw new JAebXRException("ClassificationScheme multiple match");
	        }
		}
		
		return res;
	}
	
	public Collection<ClassificationSchemeType> getClassificationSchemesById(String id) throws JAebXRException {
		ValueListType vl1 = rimFac.createValueListType();
		vl1.getValue().add(CanonicalConstants.CANONICAL_QUERY_GetClassificationSchemesById);
		
		SlotType1 s1 = rimFac.createSlotType1();
		s1.setName("urn:oasis:names:tc:ebxml-regrep:rs:AdhocQueryRequest:queryId");
		s1.setValueList(vl1);

		ValueListType vl2 = rimFac.createValueListType();
		vl2.getValue().add(id);

		SlotType1 s2 = rimFac.createSlotType1();
		s2.setName("$id");
		s2.setValueList(vl2);
		
		Collection<SlotType1> params = new ArrayList<SlotType1>();
		params.add(s1);
		params.add(s2);
		
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeStoredQuery(params);	
		
		Collection<ClassificationSchemeType> res = new ArrayList<ClassificationSchemeType>();
		
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			while (i.hasNext()) {				
				res.add((ClassificationSchemeType) i.next().getValue());
			}
		}
		
		return res;
	}
	
	public Collection<AssociationType1> getAssociations(RegistryObjectType ro) throws JAebXRException {
		StringFilterType f = queryFac.createStringFilterType();		
		f.setComparator(SimpleFilterType.Comparator.EQ);
		f.setDomainAttribute("sourceObject");
		f.setValue(ro.getId());
		
		AssociationQueryType aq = queryFac.createAssociationQueryType();
		aq.setPrimaryFilter(f);
		JAXBElement<AssociationQueryType> ebq = queryFac.createAssociationQuery(aq);
		
		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery, ebq);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		Collection<AssociationType1> res = new ArrayList<AssociationType1>();

		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			while (i.hasNext()) {				
				res.add((AssociationType1) i.next().getValue());
			}
		}

		if (res.isEmpty())
			res = null;
		
		return null;
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
		
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			if (i.hasNext()) {
				res = (RegistryObjectType) i.next().getValue();
			}
			
	        if (i.hasNext()) {
	            throw new JAebXRException("RegistryObject multiple match");
	        }
		}
		
		return res;
	}

	public RegistryObjectType getRegistryObjectType(String id, String objectType) throws JAebXRException {
		RegistryObjectType res = getRegistryObjectType(id);
		if (res != null)
			if (res.getObjectType().equals(objectType))
				return res;
		return null;
	}
	
	public Collection<AuditableEventType> getAuditTrail(RegistryObjectType ro) throws JAebXRException {
		/*
		StringFilterType f = queryFac.createStringFilterType();		
		f.setComparator(SimpleFilterType.Comparator.EQ);
		f.setDomainAttribute("id");
		f.setValue(ro.getLid());
		
		RegistryObjectQueryType roq = queryFac.createRegistryObjectQueryType();
		roq.setPrimaryFilter(f);
		
		AuditableEventQueryType aeq = queryFac.createAuditableEventQueryType();
		aeq.getAffectedObjectQuery().add(roq);
		JAXBElement<AuditableEventQueryType> ebq = queryFac.createAuditableEventQuery(aeq);

		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery, ebq);
		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery, query);
		*/
		String query = "SELECT ae.* FROM AuditableEvent ae, AffectedObject ao, RegistryObject ro WHERE ro.lid='" + ro.getLid() +
				"' AND ro.id = ao.id AND ao.eventId = ae.id ORDER BY ae.timeStamp_ ASC";

		AdhocQueryType aqt = dqm.createQuery(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_SQL_92, query);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		Collection<AuditableEventType> auditTrail = new ArrayList<AuditableEventType>();

		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			while (i.hasNext()) {				
				auditTrail.add((AuditableEventType) i.next().getValue());
			}
		}

		return auditTrail;
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
	

    public boolean isStatusSuccess(RegistryResponseType rr) {
    	return rr.getStatus().equals(CanonicalConstants.CANONICAL_RESPONSE_STATUS_TYPE_LID_Success);
    }
}
