package rs.ac.uns.ftn.informatika.jpa.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String port = request.getLocalPort() + ""; // port instance
        String method = request.getMethod();
        String uri = request.getRequestURI();
        logger.info("Zahtjev {} {} primljen na instanci sa portom {}", method, uri, port);
        return true;
    }
}
