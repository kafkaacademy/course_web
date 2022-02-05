package academy.kafka;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    static Logger logger = LoggerFactory.getLogger(App.class);
    public static void main(final String[] args) { 
        System.out.println("test log configuration");       
        logger.info("info messages, with this configuration info go to console");
        logger.info("...and apache kafka messages go to file (see <root>/logs/app.log)");
        logger.error("error from your app goes to console");
        logger.warn("warn from your app goes to console");
        logger.debug("debug  from your app goes to console");
        org.apache.kafka.SimulateApacheKafka.testLogging();

    }
}
