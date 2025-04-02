package lime1st.netty.user.adapter.out.persistence;

import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lime1st.netty.user.application.dto.in.CreateUserCommand;
import lime1st.netty.user.application.dto.out.FindUserQuery;
import lime1st.netty.user.application.port.out.ReadUserPort;
import lime1st.netty.user.application.port.out.CreateUserPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;

public class UserR2dbcAdapter implements CreateUserPort, ReadUserPort {

    private static final Logger log = LoggerFactory.getLogger(UserR2dbcAdapter.class);
    private final ConnectionFactory connectionFactory;

    public UserR2dbcAdapter(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;

        // 테이블 초기화
        initializeDatabase().subscribe();
    }

    @Override
    public Mono<Long> createUser(CreateUserCommand command) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement("INSERT INTO users (email, name, password) VALUES ($1, $2, $3)")
                                        .bind("$1", command.email())
                                        .bind("$2", command.name())
                                        .bind("$3", command.password())
                                        .returnGeneratedValues("id")
                                        .execute())
                                .flatMap(result ->
                                        Mono.from(result.map((row, metadata) ->
                                                row.get("id", Long.class))))
                                .doFinally(signal -> connection.close()));
    }

    @Override
    public Mono<FindUserQuery> readUserById(String d) {
        long userId = Long.parseLong(d);
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement("SELECT * FROM users WHERE id = $1")
                                        .bind("$1", userId)
                                        .execute())
                                .flatMap(result ->
                                        Mono.from(result.map(getFindUserQueryBiFunction())))
                                .doOnNext(query -> log.info("Found user by id '{}': {}", userId, query))
                                .doOnError(e -> log.error("Error reading user by id '{}': {}", userId, e.getMessage()))
                                .doFinally(signal -> connection.close())
                                .switchIfEmpty(Mono.empty()));
    }

    @Override
    public Mono<FindUserQuery> readUserByEmail(String email) {
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement("SELECT * FROM users WHERE email = $1")
                                        .bind("$1", email)
                                        .execute())
                                .flatMap(result ->
                                        Mono.from(result.map(getFindUserQueryBiFunction())))
                                .doOnNext(query -> log.info("Found user by email '{}': {}", email, query))
                                .doOnError(e -> log.error("Error reading user by email '{}': {}", email, e.getMessage()))
                                .doFinally(signal -> connection.close())
                                .switchIfEmpty(Mono.empty()));
    }

    public void close() {
        // R2DBC 는 ConnectionFactory 자체를 종료할 필요 없음
    }

    private BiFunction<Row, RowMetadata, FindUserQuery> getFindUserQueryBiFunction() {
        return (row, metadata) ->
                new FindUserQuery(
                        row.get("id", Long.class),
                        row.get("name", String.class),
                        row.get("email", String.class),
                        row.get("password", String.class));
    }

    private Mono<Void> initializeDatabase() {
        return Mono.from(connectionFactory.create())
                .flatMap(connection ->
                        Mono.from(connection.createStatement(
                                                "CREATE TABLE IF NOT EXISTS users (" +
                                                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                                                        "email VARCHAR(255), " +
                                                        "name VARCHAR(255), " +
                                                        "password VARCHAR(255))")
                                        .execute())
                                .then(Mono.just(connection))
                                .doOnSuccess(conn -> log.info("Database initialized"))
                                .doFinally(signal -> connection.close())
                                .then());
    }
}

