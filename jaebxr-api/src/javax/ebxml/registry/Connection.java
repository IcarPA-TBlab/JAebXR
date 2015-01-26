package javax.ebxml.registry;

import java.util.Set;

import javax.xml.registry.JAXRException;

public class Connection implements javax.xml.registry.Connection {

	private javax.xml.registry.Connection c = null;
	private RegistryService rs = null;
	
	public Connection(javax.xml.registry.Connection c) throws JAXRException {
		this.c = c;
		if (c != null)
			this.rs = new RegistryService(c.getRegistryService());
	}
	
	@Override
	public void close() throws JAXRException {
		c.close();	
	}

	@Override
	public Set<?> getCredentials() throws JAXRException {
		return c.getCredentials();
	}

	@Override
	public RegistryService getRegistryService() throws JAXRException {
		return rs;
	}

	@Override
	public boolean isClosed() throws JAXRException {
		return c.isClosed();
	}

	@Override
	public boolean isSynchronous() throws JAXRException {
		return c.isSynchronous();
	}

	@Override
	public void setCredentials(@SuppressWarnings("rawtypes") Set arg0) throws JAXRException {
		c.setCredentials(arg0);
	}

	@Override
	public void setSynchronous(boolean arg0) throws JAXRException {
		c.setSynchronous(arg0);
	}

}
