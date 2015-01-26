package javax.ebxml.registry.soap;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.ebxml.registry.CanonicalConstants;
import javax.ebxml.registry.RegistryResponseHolder;
import javax.ebxml.registry.security.CredentialInfo;
import javax.ebxml.registry.security.WSS4JSecurityUtilBST;
import javax.mail.MessagingException;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class is responsible for communicating w/ registry using SOAP.
 * 
 */
public class SOAPMessenger {
    
	private String endpoint;
	private CredentialInfo credentialInfo = null;

	public SOAPMessenger(String registryUrl, CredentialInfo credentialInfo) {
		endpoint = registryUrl;
		this.credentialInfo = credentialInfo;
	}

	/**
	 * Send a SOAP request to the registry server. Main entry point for this
	 * class.
	 * 
	 * @param requestString
	 *            String that will be placed in the body of the SOAP message to
	 *            be sent to the server
	 * 
	 * @return RegistryResponseHolder that represents the response from the
	 *         server
	 */
	public RegistryResponseHolder sendSoapRequest(String requestString) throws JAXRException {
		return sendSoapRequest(requestString, null);
	}

	/**
	 * Send a SOAP request to the registry server. Main entry point for this
	 * class. If credentials have been set on the registry connection, they will
	 * be used to sign the request.
	 * 
	 * @param requestString
	 *            String that will be placed in the body of the SOAP message to
	 *            be sent to the server
	 * 
	 * @param attachments
	 *            HashMap consisting of entries each of which corresponds to an
	 *            attachment where the entry key is the ContentId and the entry
	 *            value is a javax.activation.DataHandler of the attachment. A
	 *            parameter value of null means no attachments.
	 * 
	 * @return RegistryResponseHolder that represents the response from the
	 *         server
	 */
	@SuppressWarnings("unchecked")
	public RegistryResponseHolder sendSoapRequest(String requestString, Map<?, ?> attachments) throws JAXRException {

		MessageFactory messageFactory;
		SOAPMessage message = null;
		SOAPPart sp = null;
		SOAPEnvelope se = null;
		SOAPBody sb = null;
		SOAPHeader sh = null;

		try {

			messageFactory = MessageFactory.newInstance();
			message = messageFactory.createMessage();

			sp = message.getSOAPPart();
			se = sp.getEnvelope();
			sb = se.getBody();
			sh = se.getHeader();

			/*
			 * <soap-env:Envelope
			 * xmlns:soap-env="http://schemas.xmlsoap.org/soap/envelope/">
			 * <soap-env:Header> <capabilities
			 * xmlns="urn:freebxml:registry:soap"
			 * >urn:freebxml:registry:soap:modernFaultCodes</capabilities>
			 * 
			 * change with explicit namespace <ns1:capabilities
			 * xmlns:ns1="urn:freebxml:registry:soap"
			 * >urn:freebxml:registry:soap:modernFaultCodes</ns1:capabilities>
			 */
			SOAPHeaderElement capabilityElement = sh.addHeaderElement(se.createName(
					CanonicalConstants.SOAP_CAPABILITY_HEADER_LocalName, "ns1",
					CanonicalConstants.SOAP_CAPABILITY_HEADER_Namespace));
			// capabilityElement.addAttribute(
			// se.createName("xmlns"),
			// CanonicalConstants.SOAP_CAPABILITY_HEADER_Namespace);
			capabilityElement.setTextContent(CanonicalConstants.SOAP_CAPABILITY_ModernFaultCodes);

			/*
			 * body
			 */

			// Remove the XML Declaration, if any
			if (requestString.startsWith("<?xml")) {
				requestString = requestString.substring(requestString.indexOf("?>") + 2).trim();
			}

			// Generate DOM Document from request xml string
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			InputStream stream = new ByteArrayInputStream(requestString.getBytes("UTF-8"));

			// Inject request into body
			sb.addDocument(builderFactory.newDocumentBuilder().parse(stream));

			// Is it never the case that there attachments but no credentials
			if ((attachments != null) && !attachments.isEmpty()) {
				addAttachments(message, attachments);
			}

			if (credentialInfo != null) {

				WSS4JSecurityUtilBST.signSOAPEnvelopeOnClient(se, credentialInfo);

			}

			SOAPMessage response = send(message);

			// Check to see if the session has expired
			// by checking for an error response code
			// TODO: what error code to we look for?
			if (credentialInfo != null) {
				if (isSessionExpired(response)) {
					credentialInfo.sessionId = null;
					// sign the SOAPMessage this time
					// TODO: session - add method to do the signing
					// signSOAPMessage(msg);
					// send signed message
					// SOAPMessage response = send(msg);
				}
			}

			// Process the main SOAPPart of the response
			// check for soapfault and throw RegistryException
			SOAPFault fault = response.getSOAPBody().getFault();
			if (fault != null) {
				throw createRegistryException(fault);
			}

			Reader reader = processResponseBody(response, "Response");

			RegistryResponseType ebResponse = null;

			try {
				Object obj = BindingUtility.getInstance().getJAXBContext().createUnmarshaller()
						.unmarshal(new InputSource(reader));

				if (obj instanceof JAXBElement<?>)
					// if Element: take ComplexType from Element
					obj = ((JAXBElement<RegistryResponseType>) obj).getValue();

				ebResponse = (RegistryResponseType) obj;
			} catch (Exception x) {
				throw new JAXRException("message.invalidServerResponse");
			}

			// Process the attachments of the response if any
			HashMap<String, Object> responseAttachments = processResponseAttachments(response);

			return new RegistryResponseHolder(ebResponse, responseAttachments);

		} catch (SAXException e) {
			throw new JAXRException(e);

		} catch (ParserConfigurationException e) {
			throw new JAXRException(e);

		} catch (UnsupportedEncodingException x) {
			throw new JAXRException(x);

		} catch (MessagingException x) {
			throw new JAXRException(x);

		} catch (FileNotFoundException x) {
			throw new JAXRException(x);

		} catch (IOException e) {
			throw new JAXRException(e);

		} catch (SOAPException x) {
			x.printStackTrace();
			throw new JAXRException("message.cannotConnect", x);

		} catch (TransformerConfigurationException x) {
			throw new JAXRException(x);

		} catch (TransformerException x) {
			throw new JAXRException(x);
		}
	}

	@SuppressWarnings("rawtypes")
	private void addAttachments(SOAPMessage msg, Map attachments) throws MessagingException, FileNotFoundException,
			RegistryException {
		for (Iterator<?> it = attachments.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String id = (String) entry.getKey();
			DataHandler dh = (DataHandler) entry.getValue();

			addAttachment(msg, id, dh);
		}
	}

	private void addAttachment(SOAPMessage msg, String id, DataHandler dh) throws FileNotFoundException,
			MessagingException, RegistryException {
		String cid = WSS4JSecurityUtilBST.convertUUIDToContentId(id);
		AttachmentPart ap = msg.createAttachmentPart(dh);
		ap.setContentId(cid);
		msg.addAttachmentPart(ap);
	}

	/**
	 * Convert SOAPFault back to RegistryException (if possible)
	 * 
	 * @param fault
	 *            SOAPFault
	 * @return RegistryException
	 */
	RegistryException createRegistryException(SOAPFault fault) {
		RegistryException result = null;

		// is this message too generic?
		String unknownError = "message.unknown";

		String exceptionName = null;
		if (fault.getFaultCode().startsWith(CanonicalConstants.SOAP_FAULT_PREFIX)) {
			// Old style faultcode value, skip prefix and colon
			exceptionName = fault.getFaultCode().substring(CanonicalConstants.SOAP_FAULT_PREFIX.length() + 1);
		} else if ( // TODO: SAAJ 1.3 has introduced preferred QName interfaces
		fault.getFaultCodeAsName().getURI().equals(CanonicalConstants.SOAP_FAULT_PREFIX)) {
			// New style
			exceptionName = fault.getFaultCodeAsName().getLocalName();
		}

		if (null == exceptionName) {
			// not a recognized ebXML fault
			result = new RegistryException(unknownError);
		} else {
			// ebXML fault
			String exceptionMessage = fault.getFaultString();
			unknownError = exceptionName + " " + exceptionMessage;

			/*
			 * Detail detail = fault.getDetail(); Iterator iter =
			 * detail.getDetailEntries(); int i=0; while (iter.hasNext()) {
			 * DetailEntry detailEntry = (DetailEntry)iter.next(); unknownError
			 * += " detailEntry[" + i++ + "] = " + detailEntry.toString(); }
			 */

			// TODO: get and reconstruct Stacktrace
			try {

				Class<?> exceptionClass = null;
				// exceptionClass =
				// Class.forName("it.cnr.icar.eric.common.exceptions." +
				// exceptionName);
				exceptionClass = Class.forName(exceptionName);

				if (RegistryException.class.isAssignableFrom(exceptionClass)) {
					// Exception is a RegistryException. Reconstitute it as a
					// RegistryException

					// NPE has null message..
					if (exceptionMessage != null) {
						Class<?>[] parameterDefinition = { String.class };
						Constructor<?> exceptionConstructor = exceptionClass.getConstructor(parameterDefinition);
						Object[] parameters = { exceptionMessage };
						result = (RegistryException) exceptionConstructor.newInstance(parameters);
					} else {
						Class<?>[] parameterDefinition = {};
						Constructor<?> exceptionConstructor = exceptionClass.getConstructor(parameterDefinition);
						Object[] parameters = {};
						result = (RegistryException) exceptionConstructor.newInstance(parameters);
					}
				} else {
					// Exception is not a RegistryException.

					// Make it a RegistryException with exceptionMessage
					// In future make it a nested Throwable of a
					// RegistryException
					// NPE has null message..
					result = new RegistryException(unknownError);
				}
			} catch (ClassNotFoundException e) {
				// could happen with non-eric server?
				result = new RegistryException(unknownError, e);
			} catch (NoSuchMethodException e) {
				// should not happen
				result = new RegistryException(unknownError, e);
			} catch (IllegalAccessException e) {
				// happens when?
				result = new RegistryException(unknownError, e);
			} catch (InvocationTargetException e) {
				// happens when?
				result = new RegistryException(unknownError, e);
			} catch (InstantiationException e) {
				// happens when trying to instantiate Interface
				result = new RegistryException(unknownError, e);
			}
		}
		return result;
	}

	SOAPMessage send(SOAPMessage msg) throws SOAPException {

		SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
		SOAPConnection connection = scf.createConnection();

		// if the sessionId exists, set it as a Mime header
		// This header will be used by the server to track authenticated
		// sessions
		if (credentialInfo != null) {
			if (credentialInfo.sessionId != null) {
				msg.getMimeHeaders().addHeader("Cookie", credentialInfo.sessionId);
			}
		}

		SOAPMessage reply = connection.call(msg, endpoint);

		// if the credentialInfo.sessionId is not null, cache the sessionId
		if (credentialInfo != null) {
			cacheSessionId(reply);
		}

		return reply;
	}

	// TODO: session - fill in this method
	private boolean isSessionExpired(SOAPMessage message) {
		boolean sessionExpired = false;

		return sessionExpired;
	}

	private void cacheSessionId(SOAPMessage message) {
		MimeHeaders mimeHeaders = message.getMimeHeaders();
		String[] header = mimeHeaders.getHeader("Set-Cookie");
		if (header != null) {
			for (int i = 0; i < header.length; i++) {
				if (header[i].startsWith("JSESSIONID")) {
					// parse JSESSIONID attribute
					String[] attributes = header[i].split(";");
					// JSESSIONID will be first attribute
					credentialInfo.sessionId = attributes[0];
					break;
				}
			}
		}
	}

	Reader processResponseBody(SOAPMessage response, String lookFor) throws JAXRException, SOAPException,
			TransformerConfigurationException, TransformerException {
		// grab info out of reply
		SOAPPart replyPart = response.getSOAPPart();
		Source replySource = replyPart.getContent();

		// transform
		TransformerFactory tFactory = TransformerFactory.newInstance();
		Transformer xFormer = tFactory.newTransformer();
		DOMResult domResult = new DOMResult();
		xFormer.transform(replySource, domResult);

		org.w3c.dom.Node node = domResult.getNode();

		while (node != null) {
			String nodeLocalName = node.getLocalName();

			if ((nodeLocalName != null) && (nodeLocalName.endsWith(lookFor))) {
				break;
			}

			node = nextNode(node);
		}

		if (node == null) {
			node = domResult.getNode();
			while (node != null) {
				String nodeLocalName = node.getLocalName();
				if ((nodeLocalName != null) && (nodeLocalName.endsWith(lookFor))) {
					break;
				}

				node = nextNode(node);
			}

			throw new JAXRException("message.elementNotFound: " + lookFor);
		}

		return domNode2StringReader(node);
	}

	private static org.w3c.dom.Node nextNode(org.w3c.dom.Node node) {
		// assert(node != null);
		org.w3c.dom.Node child = node.getFirstChild();

		if (child != null) {
			return child;
		}

		org.w3c.dom.Node sib;

		while ((sib = node.getNextSibling()) == null) {
			node = node.getParentNode();

			if (node == null) {
				// End of document
				return null;
			}
		}

		return sib;
	}

	StringReader domNode2StringReader(org.w3c.dom.Node node) throws TransformerConfigurationException,
			TransformerException {
		TransformerFactory tfactory = TransformerFactory.newInstance();
		StringWriter writer = null;

		Transformer serializer = tfactory.newTransformer();
		Properties oprops = new Properties();
		oprops.put("method", "xml");
		oprops.put("indent", "yes");
		serializer.setOutputProperties(oprops);
		writer = new StringWriter();
		serializer.transform(new DOMSource(node), new StreamResult(writer));

		String outString = writer.toString();

		// log.trace("outString=" + outString);
		StringReader reader = new StringReader(outString);

		return reader;
	}

	/**
	 * @return HashMap containing {contentId, DataHandler} entries or null if no
	 *         attachments
	 */
	private HashMap<String, Object> processResponseAttachments(SOAPMessage response) throws JAXRException, SOAPException,
			MessagingException {
		if (response.countAttachments() == 0) {
			return null;
		}

		HashMap<String, Object> attachMap = new HashMap<String, Object>();

		for (Iterator<?> it = response.getAttachments(); it.hasNext();) {
			AttachmentPart ap = (AttachmentPart) it.next();

			String uuid = WSS4JSecurityUtilBST.convertContentIdToUUID(ap.getContentId());

			DataHandler dh = ap.getDataHandler();
			attachMap.put(uuid, dh);
		}

		return attachMap;
	}

	@SuppressWarnings("unused")
	private boolean isSessionEstablished() {
		return false;
	}

	@SuppressWarnings("unused")
	private void establishSession() {

	}
}
