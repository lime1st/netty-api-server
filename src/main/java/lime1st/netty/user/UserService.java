package lime1st.netty.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void save(Users user) {
        repository.save(user);
    }

    public Users findById(Long id) {
        return repository.findOne(id);
    }

    public Users findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Users findByPassword(String password) {
        return repository.findByPassword(password);
    }

    public List<Users> findAll() {
        return repository.findAll();
    }
}
