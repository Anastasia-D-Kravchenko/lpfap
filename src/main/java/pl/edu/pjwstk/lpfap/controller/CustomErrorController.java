package pl.edu.pjwstk.lpfap.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public Object error(HttpServletRequest req) {
        int status = statusOf(req);
        boolean wantsJson = wantsJson(req);

        if (wantsJson) {
            return ResponseEntity.status(status).body(Map.of(
                    "timestamp", java.time.LocalDateTime.now().toString(),
                    "status",    status,
                    "error",     HttpStatus.valueOf(status).getReasonPhrase()
            ));
        }

        if (status == 404) return "forward:/error/404.html";
        return "forward:/error/generic.html";
    }

    private int statusOf(HttpServletRequest req) {
        Object code = req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        return code instanceof Integer i ? i : 500;
    }

    private boolean wantsJson(HttpServletRequest req) {
        String accept = req.getHeader("Accept");
        String uri    = (String) req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        return (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE))
                || (uri != null && uri.startsWith("/api/"));
    }
}
