package configuration;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpHeaders;
import org.springframework.security.web.firewall.FirewalledRequest;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class HttpFirewallChangesSamesiteCookieAttr implements HttpFirewall {
    @Override
    public FirewalledRequest getFirewalledRequest(HttpServletRequest request)
            throws RequestRejectedException {
        return new RequestWrapper(request);
    }

    @Override
    public HttpServletResponse getFirewalledResponse(HttpServletResponse response) {
        return new ResponseWrapper(response);
    }

    public class ResponseWrapper extends HttpServletResponseWrapper {
        /**
         * Constructs a response adaptor wrapping the given response.
         *
         * @param response The response to be wrapped
         * @throws IllegalArgumentException if the response is null
         */
        public ResponseWrapper(HttpServletResponse response) {
            super(response);
        }
    }

    public class RequestWrapper extends FirewalledRequest {
        /**
         * Constructs a request object wrapping the given request.
         *
         * @param request The request to wrap
         * @throws IllegalArgumentException if the request is null
         */
        public RequestWrapper(HttpServletRequest request) {
            super(request);
        }

        /**
         * Must be empty by default in Spring Boot. See FirewalledRequest.
         */
        @Override
        public void reset() {
        }

        @Override
        public HttpSession getSession(boolean create) {
            HttpSession session = super.getSession(create);
            if (create) {
                ServletRequestAttributes ra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (ra != null) {
                    overwriteSetCookie(ra.getResponse());
                }
            }
            return session;
        }

        @Override
        public String changeSessionId() {
            String newSessionId = super.changeSessionId();
            ServletRequestAttributes ra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (ra != null) {
                overwriteSetCookie(ra.getResponse());
            }
            return newSessionId;
        }

        void overwriteSetCookie(HttpServletResponse response) {
            if (response != null) {
                Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
                boolean firstHeader = true;
                for (String header : headers) { // there can be multiple Set-Cookie attributes
                    if (firstHeader) {
                        response.setHeader(HttpHeaders.SET_COOKIE,
                                // A cookie with the Secure attribute is only sent by the browser over the HTTS
                                // protocol, except on localhost
                                String.format("%s; %s", header, "SameSite=None; Secure")); // set
                        firstHeader = false;
                        continue;
                    }
                    response.addHeader(HttpHeaders.SET_COOKIE,
                            String.format("%s; %s", header, "SameSite=None; Secure")); // add
                }
            }
        }
    }
}
