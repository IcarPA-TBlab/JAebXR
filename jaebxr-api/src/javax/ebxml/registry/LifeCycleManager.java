package javax.ebxml.registry;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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
import javax.xml.registry.RegistryService;
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

import org.oasis.ebxml.registry.bindings.lcm.RemoveObjectsRequest;
import org.oasis.ebxml.registry.bindings.lcm.SubmitObjectsRequest;
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
	private javax.xml.registry.RegistryService rs = null;
	
	private SOAPMessenger msgr = null;
	
	private static BindingUtility bu = BindingUtility.getInstance();
	
    private org.oasis.ebxml.registry.bindings.rim.ObjectFactory rimFac;
    //private org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFac;
    private org.oasis.ebxml.registry.bindings.lcm.ObjectFactory lcmFac;
    //private org.oasis.ebxml.registry.bindings.query.ObjectFactory queryFac;
    //private org.oasis.ebxml.registry.bindings.cms.ObjectFactory cmsFac;

	public LifeCycleManager() {
        rimFac = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
        //rsFac = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();
        lcmFac = new org.oasis.ebxml.registry.bindings.lcm.ObjectFactory();
        //queryFac = new org.oasis.ebxml.registry.bindings.query.ObjectFactory();
        //cmsFac = new org.oasis.ebxml.registry.bindings.cms.ObjectFactory();		
	}
	
	protected void setSOAPMessenger(SOAPMessenger msgr) {
		this.msgr = msgr;
	}
	
    protected void setLifeCycleManager(javax.xml.registry.LifeCycleManager lcm) {
    	this.lcm = lcm;
    }
    
    protected void setRegistryService(javax.xml.registry.RegistryService rs) {
    	this.rs = rs;
    }

    @Override
	public Association createAssociation(RegistryObject arg0, Concept arg1)
			throws JAXRException {
		return lcm.createAssociation(arg0, arg1);
	}

    public AssociationType1 createAssociationType(String srcId, String desId, String type) {
    	return createAssociationType(srcId, desId, null, null, type);
    }
    
    public AssociationType1 createAssociationType(String srcId, String desId, String name, String description, String type) {
    	AssociationType1 a = rimFac.createAssociationType1();
    	a.setSourceObject(srcId);
    	a.setTargetObject(desId);
    	if (name != null)
    		a.setName(createInternationalStringType(name));
    	if (description != null)
    		a.setDescription(createInternationalStringType(description));
    	a.setAssociationType(type);
    	return a;
    }
    
    public ClassificationType createClassification() {
    	ClassificationType c = rimFac.createClassificationType();
    	
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

	public ClassificationType createClassificationType(ClassificationNodeType cn) {
		ClassificationType c = rimFac.createClassificationType();
		return c;
	}
	
	public ClassificationType createClassificationType(ClassificationSchemeType scheme, String name, String value) {
		return createClassificationType(scheme, createInternationalStringType(name), value);
	}

	public ClassificationType createClassificationType(ClassificationSchemeType scheme, InternationalStringType name, String value) {
		ClassificationType c = rimFac.createClassificationType();
		c.setClassificationScheme(scheme.getId());
		c.setName(name);
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
		cs.setName(cn.getName());
		cs.setDescription(cn.getDescription());
		cs.getClassification().addAll(cn.getClassification());
		cs.getExternalIdentifier().addAll(cn.getExternalIdentifier());
		cs.setNodeType(CANONICAL_NODE_TYPE_ID_UniqueCode);
		cs.setIsInternal(false);
		return cs;
	}
	
	public ClassificationSchemeType createClassificationSchemeType(String name, String description) {
		return createClassificationSchemeType(createInternationalStringType(name), createInternationalStringType(description));
	}

	public ClassificationSchemeType createClassificationSchemeType(InternationalStringType name, InternationalStringType description) {
		ClassificationSchemeType cs = rimFac.createClassificationSchemeType();
		cs.setId(this.createUUID());
		cs.setName(name);
		cs.setDescription(description);
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

	public ClassificationNodeType createConceptType(RegistryObjectType parent, String name, String value) {
		return createClassificationNodeType(parent, createInternationalStringType(name), value);
	}

	public ClassificationNodeType createConceptType(RegistryObjectType parent, InternationalStringType name, String value) {
		return createClassificationNodeType(parent, name, value);
	}
	
	public ClassificationNodeType createClassificationNodeType(RegistryObjectType parent, String name, String value) {
		return createClassificationNodeType(parent, createInternationalStringType(name), value);
	}

	public ClassificationNodeType createClassificationNodeType(RegistryObjectType parent, InternationalStringType name, String value) {
		ClassificationNodeType cn = rimFac.createClassificationNodeType();
		cn.setId(this.createUUID());
		cn.setParent(parent.getId());
		cn.setName(name);
		cn.setCode(value);
		cn.setDescription(name);
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
		EmailAddressType ea = rimFac.createEmailAddressType();
		ea.setAddress(address);
		return ea;
	}

	public EmailAddressType createEmailAddressType(String address, String type) throws JAXRException {
		EmailAddressType ea = rimFac.createEmailAddressType();
		ea.setAddress(address);
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
		ei.setIdentificationScheme(scheme.getId());
		ei.setName(name);
		ei.setValue(value);
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

	public ExternalLinkType createExternalLinkType(String externalURI, String description) {
		return createExternalLinkType(externalURI, createInternationalStringType(description));
	}

	public ExternalLinkType createExternalLinkType(String externalURI, InternationalStringType description) {
		ExternalLinkType el = rimFac.createExternalLinkType();
		el.setExternalURI(externalURI);
		el.setDescription(description);
		return el;
	}
	
	@Override
	public ExtrinsicObject createExtrinsicObject(DataHandler arg0)
			throws JAXRException {
		return lcm.createExtrinsicObject(arg0);
	}

	public ExtrinsicObjectType createExtrinsicObjectType() {
		return createExtrinsicObjectType((String)null);
	}
	
	public ExtrinsicObjectType createExtrinsicObjectType(String id) {
		ExtrinsicObjectType eo = rimFac.createExtrinsicObjectType();
		if (id != null) {
			eo.setObjectType(id);
		}
		return eo;
	}

	public ExtrinsicObjectType createExtrinsicObjectType(RegistryObjectType ro) {
		ExtrinsicObjectType eo = rimFac.createExtrinsicObjectType();
		if ((ro != null)  && (ro.getId() != null)) {
			eo.setObjectType(ro.getId());
		}
		return eo;
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
    	f.setName(createInternationalStringType(name));
    	if (description != null) f.setDescription(createInternationalStringType(description));
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

	private List<ObjectRefType> createObjectRefList(Collection<String> keys) {
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
	
	@Override
	public Organization createOrganization(String arg0) throws JAXRException {
		return lcm.createOrganization(arg0);
	}

	@Override
	public Organization createOrganization(InternationalString arg0)
			throws JAXRException {
		return lcm.createOrganization(arg0);
	}

	public OrganizationType createOrganizationType(String name) {
		return createOrganizationType(createInternationalStringType(name));
	}

	public OrganizationType createOrganizationType(InternationalStringType name) {
		OrganizationType o = rimFac.createOrganizationType();
		o.setName(name);
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

	public RegistryPackageType createRegistryPackageType(String name) {
		return createRegistryPackageType(createInternationalStringType(name));
	}

	public RegistryPackageType createRegistryPackageType(InternationalStringType name) {
		RegistryPackageType rp = rimFac.createRegistryPackageType();
		rp.setName(name);
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
    	r.setName(createInternationalStringType(name));
    	if (description != null)
    		r.setDescription(createInternationalStringType(description));
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

	public ServiceType createServiceType(String name) {
		return createServiceType(createInternationalStringType(name));
	}
	
	public ServiceType createServiceType(InternationalStringType name) {
		ServiceType st = rimFac.createServiceType();
		st.setName(name);
		return st;
	}
	
	@Override
	public ServiceBinding createServiceBinding() throws JAXRException {
		return lcm.createServiceBinding();
	}

	public ServiceBindingType createServiceBindingType() {
		return rimFac.createServiceBindingType();
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
		return rimFac.createSpecificationLinkType();
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
		return rimFac.createUserType();
	}

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
    
    public AssociationType1 joinFederation(String fedId, String regId) {
    	return createAssociationType(fedId, regId, CANONICAL_ASSOCIATION_TYPE_CODE_HasFederationMember);
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

    public RegistryResponseType saveObject(JAXBElement<? extends IdentifiableType> eb) throws RegistryException {
    	SubmitObjectsRequest sreq = createSubmitObjectsRequest(eb);
    	RegistryResponseType resp = saveObjects(sreq);
    	return resp;
    }

    public RegistryResponseType saveObject(Collection <JAXBElement<? extends IdentifiableType>> ebl) throws RegistryException {
    	SubmitObjectsRequest sreq = createSubmitObjectsRequest(ebl);
    	RegistryResponseType resp = saveObjects(sreq);
    	return resp;
    }
    
    public RegistryResponseType saveObjects(SubmitObjectsRequest req) throws RegistryException {
    	return submitObjects(req);
    }
    
    public RegistryResponseType deleteObjects(RemoveObjectsRequest req) throws RegistryException {
    	return submitObjects(req);
    }
    
    private RegistryResponseType submitObjects(RegistryRequestType req) throws RegistryException {
        try {
        	addCreateSessionSlot(req);

        	StringWriter sw = new StringWriter();
            Marshaller marshaller = bu.getJAXBContext().createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(req, sw);
            
            String requestString = sw.toString();
            RegistryResponseHolder resp = msgr.sendSoapRequest(requestString);
            
            return resp.getRegistryResponseType();
        }
        catch (JAXBException e) {
            throw new RegistryException(e);
        }
        catch (RegistryException e) {
            throw e;
        }        
        catch (JAXRException e) {
            throw new RegistryException(e);
        }
    }

}
