package lime1st.netty.service;

import lime1st.netty.server.service.ApiRequest;
import lime1st.netty.server.service.ServiceDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ServiceDispatcherTest {

    private ApiRequest mockApiRequest;

    @BeforeEach
    void setUp() {
        ApplicationContext mockContext = Mockito.mock(ApplicationContext.class);
        mockApiRequest = Mockito.mock(ApiRequest.class);
        ServiceDispatcher sd = new ServiceDispatcher();
        sd.init(mockContext);
        when(mockContext.getBean(any(String.class), any(Map.class))).thenReturn(mockApiRequest);
    }

    @Test
    void testDispatchTokenGet() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("REQUEST_URI", "/tokens");
        requestMap.put("REQUEST_METHOD", "GET");

        ApiRequest result = ServiceDispatcher.dispatch(requestMap);
        assertThat(result).isEqualTo(mockApiRequest);
    }

    @Test
    void testDispatchUsers() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("REQUEST_URI", "/users");

        ApiRequest result = ServiceDispatcher.dispatch(requestMap);
        assertThat(result).isEqualTo(mockApiRequest);
    }

    @Test
    void testDispatchNotFound() {
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("REQUEST_URI", "/invalid");

        ApiRequest result = ServiceDispatcher.dispatch(requestMap);
        assertThat(result).isEqualTo(mockApiRequest);
    }

    @Test
    void testDispatchNullUri() {
        Map<String, String> requestMap = new HashMap<>();

        ApiRequest result = ServiceDispatcher.dispatch(requestMap);
        assertThat(result).isEqualTo(mockApiRequest);
    }
}