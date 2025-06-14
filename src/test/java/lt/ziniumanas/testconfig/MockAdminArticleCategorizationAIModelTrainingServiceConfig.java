package lt.ziniumanas.testconfig;

import lt.ziniumanas.service.adminservice.AdminArticleCategorizationAIModelTrainingService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MockAdminArticleCategorizationAIModelTrainingServiceConfig {
    @Bean
    public AdminArticleCategorizationAIModelTrainingService trainingService() {
        return Mockito.mock(AdminArticleCategorizationAIModelTrainingService.class);
    }
}
