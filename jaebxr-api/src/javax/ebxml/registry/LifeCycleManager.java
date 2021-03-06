package javax.ebxml.registry;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.activation.DataHandler;
import javax.ebxml.registry.soap.BindingUtility;
import javax.ebxml.registry.soap.SOAPMessenger;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.registry.BulkResponse;
import javax.xml.registry.InvalidRequestException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.registry.UnsupportedCapabilityException;
import javax.xml.registry.infomodel.Association;
import javax.xml.registry.infomodel.Classification;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.EmailAddress;
import javax.xml.registry.infomodel.ExternalIdentifier;
import javax.xml.registry.infomodel.ExternalLink;
import javax.xml.registry.infomodel.ExtrinsicObject;
import javax.xml.registry.infomodel.InternationalString;
import javax.xml.registry.infomodel.Key;
import javax.xml.registry.infomodel.LocalizedString;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.PersonName;
import javax.xml.registry.infomodel.PostalAddress;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import javax.xml.registry.infomodel.Slot;
import javax.xml.registry.infomodel.SpecificationLink;
import javax.xml.registry.infomodel.TelephoneNumber;
import javax.xml.registry.infomodel.User;

import org.cache2k.Cache;
import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.UpdateObjectsRequest;
import org.oasis.ebxml.registry.bindings.rim.AssociationType1;
import org.oasis.ebxml.registry.bindings.rim.ClassificationNodeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationSchemeType;
import org.oasis.ebxml.registry.bindings.rim.ClassificationType;
import org.oasis.ebxml.registry.bindings.rim.EmailAddressType;
import org.oasis.ebxml.registry.bindings.rim.ExternalIdentifierType;
import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.ExtrinsicObjectType;
import org.oasis.ebxml.registry.bindings.rim.FederationType;
import org.oasis.ebxml.registry.bindings.rim.IdentifiableType;
import org.oasis.ebxml.registry.bindings.rim.InternationalStringType;
import org.oasis.ebxml.registry.bindings.rim.LocalizedStringType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.OrganizationType;
import org.oasis.ebxml.registry.bindings.rim.PersonNameType;
import org.oasis.ebxml.registry.bindings.rim.PersonType;
import org.oasis.ebxml.registry.bindings.rim.PostalAddressType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectListType;
import org.oasis.ebxml.registry.bindings.rim.RegistryObjectType;
import org.oasis.ebxml.registry.bindings.rim.RegistryPackageType;
import org.oasis.ebxml.registry.bindings.rim.RegistryType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rim.ServiceType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.SpecificationLinkType;
import org.oasis.ebxml.registry.bindings.rim.TelephoneNumberType;
import org.oasis.ebxml.registry.bindings.rim.UserType;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

public class LifeCycleManager extends CanonicalConstants implements javax.xml.registry.LifeCycleManager {

    private javax.xml.registry.LifeCycleManager lcm = null;
	private RegistryService rs = null;
	
	protected Cache<String, RegistryObjectType> cache = null;

	private SOAPMessenger msgr = null;
	
	private static BindingUtility bu = BindingUtility.getInstance();
	
    protected org.oasis.ebxml.registry.bindings.rim.ObjectFactory rimFac;
    private org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFac;
    protected org.oasis.ebxml.registry.bindings.lcm.ObjectFactory lcmFac;
    //private org.oasis.ebxml.registry.bindings.query.ObjectFactory queryFac;
    //private org.oasis.ebxml.registry.bindings.cms.ObjectFactory cmsFac;

	public LifeCycleManager() {
        rimFac = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
        rsFac = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();
        lcmFac = new org.oasis.ebxml.registry.bindings.lcm.ObjectFactory();
        //queryFac = new org.oasis.ebxml.registry.bindings.query.ObjectFactory();
        //cmsFac = new org.oasis.ebxml.registry.bindings.cms.ObjectFactory();		
	}
	
	protected void setCache(Cache<String, RegistryObjectType> c) {
		this.cache = c;
	}
	
	protected void setSOAPMessenger(SOAPMessenger msgr) {
		this.msgr = msgr;
	}
	
    protected void setLifeCycleManager(javax.xml.registry.LifeCycleManager lcm) {
    	this.lcm = lcm;
    }
    
    protected void setRegistryService(RegistryService rs) {
    	this.rs = rs;
    }
    
    /*
    public boolean isStatusSuccess(RegistryResponseType rr) {
    	return rr.getStatus().equals(CanonicalConstants.CANONICAL_RESPONSE_STATUS_TYPE_LID_Success);
    }
    */

    public RegistryResponseType checkResponseAndRemoveFromCache(RegistryResponseType r, String id) {
    	if (r.getStatus().equals(CanonicalConstants.CANONICAL_RESPONSE_STATUS_TYPE_LID_Success))
    		cache.remove(id);
    	return r;
    }

    public RegistryResponseType checkResponseAndRemoveFromCache(RegistryResponseType r, Collection<?> ids) {
    	if (r.getStatus().equals(CanonicalConstants.CANONICAL_RESPONSE_STATUS_TYPE_LID_Success)) {
    		Iterator<?> it = ids.iterator();
    		while (it.hasNext()) {
    			Object o = it.next();
    			if (o instanceof String)
    				cache.remove((String)o);
    			else
    				cache.remove(((RegistryObjectType)o).getId());
    		}
    	}
    	return r;
    }

    public RegistryResponseType checkResponseAndSaveToCache(RegistryResponseType r, RegistryObjectType ro) {
    	if (r.getStatus().equals(CanonicalConstants.CANONICAL_RESPONSE_STATUS_TYPE_LID_Success))
    		cache.put(ro.getId(), ro);
    	return r;
    }
    
    @Override
	public Association createAssociation(RegistryObject arg0, Concept arg1)
			throws JAXRException {
		return lcm.createAssociation(arg0, arg1);
	}

    public JAXBElement<AssociationType1> createAssociation(AssociationType1 a) {
    	return rimFac.createAssociation(a);
    }
    
    public AssociationType1 createAssociationType(RegistryObjectType srcId, RegistryObjectType desId, ClassificationNodeType type) {
    	return createAssociationType(srcId.getId(), desId.getId(), null, null, type.getId());
    }
    
    public AssociationType1 createAssociationType(String srcId, String desId, String type) {
    	return createAssociationType(srcId, desId, null, null, type);
    }
    
    public AssociationType1 createAssociationType(String srcId, String desId, String name, String description, String type) {
    	AssociationType1 a = rimFac.createAssociationType1();
    	a.setId(this.createUUID());
    	a.setLid(a.getId());
    	a.setSourceObject(srcId);
    	a.setTargetObject(desId);
    	if (name != null)
    		a.setName(createInternationalStringType(name));
    	if (description != null)
    		a.setDescription(createInternationalStringType(description));
    	a.setAssociationType(type);
    	a.setObjectType(CANONICAL_OBJECT_TYPE_ID_Association);
    	return a;
    }
    
    public ClassificationType createClassification() {
    	ClassificationType c = rimFac.createClassificationType();
    	c.setObjectType(CANONICAL_OBJECT_TYPE_ID_Classification);
		return c;	
    }
    
	@Override
	public Classification createClassification(Concept arg0)
			throws JAXRException, InvalidRequestException {
		return lcm.createClassification(arg0);
	}

	@Override
	public Classification createClassification(ClassificationScheme arg0,
			String arg1, String arg2) throws JAXRException {
		return lcm.createClassification(arg0, arg1, arg2);
	}

	@Override
	public Classification createClassification(ClassificationScheme arg0,
			InternationalString arg1, String arg2) throws JAXRException {
		return lcm.createClassification(arg0, arg1, arg2);
	}

    public JAXBElement<ClassificationNodeType> createClassificationNode(ClassificationNodeType cs) {
    	JAXBElement<ClassificationNodeType> eb = rimFac.createClassificationNode(cs);
    	return eb;
    }
    
    public JAXBElement<ClassificationType> createClassification(ClassificationType cs) {
    	JAXBElement<ClassificationType> eb = rimFac.createClassification(cs);
    	return eb;
    }
  
    public ClassificationType createClassificationType(ClassificationSchemeType scheme) {
		return createClassificationType(scheme, null, null);
	}

	public ClassificationType createClassificationType(ClassificationSchemeType scheme, String name, String value) {
		ClassificationType c = rimFac.createClassificationType();
		c.setId(this.createUUID());
		c.setLid(c.getId());
		c.setClassificationScheme(scheme.getId());
		c.setObjectType(CANONICAL_OBJECT_TYPE_ID_Classification);
		
		if (name != null)
			c.setName(createInternationalStringType(name));
		if (value != null)
			c.setNodeRepresentation(value);
		
		return c;
	}
	
	public ClassificationType createClassificationType(ClassificationNodeType node) {
		return createClassificationType(node, null, null);
	}

	public ClassificationType createClassificationType(ClassificationNodeType node, InternationalStringType name, String value) {
		ClassificationType c = rimFac.createClassificationType();
		c.setId(this.createUUID());
		c.setLid(c.getId());
		c.setClassificationNode(node.getId());
		c.setObjectType(CANONICAL_OBJECT_TYPE_ID_Classification);
		
		if (name != null)
			c.setName(name);
		if (value != null)
			c.setNodeRepresentation(value);
		
		return c;
	}
	
	@Override
	public ClassificationScheme createClassificationScheme(Concept arg0)
			throws JAXRException, InvalidRequestException {
		return lcm.createClassificationScheme(arg0);
	}

	@Override
	public ClassificationScheme createClassificationScheme(String arg0,
			String arg1) throws JAXRException, InvalidRequestException {
		return lcm.createClassificationScheme(arg0, arg1);
	}

	@Override
	public ClassificationScheme createClassificationScheme(
			InternationalString arg0, InternationalString arg1)
			throws JAXRException, InvalidRequestException {
		return lcm.createClassificationScheme(arg0, arg1);
	}

	public ClassificationSchemeType createClassificationSchemeType(ClassificationNodeType cn) {
		ClassificationSchemeType cs = rimFac.createClassificationSchemeType();
		cs.setId(this.createUUID());
		cs.setLid(cs.getId());
		cs.setName(cn.getName());
		cs.setDescription(cn.getDescription());
		cs.getClassification().addAll(cn.getClassification());
		cs.getExternalIdentifier().addAll(cn.getExternalIdentifier());
		cs.setNodeType(CANONICAL_NODE_TYPE_ID_UniqueCode);
		cs.setIsInternal(true);
		cs.setObjectType(CANONICAL_OBJECT_TYPE_ID_ClassificationScheme);
		return cs;
	}
	
	public ClassificationSchemeType createClassificationSchemeType(String name, String description) {
		return createClassificationSchemeType(createInternationalStringType(name), createInternationalStringType(description));
	}

	public ClassificationSchemeType createClassificationSchemeType(InternationalStringType name, InternationalStringType description) {
		ClassificationSchemeType cs = rimFac.createClassificationSchemeType();
		cs.setId(this.createUUID());
		cs.setLid(cs.getId());
		cs.setName(name);
		cs.setDescription(description);
		cs.setIsInternal(true);
		cs.setObjectType(CANONICAL_OBJECT_TYPE_ID_ClassificationScheme);
		return cs;
	}

    public JAXBElement<ClassificationSchemeType> createClassificationScheme(ClassificationSchemeType cs) {
    	JAXBElement<ClassificationSchemeType> eb = rimFac.createClassificationScheme(cs);
    	return eb;
    }
    
	@Override
	public Concept createConcept(RegistryObject arg0, String arg1, String arg2)
			throws JAXRException {
		return lcm.createConcept(arg0, arg1, arg2);
	}

	@Override
	public Concept createConcept(RegistryObject arg0, InternationalString arg1,
			String arg2) throws JAXRException {
		return lcm.createConcept(arg0, arg1, arg2);
	}

	public ClassificationNodeType createClassificationNodeType(RegistryObjectType parent, String name, String value) {
		return createClassificationNodeType(parent, createInternationalStringType(name), value);
	}

	public ClassificationNodeType createClassificationNodeType(RegistryObjectType parent, InternationalStringType name, String value) {
		ClassificationNodeType cn = rimFac.createClassificationNodeType();
		cn.setId(this.createUUID());
		cn.setLid(cn.getId());
		if (parent != null)
			cn.setParent(parent.getId());
		cn.setName(name);
		cn.setCode(value);
		cn.setDescription(name);
		cn.setObjectType(CANONICAL_OBJECT_TYPE_ID_ClassificationNode);
		return cn;
	}

	public JAXBElement<ClassificationNodeType> createClassificationNodeType(ClassificationNodeType cn) {
		JAXBElement<ClassificationNodeType> eb = rimFac.createClassificationNode(cn);
		return eb;
	}
	
	@Override
	public EmailAddress createEmailAddress(String arg0) throws JAXRException {
		return lcm.createEmailAddress(arg0);
	}

	@Override
	public EmailAddress createEmailAddress(String arg0, String arg1)
			throws JAXRException {
		return lcm.createEmailAddress(arg0, arg1);
	}

	public EmailAddressType createEmailAddressType(String address) throws JAXRException {
		return createEmailAddressType(address, null);
	}

	public EmailAddressType createEmailAddressType(String address, String type) throws JAXRException {
		EmailAddressType ea = rimFac.createEmailAddressType();
		ea.setAddress(address);
		if (type != null)
			ea.setType(type);
		return ea;
	}

	@Override
	public ExternalIdentifier createExternalIdentifier(
			ClassificationScheme arg0, String arg1, String arg2)
			throws JAXRException {
		return lcm.createExternalIdentifier(arg0, arg1, arg2);
	}

	@Override
	public ExternalIdentifier createExternalIdentifier(
			ClassificationScheme arg0, InternationalString arg1, String arg2)
			throws JAXRException {
		return lcm.createExternalIdentifier(arg0, arg1, arg2);
	}

	public ExternalIdentifierType createExternalIdentifierType(ClassificationSchemeType scheme, String name,
			String value) {
		return createExternalIdentifierType(scheme, createInternationalStringType(name), value);
	}
	
	public ExternalIdentifierType createExternalIdentifierType(ClassificationSchemeType scheme, InternationalStringType name,
			String value) {
		ExternalIdentifierType ei = rimFac.createExternalIdentifierType();
		ei.setId(this.createUUID());
		ei.setLid(ei.getId());
		ei.setIdentificationScheme(scheme.getId());
		ei.setName(name);
		ei.setValue(value);
		ei.setObjectType(CANONICAL_OBJECT_TYPE_ID_ExternalIdentifier);
		return ei;
	}
	
	@Override
	public ExternalLink createExternalLink(String arg0, String arg1)
			throws JAXRException {
		return lcm.createExternalLink(arg0, arg1);
	}

	@Override
	public ExternalLink createExternalLink(String arg0, InternationalString arg1)
			throws JAXRException {
		return lcm.createExternalLink(arg0, arg1);
	}

	public RegistryResponseType addExternalLinkType(RegistryObjectType ro, ExternalLinkType el) throws JAebXRException {
		RegistryResponseType res = rsFac.createRegistryResponseType();
		res.setStatus(CANONICAL_RESPONSE_STATUS_TYPE_LID_Success);
		
		boolean extLinkExists = false;
		BusinessQueryManager bqm = null;
		try {
			bqm = rs.getBusinessQueryManager();
		} catch (JAXRException e) {
			throw new JAebXRException(e);
		}
		
		Collection<ExternalLinkType> els = bqm.getExternalLinks(ro);
		Iterator<ExternalLinkType> ei = els.iterator();
		while (ei.hasNext()) {
			if (ei.next().getLid().equals(el.getLid())) {
				extLinkExists = true;
				break;
			}
		}
		
		if (!extLinkExists) {
			boolean associationExists = false;
			
			ClassificationNodeType assocType = bqm.findClassificationNodeByPath("/" + CanonicalConstants.CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                    CanonicalConstants.CANONICAL_ASSOCIATION_TYPE_CODE_ExternallyLinks);
			
			Collection<AssociationType1> al = bqm.getAssociations(el);
			Iterator<AssociationType1> assIter = al.iterator();
			while (assIter.hasNext()) {
				AssociationType1 ass = assIter.next();
				
				if (ass.getSourceObject().equals(el.getLid()) &&
						ass.getTargetObject().equals(ro.getLid()) &&
						ass.getAssociationType().equals(assocType.getLid())) {
					associationExists = true;
					break;
				}
			}
			
			res = saveObjectType(el);
			
			if (!associationExists) {
				AssociationType1 ass = this.createAssociationType(el, ro, assocType);
				res = saveObjectType(ass);
			}
		}
		
		return res;
	}
	
	public ExternalLinkType createExternalLinkType(String externalURI, String description) {
		return createExternalLinkType(externalURI, createInternationalStringType(description));
	}

	public ExternalLinkType createExternalLinkType(String externalURI, InternationalStringType description) {
		ExternalLinkType el = rimFac.createExternalLinkType();
		el.setId(this.createUUID());
		el.setLid(el.getId());
		el.setExternalURI(externalURI);
		el.setDescription(description);
		el.setObjectType(CANONICAL_OBJECT_TYPE_ID_ExternalLink);
		return el;
	}
	
	@Override
	public ExtrinsicObject createExtrinsicObject(DataHandler arg0)
			throws JAXRException {
		return lcm.createExtrinsicObject(arg0);
	}

	public JAXBElement<ExtrinsicObjectType> createExtrinsicObject(ExtrinsicObjectType eo) {
    	JAXBElement<ExtrinsicObjectType> eb = rimFac.createExtrinsicObject(eo);
    	return eb;
    }

	// TODO
	public DataHandler getRepositoryItem(String eoID) {
		return null;
	}

	public ExtrinsicObjectType createExtrinsicObjectType(URL url) {
		return createExtrinsicObjectType(url, null, null);
	}
	
	public ExtrinsicObjectType createExtrinsicObjectType(URL url, String name) {
		return createExtrinsicObjectType(url, name, name);
	}

	public ExtrinsicObjectType createExtrinsicObjectType(URL url, String name, String description) {
		
		ExtrinsicObjectType eo = rimFac.createExtrinsicObjectType();
		eo.setId(this.createUUID());
		eo.setLid(eo.getId());
		eo.setObjectType(CANONICAL_OBJECT_TYPE_ID_ExtrinsicObject);
		
		if (name != null)
			eo.setName(createInternationalStringType(name));
		
		if (description != null)
			eo.setDescription(createInternationalStringType(description));
		
		setRepositoryItem(eo, url);
		
		return eo;
	}

	public void setRepositoryItem(ExtrinsicObjectType eo, URL url) {
		if ((eo != null) && (url != null)) {
			DataHandler dh = new DataHandler(url);		
			eo.setMimeType(dh.getContentType());
	
			ValueListType v1 = rimFac.createValueListType();
			v1.getValue().add(url.getPath());
	
			ValueListType v2 = rimFac.createValueListType();
			v2.getValue().add(url.toExternalForm());
			
			SlotType1 s1 = rimFac.createSlotType1();
			s1.setName("urn:oasis:names:tc:ebxml-regrep:rim:RegistryObject:locator");
			s1.setValueList(v1);
			
			SlotType1 s2 = rimFac.createSlotType1();
			s2.setName("urn:oasis:names:tc:ebxml-regrep:rim:RegistryObject:contentLocator");
			s2.setValueList(v1);
			
			SlotType1 s3 = rimFac.createSlotType1();
			s3.setName("URL");
			s3.setValueList(v2);
			
			eo.getSlot().add(s1);
			eo.getSlot().add(s2);
			eo.getSlot().add(s3);
		}		
	}
	
	public JAXBElement<FederationType> createFederation(FederationType f) {
    	JAXBElement<FederationType> eb = rimFac.createFederation(f);
    	return eb;
    }
    
    public FederationType createFederationType(String name) {
    	return createFederationType(name, null);
    }

    public FederationType createFederationType(String name, String description) {
       	FederationType f = rimFac.createFederationType();
       	f.setId(this.createUUID());
       	f.setLid(f.getId());
    	f.setName(createInternationalStringType(name));
    	if (description != null) f.setDescription(createInternationalStringType(description));
    	f.setObjectType(CANONICAL_OBJECT_TYPE_ID_Federation);
    	return f;
    }

	@Override
	public InternationalString createInternationalString() throws JAXRException {
		return lcm.createInternationalString();
	}

	@Override
	public InternationalString createInternationalString(String arg0)
			throws JAXRException {
		return lcm.createInternationalString(arg0);
	}

	@Override
	public InternationalString createInternationalString(Locale arg0,
			String arg1) throws JAXRException {
		return lcm.createInternationalString(arg0, arg1);
	}

    public JAXBElement<InternationalStringType> createInternationalString(InternationalStringType is) {
    	JAXBElement<InternationalStringType> eb = rimFac.createInternationalString(is);
    	return eb;
    }

    public InternationalStringType createInternationalStringType() {
        return createInternationalStringType(null);
    }

    public InternationalStringType createInternationalStringType(String str) {
    	InternationalStringType is = rimFac.createInternationalStringType();
    	if (str != null) {
	    	LocalizedStringType ls = createLocalizedStringType(str);
	    	is.getLocalizedString().add(ls);
    	}
        return is;
    }

	@Override
	public Key createKey(String arg0) throws JAXRException {
		return lcm.createKey(arg0);
	}

	@Override
	public LocalizedString createLocalizedString(Locale arg0, String arg1)
			throws JAXRException {
		return lcm.createLocalizedString(arg0, arg1);
	}

	@Override
	public LocalizedString createLocalizedString(Locale arg0, String arg1,
			String arg2) throws JAXRException {
		return lcm.createLocalizedString(arg0, arg1, arg2);
	}

    public JAXBElement<LocalizedStringType> createLocalizedString(LocalizedStringType ls) {
    	JAXBElement<LocalizedStringType> eb = rimFac.createLocalizedString(ls);
    	return eb;
    }

    public LocalizedStringType createLocalizedStringType(String value) {
    	return createLocalizedStringType(value, null, null);
    }
    
    public LocalizedStringType createLocalizedStringType(String value, String lang) {
    	return createLocalizedStringType(value, lang, null);
    }
    
    public LocalizedStringType createLocalizedStringType(String value, String lang, String charset) {
    	LocalizedStringType ls = rimFac.createLocalizedStringType();
    	ls.setValue(value);
    	if (lang != null) ls.setLang(lang);
    	if (charset != null) ls.setCharset(charset);
    	return ls;
    }

	@Override
	public Object createObject(String arg0) throws JAXRException,
			InvalidRequestException, UnsupportedCapabilityException {
		return lcm.createObject(arg0);
	}

	protected List<ObjectRefType> createObjectRefList(Collection<?> ids) {
		ArrayList<ObjectRefType> orl = new ArrayList<ObjectRefType>();
		String id;

		// Used to prevent duplicate keys from being sent
		HashSet<String> processedIds = new HashSet<String>();
		processedIds.add(null);

		if (ids != null) {
			for (Iterator<?> it = ids.iterator(); it.hasNext();) {
				Object o = it.next();
				
				if (o instanceof RegistryObjectType)
					id = ((RegistryObjectType) o).getId();
				else
					id = (String) o;

				if (!processedIds.contains(id)) {
					processedIds.add(id);

					ObjectRefType ebObjectRefType = rimFac.createObjectRefType();
					ebObjectRefType.setId(id);
					orl.add(ebObjectRefType);
				}
			}
		}

		return orl;
	}

	/*
	protected List<ObjectRefType> createObjectRefList(Collection<String> keys) {
		ArrayList<ObjectRefType> orl = new ArrayList<ObjectRefType>();

		// Used to prevent duplicate keys from being sent
		HashSet<String> processedIds = new HashSet<String>();
		processedIds.add(null);

		if (keys != null) {
			for (Iterator<String> it = keys.iterator(); it.hasNext();) {
				String id = it.next();

				if (!processedIds.contains(id)) {
					processedIds.add(id);

					ObjectRefType ebObjectRefType = rimFac.createObjectRefType();
					ebObjectRefType.setId(id);
					orl.add(ebObjectRefType);
				}
			}
		}

		return orl;
	}
	*/
	
	@Override
	public Organization createOrganization(String arg0) throws JAXRException {
		return lcm.createOrganization(arg0);
	}

	@Override
	public Organization createOrganization(InternationalString arg0)
			throws JAXRException {
		return lcm.createOrganization(arg0);
	}

    public JAXBElement<OrganizationType> createOrganization(OrganizationType o) {
    	JAXBElement<OrganizationType> eb = rimFac.createOrganization(o);
    	return eb;
    }
    
	public OrganizationType createOrganizationType(String name) {
		return createOrganizationType(createInternationalStringType(name));
	}

	public OrganizationType createOrganizationType(InternationalStringType name) {
		OrganizationType o = rimFac.createOrganizationType();
		o.setId(this.createUUID());
		o.setLid(o.getId());
		o.setName(name);
		o.setObjectType(CANONICAL_OBJECT_TYPE_ID_Organization);
		return o;
	}
	
	@Override
	public PersonName createPersonName(String arg0) throws JAXRException {
		return lcm.createPersonName(arg0);
	}

	@Override
	public PersonName createPersonName(String arg0, String arg1, String arg2)
			throws JAXRException {
		return lcm.createPersonName(arg0, arg1, arg2);
	}

	public PersonNameType createPersonNameType(String firstName, String middleName, String lastName) {
		PersonNameType pn = rimFac.createPersonNameType();
		pn.setFirstName(firstName);
		pn.setMiddleName(middleName);
		pn.setLastName(lastName);
		return pn;
	}

	public PersonNameType createPersonNameType(String fullName) {
		PersonNameType pn = rimFac.createPersonNameType();
		pn.setLastName(fullName);
		return pn;
	}

    public JAXBElement<PersonType> createPerson(PersonType p) {
    	JAXBElement<PersonType> eb = rimFac.createPerson(p);
    	return eb;
    }
    
	public PersonType createPersonType() {
		PersonType p = rimFac.createPersonType();
		p.setId(this.createUUID());
		p.setLid(p.getId());
		p.setName(createInternationalStringType(""));
		p.setPersonName(createPersonNameType(""));
		p.setObjectType(CANONICAL_OBJECT_TYPE_ID_Person);
		return p;
	}
	
	@Override
	public PostalAddress createPostalAddress(String arg0, String arg1,
			String arg2, String arg3, String arg4, String arg5, String arg6)
			throws JAXRException {
		return lcm.createPostalAddress(arg0, arg1, arg2, arg3, arg4, arg5, arg6);
	}

	public PostalAddressType createPostalAddressType(String streetNumber, String street, String city, String stateOrProvince,
			String country, String postalCode) {
		PostalAddressType pa = rimFac.createPostalAddressType();
		pa.setStreetNumber(streetNumber);
		pa.setStreet(street);
		pa.setCity(city);
		pa.setStateOrProvince(stateOrProvince);
		pa.setCountry(country);
		pa.setPostalCode(postalCode);
		return pa;
	}

	public RegistryObjectListType createRegistryObjectListType() {
    	return createRegistryObjectListType(null);
    }
    
    public RegistryObjectListType createRegistryObjectListType(Collection <JAXBElement<? extends IdentifiableType>> ebl) {
    	RegistryObjectListType ol = rimFac.createRegistryObjectListType();
    	
    	if (ebl != null)
    		ol.getIdentifiable().addAll(ebl);
    	
    	return ol;
    }

    public JAXBElement<RegistryObjectType> createRegistryObject(RegistryObjectType r) {
    	JAXBElement<RegistryObjectType> eb = rimFac.createRegistryObject(r);
    	return eb;
    }

    public RegistryObjectType createRegistryObjectType() {
    	return createRegistryObjectType((InternationalStringType)null, (InternationalStringType)null);
    }

    public RegistryObjectType createRegistryObjectType(String name) {
    	return createRegistryObjectType(name, null);
    }
    
    public RegistryObjectType createRegistryObjectType(String name, String desc) {
    	InternationalStringType n = null;
    	InternationalStringType d = null;
    	
    	if (name != null)
    		n = createInternationalStringType(name);
    	if (desc != null)
    		d = createInternationalStringType(desc);
    	
    	return createRegistryObjectType(n, d);
    }
   
    public RegistryObjectType createRegistryObjectType(InternationalStringType name, InternationalStringType desc) {
    	RegistryObjectType ro = rimFac.createRegistryObjectType();
    	ro.setId(this.createUUID());
    	ro.setLid(ro.getId());
    	if (name != null)
    		ro.setName(name);
    	if (desc != null)
    		ro.setDescription(desc);
    	ro.setObjectType(CANONICAL_OBJECT_TYPE_ID_RegistryObject);
    	return ro;
    }

	@Override
	public RegistryPackage createRegistryPackage(String arg0)
			throws JAXRException {
		return lcm.createRegistryPackage(arg0);
	}

	@Override
	public RegistryPackage createRegistryPackage(InternationalString arg0)
			throws JAXRException {
		return lcm.createRegistryPackage(arg0);
	}

    public JAXBElement<RegistryPackageType> createRegistryPackage(RegistryPackageType r) {
    	JAXBElement<RegistryPackageType> eb = rimFac.createRegistryPackage(r);
    	return eb;
    }
    
    public RegistryPackageType createRegistryPackageType(String name) {
		return createRegistryPackageType(createInternationalStringType(name));
	}

	public RegistryPackageType createRegistryPackageType(InternationalStringType name) {
		RegistryPackageType rp = rimFac.createRegistryPackageType();
		rp.setId(this.createUUID());
		rp.setLid(rp.getId());
		rp.setName(name);
		rp.setObjectType(CANONICAL_OBJECT_TYPE_ID_RegistryPackage);
		return rp;
	}
	
    public JAXBElement<RegistryType> createRegistry(RegistryType r) {
    	JAXBElement<RegistryType> eb = rimFac.createRegistry(r);
    	return eb;
    }

    public RegistryType createRegistryType(String name) {
    	return createRegistryType(name, null);
    }
    
    public RegistryType createRegistryType(String name, String description) {
    	RegistryType r = rimFac.createRegistryType();
    	r.setId(this.createUUID());
    	r.setLid(r.getId());
    	r.setName(createInternationalStringType(name));
    	if (description != null)
    		r.setDescription(createInternationalStringType(description));
    	r.setObjectType(CANONICAL_OBJECT_TYPE_ID_Registry);
    	return r;
    }

	@Override
	public Service createService(String arg0) throws JAXRException {
		return lcm.createService(arg0);
	}

	@Override
	public Service createService(InternationalString arg0) throws JAXRException {
		return lcm.createService(arg0);
	}

    public JAXBElement<ServiceType> createService(ServiceType s) {
    	JAXBElement<ServiceType> eb = rimFac.createService(s);
    	return eb;
    }
    
	public ServiceType createServiceType(String name) {
		return createServiceType(createInternationalStringType(name));
	}
	
	public ServiceType createServiceType(InternationalStringType name) {
		ServiceType st = rimFac.createServiceType();
		st.setId(this.createUUID());
		st.setLid(st.getId());
		st.setName(name);
		st.setObjectType(CANONICAL_OBJECT_TYPE_ID_Service);
		return st;
	}
	
	public RegistryResponseType addService(OrganizationType o, ServiceType s) throws JAebXRException {
		ClassificationNodeType assocType = (JAebXRClient.getInstance().getBusinessQueryManager()).findClassificationNodeByPath("/" + CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
                CANONICAL_ASSOCIATION_TYPE_CODE_OffersService);
		
		AssociationType1 ass = this.createAssociationType(o, s, assocType);
		
		Collection<RegistryObjectType> c = new ArrayList<RegistryObjectType>();
		c.add(s);
		c.add(ass);
		
		return saveObjectTypes(c);
	}
	
	@Override
	public ServiceBinding createServiceBinding() throws JAXRException {
		return lcm.createServiceBinding();
	}

	public ServiceBindingType createServiceBindingType() {
		ServiceBindingType sb = rimFac.createServiceBindingType();
		sb.setId(this.createUUID());
		sb.setLid(sb.getId());
		sb.setObjectType(CANONICAL_OBJECT_TYPE_ID_ServiceBinding);
		return sb;
	}
	
	@Override
	public Slot createSlot(String arg0, String arg1, String arg2)
			throws JAXRException {
		return lcm.createSlot(arg0, arg1, arg2);
	}

	@Override
	public Slot createSlot(String arg0, @SuppressWarnings("rawtypes") Collection arg1, String arg2)
			throws JAXRException {
		return lcm.createSlot(arg0, arg1, arg2);
	}

	public SlotType1 createSlotType1(String name, String value, String slotType) {
		ArrayList<String> al = new ArrayList<String>();
		al.add(value);
		return createSlotType1(name, al, slotType);
	}
	
	public SlotType1 createSlotType1(String name, Collection<String> values, String slotType) {
		ValueListType vl = rimFac.createValueListType();
		
		Iterator<String> is = values.iterator();
		while (is.hasNext()) {
			vl.getValue().add(is.next());
		}
		
		return createSlotType1(name, vl, slotType);
	}

	public SlotType1 createSlotType1(String name, ValueListType values, String slotType) {
		SlotType1 s = rimFac.createSlotType1();
		s.setName(name);
		s.setSlotType(slotType);
		s.setValueList(values);
		return s;
	}

	@Override
	public SpecificationLink createSpecificationLink() throws JAXRException {
		return lcm.createSpecificationLink();
	}

	public SpecificationLinkType createSpecificationLinkType() {
		SpecificationLinkType sl = rimFac.createSpecificationLinkType();
		sl.setId(this.createUUID());
		sl.setLid(sl.getId());
		sl.setObjectType(CANONICAL_OBJECT_TYPE_ID_SpecificationLink);
		return sl;
	}
	
	@Override
	public TelephoneNumber createTelephoneNumber() throws JAXRException {
		return lcm.createTelephoneNumber();
	}
	
	public TelephoneNumberType createTelephoneNumberType() {
		return rimFac.createTelephoneNumberType();
	}

	@Override
	public User createUser() throws JAXRException {
		return lcm.createUser();
	}

	public UserType createUserType() {
		UserType u = rimFac.createUserType();
		u.setId(this.createUUID());
		u.setLid(u.getId());
		u.setObjectType(CANONICAL_OBJECT_TYPE_ID_User);
		return u;
	}

	public RegistryResponseType addUser(OrganizationType o, UserType u) throws JAebXRException {
		ClassificationNodeType assocType = (JAebXRClient.getInstance().getBusinessQueryManager()).findClassificationNodeByPath("/" + CANONICAL_CLASSIFICATION_SCHEME_LID_AssociationType + "/" +
				CANONICAL_ASSOCIATION_TYPE_CODE_AffiliatedWith);
		
		AssociationType1 ass = this.createAssociationType(u, o, assocType);
		
		Collection<RegistryObjectType> c = new ArrayList<RegistryObjectType>();
		c.add(u);
		c.add(ass);
		
		return saveObjectTypes(c);
	}

    public RemoveObjectsRequest createRemoveObjectsRequest(String id) {
    	Collection <Object> ids = new ArrayList<Object>();
    	ids.add(id);
    	return createRemoveObjectsRequest(ids);
    }
    
    public RemoveObjectsRequest createRemoveObjectsRequest(Collection<?> ids) {
    	return createRemoveObjectsRequest(ids, CANONICAL_DELETION_SCOPE_TYPE_ID_DeleteAll);
    }
    
    public RemoveObjectsRequest createRemoveObjectsRequest(Collection<?> ids, String deletetionScope) {
    	RemoveObjectsRequest req = lcmFac.createRemoveObjectsRequest();    	
    	req.setDeletionScope(deletetionScope);
    	
    	ObjectRefListType orl = rimFac.createObjectRefListType();
    	orl.getObjectRef().addAll(createObjectRefList(ids));
    	
    	req.setObjectRefList(orl);
    	
    	return req;
    }

    /*
    public RemoveObjectsRequest createRemoveObjectsRequest(String id) {
    	Collection <String> ids = new ArrayList<String>();
    	ids.add(id);
    	return createRemoveObjectsRequest(ids);
    }

    public RemoveObjectsRequest createRemoveObjectsRequest(Collection<String> ids) {
    	return createRemoveObjectsRequest(ids, CANONICAL_DELETION_SCOPE_TYPE_ID_DeleteAll);
    }

    public RemoveObjectsRequest createRemoveObjectsRequest(Collection<String> ids, String deletetionScope) {
    	RemoveObjectsRequest req = lcmFac.createRemoveObjectsRequest();    	
    	req.setDeletionScope(deletetionScope);
    	
    	ObjectRefListType orl = rimFac.createObjectRefListType();
    	orl.getObjectRef().addAll(createObjectRefList(ids));
    	
    	req.setObjectRefList(orl);
    	
    	return req;
    }
    */
    
    public SubmitObjectsRequest createSubmitObjectsRequest(JAXBElement<? extends IdentifiableType> eb) {
    	Collection<JAXBElement<? extends IdentifiableType>> ebl = new ArrayList<JAXBElement<? extends IdentifiableType>>();
    	ebl.add(eb);
    	return createSubmitObjectsRequest(ebl);
    }

    public SubmitObjectsRequest createSubmitObjectsRequest(Collection <JAXBElement<? extends IdentifiableType>> ebl) {
    	SubmitObjectsRequest req = lcmFac.createSubmitObjectsRequest();
    	
    	RegistryObjectListType ol = createRegistryObjectListType(ebl);   	
    	req.setRegistryObjectList(ol);
    	
    	return req;
    }
    
    public UpdateObjectsRequest createUpdateObjectsRequest(JAXBElement<? extends IdentifiableType> eb) {
    	Collection<JAXBElement<? extends IdentifiableType>> ebl = new ArrayList<JAXBElement<? extends IdentifiableType>>();
    	ebl.add(eb);
    	return createUpdateObjectsRequest(ebl);
    }
    
    public UpdateObjectsRequest createUpdateObjectsRequest(Collection <JAXBElement<? extends IdentifiableType>> ebl) {
    	UpdateObjectsRequest req = lcmFac.createUpdateObjectsRequest();
    	
    	RegistryObjectListType ol = createRegistryObjectListType(ebl);    	
    	req.setRegistryObjectList(ol);
    	
    	return req;
    }
    
    /**
     * Create a valid registry id.
     */
    public String createUUID() {
        String id = "urn:uuid:" + UUIDFactory.getInstance().newUUID().toString();
        return id;
    }
    
	@Override
	public BulkResponse deleteObjects(@SuppressWarnings("rawtypes") Collection arg0) throws JAXRException {
		return lcm.deleteObjects(arg0);
	}

	@Override
	public BulkResponse deleteObjects(@SuppressWarnings("rawtypes") Collection arg0, String arg1)
			throws JAXRException {
		return lcm.deleteObjects(arg0, arg1);
	}

	@Override
	public BulkResponse deprecateObjects(@SuppressWarnings("rawtypes") Collection arg0) throws JAXRException {
		return lcm.deprecateObjects(arg0);
	}

	@Override
	public RegistryService getRegistryService() throws JAXRException {
		return rs;
	}
    
    public AssociationType1 joinFederation(String fedId, String regId) throws JAebXRException {
    	AssociationType1 a = createAssociationType(fedId, regId, CANONICAL_ASSOCIATION_TYPE_CODE_HasFederationMember);  	
    	saveObjectType(createAssociation(a));
    	return a;
    }
    
    @Override
	public BulkResponse saveObjects(@SuppressWarnings("rawtypes") Collection arg0) throws JAXRException {
		return lcm.saveObjects(arg0);
	}

	@Override
	public BulkResponse unDeprecateObjects(@SuppressWarnings("rawtypes") Collection arg0)
			throws JAXRException {
		return lcm.unDeprecateObjects(arg0);
	}

    /**
     * This method is used to add special rim:Slot to the RegistryRequestType
     * object to indicate that the server should create a secure session.
     *
     * @param req
     *   The RegistryRequestType to which the rim:Slot will be added
     */
    private void addCreateSessionSlot(RegistryRequestType req) throws JAXBException {
        HashMap<String, String> slotMap = new HashMap<String, String>();
        slotMap.put("urn:javax:xml:registry:connection:createHttpSession", "true");
        bu.addSlotsToRequest(req, slotMap);
    }

    public RegistryResponseType saveObjectType(AssociationType1 a) throws JAebXRException {
    	JAXBElement<AssociationType1> eb = createAssociation(a);
    	return checkResponseAndSaveToCache(saveObjectType(eb), a);
    }

    public RegistryResponseType saveObjectType(ClassificationNodeType cs) throws JAebXRException {
    	JAXBElement<ClassificationNodeType> eb = createClassificationNode(cs);
    	return checkResponseAndSaveToCache(saveObjectType(eb), cs);
    }
    
    public RegistryResponseType saveObjectType(ClassificationSchemeType cs) throws JAebXRException {
    	JAXBElement<ClassificationSchemeType> eb = createClassificationScheme(cs);
    	return checkResponseAndSaveToCache(saveObjectType(eb), cs);
    }
    
    public RegistryResponseType saveObjectType(ClassificationType c) throws JAebXRException {
    	JAXBElement<ClassificationType> eb = createClassification(c);
    	return checkResponseAndSaveToCache(saveObjectType(eb), c);
    }

    public RegistryResponseType saveObjectType(ExternalLinkType cn) throws JAebXRException {
    	JAXBElement<ExternalLinkType> eb = rimFac.createExternalLink(cn);
    	return checkResponseAndSaveToCache(saveObjectType(eb), cn);
    }
  
    public RegistryResponseType saveObjectType(RegistryType cn) throws JAebXRException {
    	JAXBElement<RegistryType> eb = createRegistry(cn);
    	return checkResponseAndSaveToCache(saveObjectType(eb), cn);
    }
  
    public RegistryResponseType saveObjectType(ExtrinsicObjectType eo) throws JAebXRException {
    	JAXBElement<ExtrinsicObjectType> eb = createExtrinsicObject(eo);
    	Map<String, DataHandler> m = new HashMap<String, DataHandler>();
    	String urlString = null;
    	List<SlotType1> sl = eo.getSlot();
    	if (sl != null) {
	    	Iterator<SlotType1> i = sl.iterator();
	    	while (i.hasNext()) {
	    		SlotType1 s = (SlotType1) i.next();
	    		if (s.getName().equals("URL")) {
	    			urlString = s.getValueList().getValue().get(0);
	    			//System.err.println("Attach: " + urlString);
	    			break;
	    		}
	    	}
	    	if (urlString != null)
				try {
					m.put(eo.getId(), new DataHandler(new URL(urlString)));
				} catch (MalformedURLException e) {
					throw new JAebXRException(e);
				}
    	}
    	return checkResponseAndSaveToCache(saveObjectType(eb, m), eo);
    }
    
    public RegistryResponseType saveObjectType(OrganizationType o) throws JAebXRException {
    	JAXBElement<OrganizationType> eb = createOrganization(o);
    	return checkResponseAndSaveToCache(saveObjectType(eb), o);
    }

    public RegistryResponseType saveObjectType(PersonType o) throws JAebXRException {
    	JAXBElement<PersonType> eb = createPerson(o);
    	return checkResponseAndSaveToCache(saveObjectType(eb), o);
    }
    
    public RegistryResponseType saveObjectType(RegistryPackageType cn) throws JAebXRException {
    	JAXBElement<RegistryPackageType> eb = createRegistryPackage(cn);
    	return checkResponseAndSaveToCache(saveObjectType(eb), cn);
    }
    
    public RegistryResponseType saveObjectType(ServiceType s) throws JAebXRException {
    	JAXBElement<ServiceType> eb = createService(s);
    	return checkResponseAndSaveToCache(saveObjectType(eb), s);
    }

    public RegistryResponseType saveFederations(Collection<FederationType> f) throws JAebXRException {
    	Collection <JAXBElement<? extends IdentifiableType>> list = new ArrayList<JAXBElement<? extends IdentifiableType>>();
    	Iterator<FederationType> i = f.iterator();
    	while (i.hasNext()) {
    		list.add(this.createFederation((FederationType) i.next()));
    	};
    	SubmitObjectsRequest sreq = createSubmitObjectsRequest(list);
    	RegistryResponseType resp = saveObjectTypes(sreq);
    	return resp;    	
    }
    
    public RegistryResponseType saveObjectType(JAXBElement<? extends IdentifiableType> eb) throws JAebXRException {
    	return saveObjectType(eb, null);
    }
    
    public RegistryResponseType saveObjectType(JAXBElement<? extends IdentifiableType> eb, Map<String, DataHandler> attachments) throws JAebXRException {
    	SubmitObjectsRequest sreq = createSubmitObjectsRequest(eb);
    	RegistryResponseType resp = saveObjectTypes(sreq, attachments);
    	return resp;
    }

    public RegistryResponseType updateObjectType(RegistryObjectType o) throws JAebXRException {
    	o.setId(this.createUUID());
    	cache.remove(o.getId());
    	
    	if (o.getClassification() != null) {
    		List<ClassificationType> c = o.getClassification();
    		Iterator<ClassificationType> i = c.iterator();
    		while (i.hasNext())
    			(i.next()).setClassifiedObject(o.getId());
    	}
    	
    	cache.put(o.getId(), o);

    	JAXBElement<? extends IdentifiableType> eb = null;
    	
		if (o instanceof AssociationType1)
			eb = createAssociation((AssociationType1) o);
		else if (o instanceof ClassificationNodeType)
			eb = createClassificationNode((ClassificationNodeType) o);
		else if (o instanceof ClassificationSchemeType)
			eb = createClassificationScheme((ClassificationSchemeType) o);
		else if (o instanceof ClassificationType)
			eb = createClassification((ClassificationType) o);
		else if (o instanceof ExtrinsicObjectType)
			eb = createExtrinsicObject((ExtrinsicObjectType) o);
		else if (o instanceof FederationType)
			eb = createFederation((FederationType) o);
		else if (o instanceof OrganizationType)
			eb = createOrganization((OrganizationType) o);
		else if (o instanceof PersonType)
			eb = createPerson((PersonType) o);
		else if (o instanceof RegistryPackageType)
			eb = createRegistryPackage((RegistryPackageType) o);
		else if (o instanceof ServiceType)
			eb = createService((ServiceType) o);
		else
			throw new JAebXRException("Object not yet supported: " + o.getClass().getName());
		
    	//UpdateObjectsRequest sreq = createUpdateObjectsRequest(eb);
    	//RegistryResponseType resp = updateObjectTypes(sreq);
    	return saveObjectType(eb);
    }
    
    public RegistryResponseType updateObjectType(JAXBElement<? extends IdentifiableType> eb) throws JAebXRException {
    	UpdateObjectsRequest sreq = createUpdateObjectsRequest(eb);
    	RegistryResponseType resp = updateObjectTypes(sreq);
    	return resp;
    }
    
    public RegistryResponseType saveObjectTypes(Collection<? extends IdentifiableType> c) throws JAebXRException {
    	if (c == null)
    		return handleNullParam();
    	
    	Collection <JAXBElement<? extends IdentifiableType>> list = new ArrayList<JAXBElement<? extends IdentifiableType>>();
    	
    	Iterator<? extends IdentifiableType> i = c.iterator();
    	while (i.hasNext()) {
    		Object o = i.next();
    		JAXBElement<? extends IdentifiableType> eb = null;
    		
    		if (o instanceof AssociationType1)
    			eb = createAssociation((AssociationType1) o);
    		else if (o instanceof ClassificationNodeType)
    			eb = createClassificationNode((ClassificationNodeType) o);
    		else if (o instanceof ClassificationSchemeType)
    			eb = createClassificationScheme((ClassificationSchemeType) o);
    		else if (o instanceof ClassificationType)
    			eb = createClassification((ClassificationType) o);
    		else if (o instanceof ExtrinsicObjectType)
    			eb = createExtrinsicObject((ExtrinsicObjectType) o);
    		else if (o instanceof FederationType)
    			eb = createFederation((FederationType) o);
    		else if (o instanceof OrganizationType)
    			eb = createOrganization((OrganizationType) o);
    		else if (o instanceof PersonType)
    			eb = createPerson((PersonType) o);
    		else if (o instanceof RegistryPackageType)
    			eb = createRegistryPackage((RegistryPackageType) o);
    		else if (o instanceof ServiceType)
    			eb = createService((ServiceType) o);
    		else
    			throw new JAebXRException("Object not yet supported: " + o.getClass().getName());
    		    		
    		list.add(eb);
    		
    		RegistryObjectType ro = (RegistryObjectType)o;
    		cache.put(ro.getId(), ro);
    	};
    	SubmitObjectsRequest sreq = createSubmitObjectsRequest(list);
    	RegistryResponseType resp = saveObjectTypes(sreq);
    	return resp;    	
    }
    
    public RegistryResponseType deleteObjectType(String id) throws JAebXRException {
    	RemoveObjectsRequest sreq = createRemoveObjectsRequest(id);
    	return checkResponseAndRemoveFromCache(deleteObjectTypes(sreq), id);
    }

    public RegistryResponseType deleteObjectTypes(Collection<?> ass) throws JAebXRException {
    	if (ass == null)
    		return handleNullParam();
    	
    	RemoveObjectsRequest sreq = createRemoveObjectsRequest(ass);
    	return checkResponseAndRemoveFromCache(deleteObjectTypes(sreq), ass);
    }
    
    public RegistryResponseType updateObjectTypes(UpdateObjectsRequest req) throws JAebXRException {
    	return submitObjectTypes(req, null);
    }
    
    public RegistryResponseType saveObjectTypes(SubmitObjectsRequest req) throws JAebXRException {
    	return saveObjectTypes(req, null);
    }
    
    public RegistryResponseType saveObjectTypes(SubmitObjectsRequest req, Map<String, DataHandler> attachments) throws JAebXRException {
    	return submitObjectTypes(req, attachments);
    }
    
    public RegistryResponseType deleteObjectTypes(RemoveObjectsRequest req) throws JAebXRException {
    	return submitObjectTypes(req);
    }
    
    protected RegistryResponseType submitObjectTypes(RegistryRequestType req) throws JAebXRException {
    	return submitObjectTypes(req, null);
    }
    
    private RegistryResponseType submitObjectTypes(RegistryRequestType req, Map<String, DataHandler> attachments) throws JAebXRException {
        try {
        	addCreateSessionSlot(req);

        	StringWriter sw = new StringWriter();
            Marshaller marshaller = bu.getJAXBContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(req, sw);
            
            String requestString = sw.toString();
            RegistryResponseHolder resp = msgr.sendSoapRequest(requestString, attachments);
            
            return resp.getRegistryResponseType();
        }
        catch (JAXBException e) {
            throw new JAebXRException(e);
        }
        catch (RegistryException e) {
            throw new JAebXRException(e);
        }        
        catch (JAXRException e) {
            throw new JAebXRException(e);
        }
    }
    
    // TODO
    public RegistryResponseType deprecateObjectTypes(Collection<String> c) throws JAebXRException {
    	if (c == null)
    		return handleNullParam();
    	
    	throw new JAebXRException("Not yet implemented!");   	
    }
    
    // TODO
    public RegistryResponseType unDeprecateObjectTypes(Collection<String> c) throws JAebXRException {
    	if (c == null)
    		return handleNullParam();
    	
    	throw new JAebXRException("Not yet implemented!");   	
    }
    
    protected RegistryResponseType handleNullParam() {
		RegistryResponseType res = rsFac.createRegistryResponseType();
		res.setStatus(CanonicalConstants.CANONICAL_RESPONSE_STATUS_TYPE_LID_Failure);
		return res;
	}

	
	public OrganizationType getProvidingOrganization(ServiceType s) throws JAebXRException {
		OrganizationType org = null;
		
		SlotType1 providingOrgSlot = getSlot(s, "providingOrganization");
        if (providingOrgSlot != null) {
            List<String> providingOrgValues = providingOrgSlot.getValueList().getValue();
            Iterator<String> providingOrgIter = providingOrgValues.iterator();
            org = (OrganizationType) (JAebXRClient.getInstance().getBusinessQueryManager().getRegistryObjectType(providingOrgIter.next()));
        }

		return org;
	}
	
	public ServiceType setProvidingOrganization(ServiceType s, OrganizationType o) {
		SlotType1 providingOrgSlot = getSlot(s, "providingOrganization");
		if (providingOrgSlot != null)
			s.getSlot().remove(providingOrgSlot);
		if (o != null) {
			providingOrgSlot = this.createSlotType1("providingOrganization", o.getId(), null);
			s.getSlot().add(providingOrgSlot);
		}
		return s;
	}
	
    public SlotType1 getSlot(RegistryObjectType ro, String name) {
    	if (ro.getSlot() != null) {
        	Iterator<SlotType1> i = ro.getSlot().iterator();
        	while (i.hasNext()) {
            	SlotType1 s = i.next();
            	if (s.getName().equals(name))
            		return s;
        	}
    	}
    	return null;
    }
    
    public URL getUrl(UserType u) throws JAebXRException {
        URL url = null;
        SlotType1 urlSlot = getSlot(u, IMPL_SLOT_PERSON_URL);
        if (urlSlot != null) {
            String urlStr = (String)(urlSlot.getValueList().getValue().toArray())[0];
            try {
                url = new URL(urlStr);
            } catch (MalformedURLException e) {
                throw new JAebXRException(e);
            }
        }
        return url;
    }

    public UserType setUrl(UserType u, URL url) {
        SlotType1 urlSlot = createSlotType1(IMPL_SLOT_PERSON_URL, url.toString(), CANONICAL_DATA_TYPE_LID_String);
        u.getSlot().add(urlSlot);
        return u;
    }
}
