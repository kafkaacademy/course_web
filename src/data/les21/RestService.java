package academy.kafka.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import io.undertow.util.PathTemplateMatch;
import io.undertow.util.StatusCodes;

public class RestService {

    final private static String host = "localhost";
    final private static int port = 8082;
    private static final ObjectMapper JACKSON_MAPPER = new ObjectMapper();
    private static Undertow restServer;

    private static JsonNode find(String key) {
        String resultStr = """
                {"bsn":"BSN1235","FirstName":"Pietje", "lastName": "Puk"}
                """;
        JsonNode result = null;
        Integer id = null;
        try {
            id = Integer.parseInt(key);
        } catch (Exception e) {
            return null;
        }
        try {
            result = JACKSON_MAPPER.readTree(resultStr);
            ((ObjectNode) result).put("bsn", "BSN" + id);

        } catch (JsonMappingException e) {
        } catch (JsonProcessingException e) {
        }
        return result;
    }

    static class RestHandler implements HttpHandler {

        @Override
        public void handleRequest(HttpServerExchange exchange) throws Exception {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseHeaders().put(new HttpString("Access-Control-Allow-Origin"), "*");

            PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
            String id = pathMatch.getParameters().get("bsn");
            JsonNode result = find(id);
            if (result == null) {
                exchange.setStatusCode(StatusCodes.NOT_FOUND);
                return;
            }
            exchange.setStatusCode(StatusCodes.FOUND);
            exchange.getResponseSender().send(result.toString());

        }
    }

    public static void main(String args[]) {
        restServer = Undertow.builder().addHttpListener(port, host)
                .setHandler(Handlers.pathTemplate().add("/rest/{bsn}", new RestHandler())).build();
         System.out.println(String.format("test in terminal with \"curl http://%s:%d/rest/123456\"", host, port));
        restServer.start();
    }

}
