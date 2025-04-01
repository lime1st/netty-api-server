package lime1st.netty.user.adapter.out.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;
import java.util.function.Function;

public class UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepository.class);
    private static final UserRepository INSTANCE = new UserRepository();
    private final EntityManagerFactory emf;

    public UserRepository() {
        try {
            this.emf = Persistence.createEntityManagerFactory("nettyPU");
            log.info("EntityManagerFactory initialized for nettyPU.");
        } catch (Exception e) {
            log.error("Failed to initialize EntityManagerFactory", e);
            throw new RuntimeException("Failed to initialize Hibernate", e);
        }
    }

    public static UserRepository getInstance() {
        return INSTANCE;
    }

    public UserJpaEntity save(UserJpaEntity userJpaEntity) {
        executeWithTransaction(em -> {
            if (userJpaEntity.getId() == null) {
                em.persist(userJpaEntity);
            } else {
                em.merge(userJpaEntity);
            }
        }, "save user");
        return userJpaEntity;
    }

    public UserJpaEntity findByEmail(String email) {
        return execute(em -> {
            try {
                return em.createNamedQuery("User.findByEmail", UserJpaEntity.class)
                        .setParameter("email", email)
                        .getSingleResult();
            } catch (NoResultException e) {
                log.debug("No user found with email: {}", email);
                return null;
            }
        }, "find user by email");
    }

    public UserJpaEntity findByPassword(String password) {
        return execute(em -> {
            try {
                return em.createNamedQuery("User.findByPassword", UserJpaEntity.class)
                        .setParameter("password", password)
                        .getSingleResult();
            } catch (NoResultException e) {
                log.debug("No user found with password: {}", password);
                return null;
            }
        }, "find user by password");
    }

    // 트랜잭션 실행 공통 메서드
    private void executeWithTransaction(Consumer<EntityManager> action, String operation) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                action.accept(em);
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                handleException("Failed to " + operation, e);
            }
        } catch (Exception e) {
            handleException("Failed to open EntityManager for " + operation, e);
        }
    }

    // 조회 실행 공통 메서드
    private <T> T execute(Function<EntityManager, T> action, String operation) {
        try (EntityManager em = emf.createEntityManager()) {
            return action.apply(em);
        } catch (Exception e) {
            handleException("Failed to " + operation, e);
            return null;
        }
    }

    // 예외 처리 공통화
    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new RuntimeException(message, e);
    }

    // 리소스 정리
    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            log.info("EntityManagerFactory closed.");
        }
    }
}