package com.zoo.management.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * פילטר לוגים מסודר עבור בקשות HTTP שנשלחות מהאתר.
 * מציג ב-Docker Desktop מידע ברור על כל קריאת API נכנסת והתשובה שהוחזרה.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ZooAuditLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger("ZooAuditLog");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String uri = httpRequest.getRequestURI();

            // מתעדים רק בקשות ל-API (מדלגים על קבצים סטטיים של css/js/html)
            if (uri.startsWith("/api")) {
                String method = httpRequest.getMethod();
                long startTime = System.currentTimeMillis();

                // אייקון ייעודי לפי סוג הפעולה
                String actionIcon = switch (method) {
                    case "POST" -> "➕ [POST]";
                    case "PUT" -> "✏️ [PUT]";
                    case "DELETE" -> "🗑️ [DELETE]";
                    case "GET" -> "🔍 [GET]";
                    default -> "🌐 [" + method + "]";
                };

                // תיעוד הבקשה הנכנסת
                log.info("{} {} | התחלת פעולה מהאתר", actionIcon, uri);

                try {
                    chain.doFilter(request, response);
                } finally {
                    long duration = System.currentTimeMillis() - startTime;
                    int status = httpResponse.getStatus();
                    String statusText = status >= 200 && status < 300 ? "הצלחה (" + status + ")" : "קוד תגובה: " + status;

                    log.info("✔️  {} {} -> {} [זמן ביצוע: {}ms]", actionIcon, uri, statusText, duration);
                }
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
