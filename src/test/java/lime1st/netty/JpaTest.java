package lime1st.netty;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lime1st.netty.user.Users;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JpaTest {

    private static ClassPathXmlApplicationContext context;
    private static EntityManager em;

    @BeforeAll
    static void setUp() {
        context = new ClassPathXmlApplicationContext("dataSourceConfig.xml");
        EntityManagerFactory emf = context.getBean(EntityManagerFactory.class);
        em = emf.createEntityManager();
    }

    @AfterAll
    static void tearDown() {
        if (em != null) em.close();
        if (context != null) context.close();
    }

    @Test
    public void testPersistAndFind() {
        em.getTransaction().begin();
        Users user = new Users();
        user.setUsername("Test User");
        em.persist(user);
        em.getTransaction().commit();

        Users found = em.find(Users.class, user.getId());
        assertEquals("Test User", found.getUsername());
    }
}