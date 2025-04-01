package lime1st.netty.user.adapter.out.persistence;

import lime1st.netty.user.application.dto.in.CreateUserCommand;
import lime1st.netty.user.application.dto.out.FindUserQuery;
import lime1st.netty.user.application.port.out.LoadUserPort;
import lime1st.netty.user.application.port.out.SaveUserPort;

public class UserPersistenceAdapter implements SaveUserPort, LoadUserPort {

    private final UserRepository userRepository = UserRepository.getInstance();
    private final UserMapper userMapper = new UserMapper();

    @Override
    public long saveUser(CreateUserCommand command) {
        UserJpaEntity savedUser = userRepository.save(userMapper.mapToJpaEntityFrom(command));
        return savedUser.getId();
    }

    @Override
    public FindUserQuery loadUserByEmail(String email) {
        UserJpaEntity loadUser = userRepository.findByEmail(email);
        return userMapper.mapToQueryFrom(loadUser);
    }

    @Override
    public FindUserQuery loadUserByPassword(String password) {
        UserJpaEntity loadUser = userRepository.findByPassword(password);
        return userMapper.mapToQueryFrom(loadUser);
    }

    public void close() {
        userRepository.close();
    }
}
