package Application.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        OpenAPI openAPI = new OpenAPI();
        openAPI.info(new Info()
                .title("Timetable server API")
                .version("1.0.0")
                .contact(new Contact()
                        .name("SaZha")
                        .email("kaa5843771@yandex.ru"))
        );
        return openAPI;
    }
}
