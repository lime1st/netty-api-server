package lime1st.netty.user.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users")
@NamedQueries({
        @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM UserJpaEntity u WHERE u.email = :email"),
        @NamedQuery(name = "User.findByPassword", query = "SELECT u FROM UserJpaEntity u WHERE u.password = :password")
})
public class UserJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String email;
    private String name;
    private String password;

    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 생성자 (JPA 를 위해 필요)
    public UserJpaEntity() {
    }

    // 생성자 (빌더에서 사용)
    private UserJpaEntity(Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.name = builder.name;
        this.password = builder.password;
    }

    // Auditing 메서드
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
//        this.createdBy = getCurrentUser(); 필요 시 추가
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

//    private String getCurrentUser() {
//        return "system";
//    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserJpaEntity userJpaEntity)) return false;
        return Objects.equals(id, userJpaEntity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    // 빌더 클래스
    public static class Builder {
        private Long id;
        private String email;
        private String name;
        private String password;

        Builder id(Long id) {
            this.id = id;
            return this;
        }

        Builder name(String name) {
            this.name = name;
            return this;
        }

        Builder email(String email) {
            this.email = email;
            return this;
        }

        Builder password(String password) {
            this.password = password;
            return this;
        }

        UserJpaEntity build() {
            return new UserJpaEntity(this);
        }
    }

    // 빌더 객체를 반환하는 정적 메서드
    public static Builder builder() {
        return new Builder();
    }
}

