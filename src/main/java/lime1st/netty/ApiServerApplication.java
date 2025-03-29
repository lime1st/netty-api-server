package lime1st.netty;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lime1st.netty.server.core.ApiServer;
import lime1st.netty.user.Users;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ApiServerApplication {

    public static void main(String[] args) {

        // 스프링 컨텍스트 초기화
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("applicationContext.xml");

        context.registerShutdownHook();

        EntityManagerFactory emf = context.getBean(EntityManagerFactory.class);
        init(emf);

        ApiServer server = context.getBean(ApiServer.class);
        server.start();
    }

    private static void init(EntityManagerFactory emf) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            String[][] users = {
                    {"alex@mail.com", "alex", "passworda"},
                    {"bred@mail.com", "bred", "passwordb"},
                    {"ched@mail.com", "ched", "passwordc"},
                    {"dave@mail.com", "dave", "passwordd"}
            };
            for (String[] userData : users) {
                Users user = new Users();
                user.setEmail(userData[0]);
                user.setUsername(userData[1]);
                user.setPassword(userData[2]);
                em.persist(user);
            }
            em.getTransaction().commit();
        }
    }
}
