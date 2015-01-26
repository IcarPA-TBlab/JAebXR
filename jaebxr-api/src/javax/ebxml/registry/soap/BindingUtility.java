package javax.ebxml.registry.soap;

import java.io.StringWriter;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ebxml.registry.CanonicalConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.registry.JAXRException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.transform.dom.DOMResult;

import org.oasis.ebxml.registry.bindings.rim.ObjectRefListType;
import org.oasis.ebxml.registry.bindings.rim.ObjectRefType;
import org.oasis.ebxml.registry.bindings.rim.SlotListType;
import org.oasis.ebxml.registry.bindings.rim.SlotType1;
import org.oasis.ebxml.registry.bindings.rim.ValueListType;
import org.oasis.ebxml.registry.bindings.rs.RegistryRequestType;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

public class BindingUtility {

    private static BindingUtility instance = null;
    
    public org.oasis.ebxml.registry.bindings.rim.ObjectFactory rimFac;
    public org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFac;
    public org.oasis.ebxml.registry.bindings.lcm.ObjectFactory lcmFac;
    public org.oasis.ebxml.registry.bindings.query.ObjectFactory queryFac;
    public org.oasis.ebxml.registry.bindings.cms.ObjectFactory cmsFac;
    
    JAXBContext jaxbContext = null;

    /**
     * Class Constructor. Protected and only used by getInstance()
     *
     */
    protected BindingUtility() {
        try {
            getJAXBContext();
            rimFac = new org.oasis.ebxml.registry.bindings.rim.ObjectFactory();
            rsFac = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();
            lcmFac = new org.oasis.ebxml.registry.bindings.lcm.ObjectFactory();
            queryFac = new org.oasis.ebxml.registry.bindings.query.ObjectFactory();
            cmsFac = new org.oasis.ebxml.registry.bindings.cms.ObjectFactory();
            //samlProtocolFac = new org.oasis.saml.bindings._20.protocol.ObjectFactory();
            //samlAssertionFac = new org.oasis.saml.bindings._20.assertion.ObjectFactory();
        } catch (JAXBException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Gets the singleton instance as defined by Singleton pattern.
     *
     * @return the singleton instance
     *
     */
    public synchronized static BindingUtility getInstance() {
        if (instance == null) {
            instance = new BindingUtility();
        }
        return instance;
    }

    
    public JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance(
                    "org.oasis.ebxml.registry.bindings.rim:org.oasis.ebxml.registry.bindings.rs:org.oasis.ebxml.registry.bindings.lcm:org.oasis.ebxml.registry.bindings.query:org.oasis.ebxml.registry.bindings.cms"
                    );
        }
        return jaxbContext;
    }

    public Unmarshaller getUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {
                public boolean handleEvent(ValidationEvent event) {
                    boolean keepOn = false;
                    return keepOn;
                }
            });
        return unmarshaller;
    }

    public SOAPElement getSOAPElementFromBindingObject(Object obj) throws JAXRException {
        SOAPElement soapElem = null;
        try {
            SOAPElement parent = SOAPFactory.newInstance().createElement("dummy");
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal( obj, new DOMResult(parent) );
            soapElem = (SOAPElement)parent.getChildElements().next();
        }
        catch (Exception e) {
            throw new JAXRException(e);
        }
        return soapElem;
    }

    public Object getBindingObjectFromSOAPElement(SOAPElement soapElem) throws JAXRException {
        Object obj = null;
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            obj = unmarshaller.unmarshal(soapElem);
        }
        catch (Exception e) {
            throw new JAXRException(e);
        }
        return obj;
    }

    public void checkRegistryResponse(RegistryResponseType resp) throws JAXRException {
        if (!(resp.getStatus().equals(CanonicalConstants.CANONICAL_RESPONSE_STATUS_TYPE_ID_Success))) {
            StringWriter sw = new StringWriter();
            try {
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.marshal(resp, sw);
                throw new JAXRException(sw.toString());
            }
            catch (Exception e) {
                throw new JAXRException(e);
            }
        }
    }

    public String marshalObject(Object obj) throws JAXBException {
        StringWriter sw = new StringWriter();
        javax.xml.bind.Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(obj, sw);
        String str = sw.toString();
        return str;
    }
 
    public void addSlotsToRequest(RegistryRequestType req, Map<String, String> slotsMap) throws JAXBException {
        SlotListType slotList = req.getRequestSlotList();
        if (slotList == null) {
            slotList = rimFac.createSlotListType();
        }
        Iterator<String> iter = slotsMap.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            Object slotValue = slotsMap.get(key);

            SlotType1 ebSlotType = rimFac.createSlotType1();
            ebSlotType.setName(key.toString());
            ValueListType ebValueListType = rimFac.createValueListType();

            //slotValue must either be a String or a Collection of Strings
            if (slotValue instanceof String) {
                ebValueListType.getValue().add((String) slotValue);

            } else if (slotValue instanceof Collection) {
                @SuppressWarnings("unchecked")
				Collection<String> c = (Collection<String>)slotValue;
                Iterator<String> citer = c.iterator();
                while (citer.hasNext()) {
                    ebValueListType.getValue().add(citer.next());
                }
            } else {
                throw new IllegalArgumentException("message.addingParameter: " + slotValue.getClass().getName());
            }
            ebSlotType.setValueList(ebValueListType);
            slotList.getSlot().add(ebSlotType);
        }

        req.setRequestSlotList(slotList);
    }

    /**
     * Get List of id of ObjectRef under ObjectRefList.
     */
    public List<String> getIdsFromObjectRefList(ObjectRefListType refList) {
        List<String> ids = new ArrayList<String>();

        if (refList != null) {
            List<ObjectRefType> refs = refList.getObjectRef();
            Iterator<ObjectRefType> iter = refs.iterator();

            while (iter.hasNext()) {
                ObjectRefType ref = iter.next();
                ids.add(ref.getId());
            }
        }

        return ids;
    }

}
