package javax.ebxml.registry;

import java.math.BigInteger;
import java.util.Collection;

import javax.ebxml.registry.soap.BindingUtility;
import javax.ebxml.registry.soap.SOAPMessenger;
import javax.xml.bind.JAXBException;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.Query;
import javax.xml.registry.RegistryException;
import javax.xml.registry.UnsupportedCapabilityException;

import org.oasis.ebxml.registry.bindings.query.AdhocQueryRequest;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType;
import org.oasis.ebxml.registry.bindings.query.ResponseOptionType.ReturnType;
import org.oasis.ebxml.registry.bindings.rim.AdhocQueryType;
import org.oasis.ebxml.registry.bindings.rim.QueryExpressionType;
import org.oasis.ebxml.registry.bindings.rim.SlotListType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

public class DeclarativeQueryManager extends QueryManager implements javax.xml.registry.DeclarativeQueryManager {

	private javax.xml.registry.DeclarativeQueryManager dqm = null;

	private SOAPMessenger msgr = ConfigurationFactory.getInstance().getSOAPMessenger();
	
	public DeclarativeQueryManager(javax.xml.registry.RegistryService rs) throws UnsupportedCapabilityException, JAXRException {
		super();
		if (rs != null) {
			this.dqm = rs.getDeclarativeQueryManager();
			this.setRegistryService(rs);
		}
	}

	public void setDeclarativeQueryManager(javax.xml.registry.DeclarativeQueryManager dqm) {
		this.dqm = dqm;
	}
	
	@Override
	public Query createQuery(int arg0, String arg1)
			throws InvalidRequestException, JAXRException {
		return dqm.createQuery(arg0, arg1);
	}

	public AdhocQueryType createQuery(String queryType, Object queryString) throws JAebXRException {
		if ((!queryType.equals(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_ebRSFilterQuery)) &&
		    (!queryType.equals(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_SQL_92)))
				throw new JAebXRException("Unsupported query language: " + queryType);
		
		QueryExpressionType qet = rimFac.createQueryExpressionType();
		qet.setQueryLanguage(queryType);
		
		if (queryType.equals(CanonicalConstants.CANONICAL_QUERY_LANGUAGE_LID_SQL_92))
			qet.getContent().add(queryString);
		else {
			try {
				qet.getContent().add(BindingUtility.getInstance().marshalObject(queryString));
			} catch (JAXBException e1) {
				throw new JAebXRException(e1);
			}
		}

		AdhocQueryType aqt = rimFac.createAdhocQueryType();
		
		aqt.setId(createUUID());		
		aqt.setQueryExpression(qet);

		return aqt;
	}
	
	@Override
	public BulkResponse executeQuery(Query arg0) throws JAXRException {
		return dqm.executeQuery(arg0);
	}

	public RegistryResponseType executeQuery(AdhocQueryType query) throws JAebXRException {
		return executeQuery(query, ReturnType.LEAF_CLASS);
	}
	
	public RegistryResponseType executeQuery(AdhocQueryType query, ReturnType rType) throws JAebXRException {
		ValueListType vlt = rimFac.createValueListType();
		vlt.getValue().add("true");
		
		SlotType1 st = rimFac.createSlotType1();
		st.setName(CanonicalConstants.IMPL_SLOT_CREATE_HTTP_SESSION);
		st.setValueList(vlt);
		
		SlotListType slt = rimFac.createSlotListType();
		slt.getSlot().add(st);

		ResponseOptionType rot = queryFac.createResponseOptionType();
		rot.setReturnComposedObjects(true);
		rot.setReturnType(rType);
		
		AdhocQueryRequest aqr = queryFac.createAdhocQueryRequest();
		
		aqr.setId(createUUID());
		
		aqr.setMaxResults(BigInteger.valueOf(-1));
		aqr.setStartIndex(BigInteger.valueOf(0));
		aqr.setFederated(false);
		
		aqr.setAdhocQuery(query);
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
	
	public RegistryResponseType executeStoredQuery(Collection<SlotType1> params) throws JAebXRException {
		ValueListType vlt = rimFac.createValueListType();
		vlt.getValue().add("true");
		
		SlotType1 st = rimFac.createSlotType1();
		st.setName(CanonicalConstants.IMPL_SLOT_CREATE_HTTP_SESSION);
		st.setValueList(vlt);
		
		SlotListType slt = rimFac.createSlotListType();
		slt.getSlot().add(st);
		slt.getSlot().addAll(params);

		ResponseOptionType rot = queryFac.createResponseOptionType();
		rot.setReturnComposedObjects(true);
		rot.setReturnType(ReturnType.LEAF_CLASS_WITH_REPOSITORY_ITEM);
		
		AdhocQueryRequest aqr = queryFac.createAdhocQueryRequest();

		aqr.setId(createUUID());
		
		aqr.setMaxResults(BigInteger.valueOf(-1));
		aqr.setStartIndex(BigInteger.valueOf(0));
		aqr.setFederated(false);
		
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
}
