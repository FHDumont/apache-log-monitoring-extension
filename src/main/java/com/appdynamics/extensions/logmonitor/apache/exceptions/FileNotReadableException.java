package com.appdynamics.extensions.logmonitor.apache.exceptions;

/**
 * @author Florencio Sarmiento
 *
 */
public class FileNotReadableException extends RuntimeException {

	private static final long serialVersionUID = 284113142708944947L;

	public FileNotReadableException() {
	}

	public FileNotReadableException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileNotReadableException(String message) {
		super(message);
	}

	public FileNotReadableException(Throwable cause) {
		super(cause);
	}

}
