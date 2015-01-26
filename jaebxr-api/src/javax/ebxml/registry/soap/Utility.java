package javax.ebxml.registry.soap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import javax.ebxml.registry.CanonicalConstants;
import javax.xml.bind.JAXBException;
import javax.xml.registry.JAXRException;
import javax.xml.registry.RegistryException;
import javax.xml.soap.SOAPMessage;

import org.oasis.ebxml.registry.bindings.rim.ExternalLinkType;
import org.oasis.ebxml.registry.bindings.rim.ServiceBindingType;
import org.oasis.ebxml.registry.bindings.rs.RegistryError;
import org.oasis.ebxml.registry.bindings.rs.RegistryResponseType;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;


public class Utility {
    /**
     * @link
     * @shapeType PatternLink
     * @pattern Singleton
     * @supplierRole Singleton factory
     */

    /* # private Utility _utility; */
    private static Utility instance = null;
    private static org.oasis.ebxml.registry.bindings.rs.ObjectFactory rsFactory = null;

    /**
     * Class Constructor.
     *
     *
     * @see
     */
    protected Utility() {
    }

    /**
     * Strip urn:uuid: part from start of registry
     * object id.  If id is null or doesn't start with
     * urn:uuid: then the id is returned without modification.
     */
    public String stripId(String id) {
        if (id != null) {
            if (id.startsWith("urn:uuid:")) {
                id = id.substring(9).trim();
            } else {
                // id is urn
                id = id.replaceAll(":", "_");
            }
        }

        return id;
    }

    /**
     *     Get the <?xml version="1.0"?> decalaration from XML bytes
     */
    public String getXMLDeclaration(byte[] bytes) {
        String str = new String(bytes);

        return removeXMLDeclaration(new StringBuffer(str));
    }

    /**
     * Removes the <?xml version="1.0"?> decalaration from XML bytes
     *    @return the bytes of the XML document
     */
    public byte[] removeXMLDeclaration(byte[] bytes) throws IOException {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        int declEndIndex = 0;

        // Get the end index of declaration
        if (bytes[0] == '<') {
            boolean hasDecl = false;

            for (int i = 1; i < bytes.length; i++) {
                if (bytes[1] == ' ') {
                    //Skip spaces after '<'
                    declEndIndex++;
                } else if (bytes[i] == '?') {
                    // It has declaration
                    hasDecl = true;
                }

                if (hasDecl) {
                    if (bytes[i] != '>') {
                        declEndIndex++;
                    } else {
                        declEndIndex++;

                        break;
                    }
                }
            }
        }

        // skip all spaces after the xml declaration
        for (int i = declEndIndex; i < bytes.length; i++) {
            declEndIndex++;

            if (bytes[i] != ' ') {
                break;
            }
        }

        // System.err.println(declEndIndex + "!!!!!");
        bos.write(bytes, declEndIndex, bytes.length - declEndIndex);
        bos.flush();
        bos.close();

        //System.err.println(new String(bos.toByteArray()));
        return bos.toByteArray();
    }

    /**
     * Removes the <?xml version="1.0"?> decalaration from XML string
     *      @return the XML declaration
     */
    public String removeXMLDeclaration(StringBuffer str) {
        String decl = "";
        int len = str.length();
        int startIndex = -1; //start of what we are removing
        int endIndex = -1; //end of what we are removing

        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == '<') {
                decl += "<";
                startIndex = i;

                //Skip spaces after '<'
                i++;

                while (str.charAt(i) == ' ') {
                    decl += " ";
                    i++;
                }

                if (str.charAt(i) == '?') {
                    //We have an XML declaration to remove
                    //Skip forward and find matching '>'
                    while (str.charAt(i) != '>') {
                        decl += str.charAt(i);
                        i++;
                    }

                    decl += ">";
                    endIndex = i;

                    break;
                } else {
                    //Not an XML declaration
                    startIndex = -1;

                    break;
                }
            }
        }

        if (startIndex != -1) {
            str.delete(startIndex, endIndex + 1);
        }

        return decl;
    }

    /**
     *     Get bytes array from InputStream
     */
    public byte[] getBytesFromInputStream(InputStream is)
        throws IOException {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();

        while (true) {
            byte[] buffer = new byte[100];
            int noOfBytes = is.read(buffer);

            if (noOfBytes == -1) {
                break;
            } else {
                bos.write(buffer, 0, noOfBytes);
            }
        }

        bos.flush();
        bos.close();

        return bos.toByteArray();
    }

    /**
     *     Create a SOAPMessage containing a registry request (e.g. SubmitObjectsRequest)
     *     @param req the InputStream to the registry request
     *     @return the InputStream to the the created SOAPMessage
     */
    public InputStream createSOAPStreamFromRequestStream(InputStream req)
        throws javax.xml.soap.SOAPException, IOException,
            javax.mail.internet.ParseException {
        String soapNS = "http://schemas.xmlsoap.org/soap/envelope/";
        String soapEnv = "soap-env";
        String soapEnvPrefix = soapEnv + ":";

        byte[] requestBytesWithDecl = getBytesFromInputStream(req);
        String decl = getXMLDeclaration(requestBytesWithDecl);
        byte[] requestBytes = removeXMLDeclaration(requestBytesWithDecl);
        requestBytesWithDecl = null;

        // System.err.println(decl);
        String partBeforeRequestStr = decl + "\n<" + soapEnvPrefix +
            "Envelope" + "\n\txmlns:soap-env=\"" + soapNS + "\"" +
            "\n\txmlns:SOAP-SEC=\"http://schemas.xmlsoap.org/soap/security/2000-12\" soap-env:actor=\"some-uri\"" +
            "\n\tsoap-env:mustUnderstand=\"1\">" +
            //The SOAP Header
            "\n\t<" + soapEnvPrefix + "Header>" +
	    // tell server about our superior SOAP Fault capabilities
	    "\n\t\t<" + CanonicalConstants.SOAP_CAPABILITY_HEADER_LocalName +
	    " xmlns='" + CanonicalConstants.SOAP_CAPABILITY_HEADER_Namespace + "'>" +
	    CanonicalConstants.SOAP_CAPABILITY_ModernFaultCodes +
	    "</" + CanonicalConstants.SOAP_CAPABILITY_HEADER_LocalName + ">" +
            //DSIG goes here
            "\n\t</" + soapEnvPrefix + "Header>" +
            //The SOAP Body
            "\n\t<" + soapEnvPrefix + "Body SOAP-SEC:id=\"Body\">" + "\n";

        //The ebXML Registry request goes here
        String partAfterRequestStr = "\n\t</" + soapEnvPrefix + "Body>" +
            "\n</" + soapEnvPrefix + "Envelope>";

        // We concatenate the three bytes array to a single big array
        byte[] partBeforeRequestBytes = partBeforeRequestStr.getBytes(
                "ISO-8859-1"); // The soap env does not have non ascii chars
        byte[] partAfterRequestBytes = partAfterRequestStr.getBytes(
                "ISO-8859-1"); // The soap env does not have non ascii chars
        byte[] soapBytes = new byte[partBeforeRequestBytes.length +
            requestBytes.length + partAfterRequestBytes.length];

        int soapBytesIndex = 0;

        for (int a = 0; a < partBeforeRequestBytes.length; a++) {
            soapBytes[soapBytesIndex] = partBeforeRequestBytes[a];
            soapBytesIndex++;
        }

        for (int b = 0; b < requestBytes.length; b++) {
            soapBytes[soapBytesIndex] = requestBytes[b];
            soapBytesIndex++;
        }

        for (int c = 0; c < partAfterRequestBytes.length; c++) {
            soapBytes[soapBytesIndex] = partAfterRequestBytes[c];
            soapBytesIndex++;
        }

        // System.err.println("L343:" + new String(soapBytes));
        return new java.io.ByteArrayInputStream(soapBytes);
    }

    /**
     *     Create a SOAPMessage containing a registry request (e.g. SubmitObjectsRequest)
     *     @param req the InputStream to the registry request
     *     @return the created SOAPMessage
     */
    public SOAPMessage createSOAPMessageFromRequestStream(InputStream reqStream)
        throws javax.xml.soap.SOAPException, IOException,
            javax.mail.internet.ParseException {
        InputStream is = createSOAPStreamFromRequestStream(reqStream);

        javax.xml.soap.MimeHeaders mimeHeaders = new javax.xml.soap.MimeHeaders();

        javax.mail.internet.ContentType contentType = new javax.mail.internet.ContentType(
                "text/xml"); //"multipart/related");

        String contentTypeStr = contentType.toString();

        //System.err.println("contentTypeStr = '" + contentTypeStr + "'");
        mimeHeaders.addHeader("Content-Type", contentTypeStr);
        mimeHeaders.addHeader("Content-Id", "ebXML Registry SOAP request");

        javax.xml.soap.MessageFactory factory = javax.xml.soap.MessageFactory.newInstance();
        SOAPMessage msg = factory.createMessage(mimeHeaders, is);

        msg.saveChanges();

        return msg;
    }

    /**
     *     Create a SOAPMessage object from a InputStream to a SOAPMessage
     *     @param soapStream the InputStream to the SOAPMessage
     *     @return the created SOAPMessage
     */
    public SOAPMessage createSOAPMessageFromSOAPStream(InputStream soapStream)
        throws javax.xml.soap.SOAPException, IOException,
            javax.mail.internet.ParseException {
    	
        javax.xml.soap.MimeHeaders mimeHeaders = new javax.xml.soap.MimeHeaders();

        javax.mail.internet.ContentType contentType = new javax.mail.internet.ContentType(
                "text/xml"); //"multipart/related");

        String contentTypeStr = contentType.toString();

        //System.err.println("contentTypeStr = '" + contentTypeStr + "'");
        mimeHeaders.addHeader("Content-Type", contentTypeStr);
        mimeHeaders.addHeader("Content-Id", "ebXML Registry SOAP request");

        javax.xml.soap.MessageFactory factory = javax.xml.soap.MessageFactory.newInstance();
        SOAPMessage msg = factory.createMessage(mimeHeaders, soapStream);

        // this saveChanges() call breaks SaaJ attachment branch with JDK6 code switch!!!
        // resulting in broken multipart/related + length calculated wrong 
//        msg.saveChanges();

        return msg;
    }

    public RegistryError createRegistryErrorFromThrowable(Throwable t,
        String codeContext, String errCode) throws JAXBException {
        RegistryError re = rsFactory.createRegistryError();

        String stackTrace = getStackTraceFromThrowable(t);

        //System.err.println(stackTrace);
        String nestedStackTrace = null;

        if (t instanceof JAXRException) {
            Throwable nestedException = ((JAXRException) t).getCause();
            nestedStackTrace = getStackTraceFromThrowable(nestedException);
        }

        stackTrace += ("\n\nNested exception was:\n" + nestedStackTrace);

        re.setValue(stackTrace);
        re.setCodeContext(codeContext);
        re.setErrorCode(errCode);

        return re;
    }

    public RegistryResponseType createRegistryResponseFromThrowable(Throwable t,
        String codeContext, String errCode) throws JAXBException {
        RegistryResponseType resp = null;

        resp = rsFactory.createRegistryResponseType();
		updateRegistryResponseFromThrowable(resp, t, codeContext, errCode);

        return resp;
    }

    public void updateRegistryResponseFromThrowable(
	    org.oasis.ebxml.registry.bindings.rs.RegistryResponseType resp,
	    Throwable t, String codeContext, String errCode) throws JAXBException {
	        resp.setStatus(CanonicalConstants.CANONICAL_RESPONSE_STATUS_TYPE_ID_Failure);
	
	        org.oasis.ebxml.registry.bindings.rs.RegistryErrorList el = rsFactory.createRegistryErrorList();
	        el.getRegistryError().add(createRegistryErrorFromThrowable(t,
	                codeContext, errCode));
	        resp.setRegistryErrorList(el);
    }

    public String getStackTraceFromThrowable(Throwable t) {
        String trace = null;

        if (t != null) {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            t.printStackTrace(pw);
            trace = sw.toString();
        }

        return trace;
    }

    public String createTimestamp() {
        // Get the time zone offset in +HH:mm format
        java.util.TimeZone tz = java.util.TimeZone.getDefault();

        float rawOffset = tz.getRawOffset() / 1000 / 60 / 60.0f;
        int offsetHr = (int) Math.abs(rawOffset);
        int offsetMin = (int) (rawOffset - offsetHr) * 60;

        String offset = (rawOffset > 0) ? "+" : "-";
        offset += ((offsetHr < 10) ? "0" : "");
        offset += offsetHr;
        offset += ":";
        offset += ((offsetMin < 10) ? "0" : "");
        offset += offsetMin;

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss");

        return sdf.format(new java.util.Date()) + offset;
    }

    /**
     *         Return ArrayList of ExternalLink that points to unresolvable Http URLs. Any
     *         non-Http URLs will not be checked. Any non-Http URLs and other types
     *         of URIs will not be checked. If the http response code is smaller than 200
     *         or bigger than 299, the http URL is considered invalid.
     */
    public ArrayList<Object> validateURIs(ArrayList<?> sourceRegistryObjects)
        throws RegistryException {
        ArrayList<Object> invalidURLROs = new ArrayList<Object>();
        Iterator<?> iter = sourceRegistryObjects.iterator();

        while (iter.hasNext()) {
            Object ro = iter.next();
            String uRI = null;

            if (ro instanceof ExternalLinkType) {
                uRI = ((ExternalLinkType) ro).getExternalURI();
            } else if (ro instanceof ServiceBindingType) {
                uRI = ((ServiceBindingType) ro).getAccessURI();
            } else {
                throw new RegistryException("message.unknownRegistryObjectType");
            }

            if (!isValidURI(uRI)) {
                invalidURLROs.add(ro);
            }
        }

        return invalidURLROs;
    }

    /**
     * Validates URIs according to Registry specs.
     *
     * From Registry specs: "If the URI is a URL then a registry MUST validate the
     * URL to be resolvable at the time of submission before accepting an
     * ExternalLink submission to the registry."
     *
     * Any non-Http URLs and other types of URIs will not be checked. If the http
     * response code is smaller than 200 or bigger than 299, the http URL is
     * considered invalid.
     */
    public boolean isValidURI(String uRI) {
        if (uRI == null) {
            return false;
        }
        
        try {
            @SuppressWarnings("unused")
			java.net.URI _uri = new java.net.URI(uRI);
        } catch (java.net.URISyntaxException e) {
            // Not an URI. return false
            return false;
        }

        // Quoting Doug: "URLs are resolvable URIs"!
        // Try to resolve the URLs here, if it is URL
        try {
            java.net.URL uRL = new java.net.URL(uRI);
            if (uRI.startsWith("http:")) {
                java.net.HttpURLConnection httpUrlConn = (java.net.HttpURLConnection) uRL.openConnection();
                int responseCode = httpUrlConn.getResponseCode();

                return (!((responseCode < 200) || (responseCode > 299)));
                /*
                if ((responseCode < 200) || (responseCode > 299)) {
                    return false;
                } else {
                    return true;
                }
                */
            } else {
                @SuppressWarnings("unused")
				URLConnection conn = uRL.openConnection();
            }
        } catch (java.net.MalformedURLException e) {
            // Not an URL, will not try to resolve. Valid URI
        	return true;
        } catch (IOException e) {
            return false;
        }
        
        return true;
    }

    //Based on code contributed by Bobby Bissett
    public org.w3c.dom.Element getSignatureElement(SOAPMessage msg)
        throws javax.xml.soap.SOAPException,
            javax.xml.transform.TransformerException {
        org.w3c.dom.Element sigElement = null;

        javax.xml.transform.Transformer xFormer = javax.xml.transform.TransformerFactory.newInstance()
                                                                                        .newTransformer();

        // grab info out of msg
        javax.xml.soap.SOAPPart msgPart = msg.getSOAPPart();
        javax.xml.transform.Source msgSource = msgPart.getContent();

        // transform
        javax.xml.transform.dom.DOMResult domResult = new javax.xml.transform.dom.DOMResult();
        xFormer.transform(msgSource, domResult);

        //root node is the soap:Envelope
        Node envelopeNode = domResult.getNode();

        //now you have the node. the following code strips off the envelope of
        //the soap message to get to the actual content
        // Advance to envelope node in case of text nodes preceding it
        while ((envelopeNode.getLocalName() == null) ||
                (!envelopeNode.getLocalName().equalsIgnoreCase("envelope"))) {
            envelopeNode = envelopeNode.getFirstChild();
        }

        // Advance to header within envelope node
        Node headerNode = envelopeNode.getFirstChild();

        while ((headerNode.getLocalName() == null) ||
                (!headerNode.getLocalName().equalsIgnoreCase("header"))) {
            headerNode = headerNode.getNextSibling();
        }

        //System.err.println("headerNode name is: " + headerNode.getLocalName());
        // Advance to signature node within header
        Node sigNode = headerNode.getFirstChild();

        if (sigNode == null) {
            return null;
        }

        //System.err.println("sigNode: " + sigNode);
        while ((sigNode.getLocalName() == null) ||
                (!sigNode.getLocalName().equalsIgnoreCase("signature"))) {
            sigNode = sigNode.getNextSibling();

            if (sigNode == null) {
                return null;
            }
        }

        //Desired Signature element may be inside a SOAP-SEC signature element
        if (!sigNode.getNamespaceURI().equals("http://www.w3.org/2000/09/xmldsig#")) {
            sigNode = sigNode.getFirstChild();

            while ((sigNode.getLocalName() == null) ||
                    (!sigNode.getLocalName().equalsIgnoreCase("signature"))) {
                sigNode = sigNode.getNextSibling();
            }
        }

        if (sigNode.getNamespaceURI().equals("http://www.w3.org/2000/09/xmldsig#")) {
            if (sigNode instanceof org.w3c.dom.Element) {
                sigElement = (org.w3c.dom.Element) sigNode;
            }
        }

        return sigElement;
    }

    /**
     *     Convert the apostrophes in a String field in a SQL statement to 2
     *     consecutive apostrohes
     */
    public static String escapeSQLChars(String stringField) {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < stringField.length(); i++) {
            char c = stringField.charAt(i);
            result.append(c);

            if (c == '\'') {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Method Declaration.
     *
     *
     * @return
     *
     * @see
     */
    public synchronized static Utility getInstance() {
        if (instance == null) {
            instance = new Utility();
            rsFactory = new org.oasis.ebxml.registry.bindings.rs.ObjectFactory();
        }

        return instance;
    }


    /**
     * This is an implementation of the JAXP DOM Node.setTextContent
     * method, which does not exist in JDK 1.4. This code was adapted
     * from the Apache Xerces 2.8.0 implementation of this method.
     */
    public static void setTextContent(Node node, String textContent)
	throws DOMException
    {
        Node child;

        // get rid of any existing children
        while ((child = node.getFirstChild()) != null) {
            node.removeChild(child);
        }

        // create a Text node to hold the given content
        if (textContent != null && textContent.length() != 0) {
            node.appendChild(node.getOwnerDocument().createTextNode(textContent));
        }
    }
}
