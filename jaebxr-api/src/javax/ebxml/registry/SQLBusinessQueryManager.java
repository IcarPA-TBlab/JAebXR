package javax.ebxml.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBElement;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryService;

import org.oasis.ebxml.registry.bindings.query.AdhocQueryResponse;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;

public class SQLBusinessQueryManager extends BusinessQueryManager {

	public SQLBusinessQueryManager(DeclarativeQueryManager qm)
			throws JAXRException {
		super(qm);
	}

	public SQLBusinessQueryManager(RegistryService rs) throws JAXRException {
		super(rs);
	}

	public ClassificationSchemeType findClassificationSchemeByName(String name) throws JAebXRException {
		ClassificationSchemeType res = null;
		String sqlQuery = "SELECT cs.* FROM ClassificationScheme cs WHERE name = '" + name + "'";

		AdhocQueryType aqt = dqm.createSQLQuery(sqlQuery);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			if (i.hasNext()) {
				res = (ClassificationSchemeType) i.next().getValue();
				res.getClassificationNode().addAll(getChildrens(res.getId()));
				cache.put(res.getId(), res);
			}
			
	        if (i.hasNext()) {
	            throw new JAebXRException("ClassificationScheme multiple match");
	        }
		}
		
		return res;
	}

	public ClassificationSchemeType findClassificationSchemeByPattern(String namePattern) throws JAebXRException {
		ClassificationSchemeType res = null;
		String sqlQuery = "SELECT cs.* FROM ClassificationScheme cs WHERE name LIKE '" + namePattern + "'";

		AdhocQueryType aqt = dqm.createSQLQuery(sqlQuery);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			if (i.hasNext()) {
				res = (ClassificationSchemeType) i.next().getValue();
				res.getClassificationNode().addAll(getChildrens(res.getId()));
				cache.put(res.getId(), res);
			}
			
	        if (i.hasNext()) {
	            throw new JAebXRException("ClassificationScheme multiple match");
	        }
		}
		
		return res;
	}

	public Collection<ClassificationNodeType> findClassificationNodesByNamePattern(String namePattern) throws JAebXRException {
		String sqlQuery = "SELECT cn.* FROM ClassificationNode cn WHERE code LIKE '" + namePattern + "'";

		AdhocQueryType aqt = dqm.createSQLQuery(sqlQuery);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		Collection<ClassificationNodeType> res = new ArrayList<ClassificationNodeType>();
		
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			while (i.hasNext()) {
				ClassificationNodeType node = (ClassificationNodeType) i.next().getValue();
				node.getClassificationNode().addAll(getChildrens(node.getId()));
				res.add(node);
			}
		}

		return res;
	}


	public ClassificationNodeType findClassificationNodeByPath(String path) throws JAebXRException {
		ClassificationNodeType res = (ClassificationNodeType) cache.peek(path);
		if (res != null)
			return res;
		
		StringTokenizer st = new StringTokenizer(path, "/");		
		String parent = st.nextToken();
		String code = st.nextToken();

		String sqlQuery = "SELECT cn.* FROM ClassificationNode cn WHERE code = '" + code + "' AND parent = '" + parent +"'";

		AdhocQueryType aqt = dqm.createSQLQuery(sqlQuery);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);
		
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			if (i.hasNext()) {
				res = (ClassificationNodeType) i.next().getValue();
				res.getClassificationNode().addAll(getChildrens(res.getId()));
				cache.put(path, res);
			}
		}

		return res;
	}

	public Collection<AssociationType1> getAssociations(RegistryObjectType ro) throws JAebXRException {
		String sqlQuery = "SELECT a.* FROM Association a WHERE sourceObject = '" + ro.getId() + "'";

		AdhocQueryType aqt = dqm.createSQLQuery(sqlQuery);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		Collection<AssociationType1> res = new ArrayList<AssociationType1>();

		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			while (i.hasNext()) {				
				res.add((AssociationType1) i.next().getValue());
			}
		}

		return res;
	}
	
	public Collection<ClassificationNodeType> getChildrens(String id) throws JAebXRException {
		String sqlQuery = "SELECT cn.* FROM ClassificationNode cn WHERE parent = '" + id + "'";

		AdhocQueryType aqt = dqm.createSQLQuery(sqlQuery);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		Collection<ClassificationNodeType> res = new ArrayList<ClassificationNodeType>();

		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			while (i.hasNext()) {
				ClassificationNodeType node = (ClassificationNodeType) i.next().getValue();
				res.add(node);
				res.addAll(getChildrens(node.getId()));
			}
		}

		return res;
	}

	public RegistryObjectType getRegistryObjectType(String id) throws JAebXRException {
		RegistryObjectType res = cache.peek(id);
		if (res != null)
			return res;
		
		String sqlQuery = "SELECT ro.* FROM RegistryObject ro WHERE id = '" + id + "'";

		AdhocQueryType aqt = dqm.createSQLQuery(sqlQuery);
		AdhocQueryResponse rr = (AdhocQueryResponse) dqm.executeQuery(aqt);

		
		if (isStatusSuccess(rr)) {
			Iterator<JAXBElement<? extends IdentifiableType>> i = rr.getRegistryObjectList().getIdentifiable().iterator();
			if (i.hasNext()) {
				res = (RegistryObjectType) i.next().getValue();
				if (res instanceof ClassificationSchemeType) {
					ClassificationSchemeType cs = (ClassificationSchemeType)res;
					cs.getClassificationNode().addAll(getChildrens(res.getId()));
					cache.put(id, cs);
					return cs;
				} else if (res instanceof ClassificationNodeType) {
					ClassificationNodeType cn = (ClassificationNodeType)res;
					cn.getClassificationNode().addAll(getChildrens(res.getId()));
					cache.put(id, cn);
					return cn;					
				} else if (res instanceof RegistryPackageType) {
					RegistryPackageType rp = (RegistryPackageType)res;
					RegistryObjectListType members = rimFac.createRegistryObjectListType();
					Collection<AssociationType1> ass = this.findAssociations(id);
					Iterator<AssociationType1> iter = ass.iterator();
					while (iter.hasNext()) {
						AssociationType1 a = iter.next();
						if (a.getAssociationType().equals(CanonicalConstants.CANONICAL_ASSOCIATION_TYPE_ID_HasMember)) {
							RegistryObjectType target = getRegistryObjectType(a.getTargetObject());
							members.getIdentifiable().add((JAebXRClient.getInstance().getLifeCycleManager().createRegistryObject(target)));
						}
					}
					rp.setRegistryObjectList(members);
					cache.put(id, rp);
					return rp;
				}
			}
			
	        if (i.hasNext()) {
	            throw new JAebXRException("RegistryObject multiple match");
	        }
		}
		
		return res;
	}
}
