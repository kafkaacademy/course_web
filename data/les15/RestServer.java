package academy.kafka;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import academy.kafka.entities.ProvinceAggregate;
import academy.kafka.utils.KafkaUtils;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;

public class RestServer extends Thread {
  public static final ObjectMapper JACKSON_MAPPER = KafkaUtils.getJacksonMapper();

  final KafkaStreams streams;
  boolean ready = false;
  final int pause;
  final String storeName;

  public RestServer(KafkaStreams streams, String storeName,int pause) {
    this.pause = pause;
    this.storeName=storeName;
    this.streams = streams;
  }

  public void run() {
    while (!ready) {
      try {
        sleep(pause * 1000);
        ReadOnlyKeyValueStore<String, ProvinceAggregate> keyValueStore = streams.store(storeName,
            QueryableStoreTypes.keyValueStore());

        KeyValueIterator<String, ProvinceAggregate> range = keyValueStore.all();
        while (range.hasNext()) {
          ready = true;
          KeyValue<String, ProvinceAggregate> next = range.next();
          System.out.println(next.value);
        }
        if (ready) {
          System.out.println("restservice starting");
      
          Undertow server = Undertow.builder().addHttpListener(8080, "0.0.0.0")
              .setHandler(Handlers.pathTemplate().add("rest/{topic}/{key}", new HttpHandler() {

                @Override
                public void handleRequest(final HttpServerExchange exchange) throws Exception {
                  exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                  PathTemplateMatch pathMatch = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);

                  JsonNode result = null;

                //  String topic = pathMatch.getParameters().get("topic");
                  String keyStr = pathMatch.getParameters().get("key");
                  ProvinceAggregate pa = (ProvinceAggregate) keyValueStore.get(keyStr);
                  if (pa != null)
                    result = JACKSON_MAPPER.readTree(pa.toJson());
                  else {
                    exchange.setStatusCode(404);
                    return;
                  }

                  exchange.setStatusCode(200);
                  exchange.getResponseSender().send(result.toString()); // nodig

                }
              })).build();
          server.start();
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}