package lime1st.netty.user.application.port.out;

import lime1st.netty.user.application.dto.in.CreateUserCommand;
import reactor.core.publisher.Mono;

public interface SaveUserPort {

    Mono<Long> saveUser(CreateUserCommand command);
}
