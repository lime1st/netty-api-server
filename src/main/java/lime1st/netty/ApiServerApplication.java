package lime1st.netty;

import lime1st.netty.bootstrap.ApiServer;
import lime1st.netty.domain.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiServerApplication {

    private static final Logger log = LoggerFactory.getLogger(ApiServerApplication.class);
    private static boolean initialized = false;

    private final UserRepository userRepository;

    public ApiServerApplication(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static void main(String[] args) throws Exception {
        ApiServerApplication app = new ApiServerApplication(new UserRepository());
        app.initializeData();
        ApiServer server = new ApiServer(8080, 1, Runtime.getRuntime().availableProcessors());
        server.start();
    }

    private synchronized void initializeData() {
        if (!initialized) {
            log.info("Initializing data...");
            initUsers();
            initialized = true;
            log.info("Initialized data");
        }
    }

    private synchronized void initUsers() {
        try {
            userRepository.insertUser(1L, "alex@mail.com", "alex", "passworda");
            userRepository.insertUser(2L, "bred@mail.com", "bred", "passwordb");
            userRepository.insertUser(2L, "ched@mail.com", "ched", "passwordc");
            userRepository.insertUser(2L, "dave@mail.com", "dave", "passwordd");
        } catch (Exception e) {
            log.error("Error initializing users", e);
        }
    }
}
