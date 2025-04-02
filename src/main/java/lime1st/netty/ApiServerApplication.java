package lime1st.netty;

import lime1st.netty.server.framework.ApiServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiServerApplication {

    private static final Logger log = LoggerFactory.getLogger(ApiServerApplication.class);

    public static void main(String[] args) throws Exception {
        ApiServer server = new ApiServer(8080, 1, Runtime.getRuntime().availableProcessors());
        server.start();
    }
}
