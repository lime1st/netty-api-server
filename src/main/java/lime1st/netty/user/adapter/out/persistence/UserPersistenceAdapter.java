package lime1st.netty.user.adapter.out.persistence;

import lime1st.netty.user.application.dto.command.CreateUserCommand;
import lime1st.netty.user.application.dto.query.FindUserQuery;
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
        UserJpaEntity findUser = userRepository.findByEmail(email);
        return userMapper.mapToQueryFrom(findUser);
    }
}
