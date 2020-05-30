package org.knx;

public class ETSLoaderException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3656841801792607064L;
	
	public ETSLoaderException(Exception e)
	{
		super(e);
	}

	public ETSLoaderException(String message, Throwable cause) {
		super(message, cause);
	}

	public ETSLoaderException(String message) {
		super(message);
	}

	public ETSLoaderException(Throwable cause) {
		super(cause);
	}
	
	

}
