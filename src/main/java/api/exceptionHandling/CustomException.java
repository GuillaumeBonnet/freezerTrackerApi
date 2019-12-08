package api.exceptionHandling;

import java.util.List;

public class CustomException extends RuntimeException {

    public CustomException(String exceptionMessage) {
        super(exceptionMessage);
	}
    public CustomException(String exceptionMessage, List<String> details) {
        super(exceptionMessage);
        this.details = details;
	}

	/**
     *
     */
    private static final long serialVersionUID = 1L;

    public List<String> details;

}
