package lime1st.netty.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository {

    private final EntityManager em;

    public UserRepository(EntityManagerFactory emf) {
        this.em = emf.createEntityManager();
    }

    public void save(Users user) {
        em.persist(user);
    }

    public List<Users> findAll() {
        return em.createQuery("select u from Users u", Users.class)
                .getResultList();
    }

    public Users findOne(Long id) {
        return em.find(Users.class, id);
    }

    public Users findByEmail(String email) {
        return em.createQuery("select u from Users u where u.email = :email", Users.class)
                .setParameter("email", email)
                .getSingleResult();
    }

    public Users findByPassword(String password) {
        return em.createQuery("select u from Users u where u.password = :password", Users.class)
                .setParameter("password", password)
                .getSingleResult();
    }
}
