package ws.filters;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtre CORS pour WildFly
 */
public class CORSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Récupérer l'Origin du request
        String origin = httpRequest.getHeader("Origin");

        // Ajouter les headers CORS à TOUTES les réponses
        // Pour les credentials, il faut spécifier l'origine exacte au lieu de *
        if (origin != null && (origin.contains("localhost") || origin.contains("127.0.0.1"))) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            // Par défaut pour développement local
            httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        }

        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept, X-Requested-With");
        httpResponse.setHeader("Access-Control-Expose-Headers", "Content-Type, Authorization");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

        // Gérer les preflight requests OPTIONS
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
