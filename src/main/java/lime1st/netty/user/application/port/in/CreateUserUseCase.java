package lime1st.netty.user.application.port.in;

import lime1st.netty.user.application.dto.in.CreateUserCommand;
import reactor.core.publisher.Mono;

public interface CreateUserUseCase {

    Mono<Long> createUser(CreateUserCommand command);
}
