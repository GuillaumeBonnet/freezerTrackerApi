package api.exceptionHandling;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
	

	@ExceptionHandler(CustomException.class)
	public @ResponseBody ResponseEntity<ApiErrorResponse> handleNotFoundException(CustomException ex) {
		
		return new ApiErrorResponse()
			.withStatus(HttpStatus.BAD_REQUEST)
			.withError_code("BAD_REQUEST")
			.withMessage(ex.getLocalizedMessage())
			.withDetails(ex.details)
			.build()
		;
	}		

	/* -------------------------------------------------------------------------- */
	/*                                Inner classes                               */
	/* -------------------------------------------------------------------------- */

	@XmlRootElement(name = "error")
	public class ApiErrorResponse {

		private HttpStatus status;
		private String error_code;
		private String message;
		private List<String> details = new ArrayList<String>();

		
		public ApiErrorResponse withStatus(HttpStatus status) {
			this.status = status;
			return this;
		}
	
		public ApiErrorResponse withError_code(String error_code) {
			this.error_code = error_code;
			return this;
		}
	
		public ApiErrorResponse withMessage(String message) {
			this.message = message;
			return this;
		}
	
		public ApiErrorResponse withDetails(List<String> details) {
			if(details != null) {
				this.details.addAll(details);
			}
			return this;
		}

		public ResponseEntity<ApiErrorResponse> build() {
			return new ResponseEntity<ApiErrorResponse>(this, this.status);
		}

		/* -------------------------------------------------------------------------- */
		/*                                   Getters                                  */
		/* -------------------------------------------------------------------------- */
		public HttpStatus getStatus() {
			return status;
		}

		public void setStatus(HttpStatus status) {
			this.status = status;
		}

		public String getError_code() {
			return error_code;
		}

		public void setError_code(String error_code) {
			this.error_code = error_code;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public List<String> getDetails() {
			return details;
		}

		public void setDetails(List<String> details) {
			this.details = details;
		}

	}	

}