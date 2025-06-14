package lt.ziniumanas.controller;

import lt.ziniumanas.dto.ArticleCategorizationAIModelTrainingDto;
import lt.ziniumanas.service.adminservice.AdminArticleCategorizationAIModelTrainingService;
import lt.ziniumanas.testconfig.MockAdminArticleCategorizationAIModelTrainingServiceConfig;
import lt.ziniumanas.testconfig.TestSecurityConfig;
import lt.ziniumanas.util.HttpEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminArticleCategorizationAIModelTrainingController.class)
@Import({
        MockAdminArticleCategorizationAIModelTrainingServiceConfig.class,
        TestSecurityConfig.class
})
public class AdminArticleCategorizationAIModelTrainingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminArticleCategorizationAIModelTrainingService trainingService;

    @Test
    void testShowTrainingPageWithNoData() throws Exception {
        when(trainingService.getTrainingDataCount()).thenReturn(0L);
        when(trainingService.getValidCategories()).thenReturn(List.of("Politika", "Verslas"));

        mockMvc.perform(get(HttpEndpoint.ADMIN_AI_TRAINING))
                .andExpect(status().isOk())
                .andExpect(view().name(HttpEndpoint.VIEW_AI_TRAINING))
                .andExpect(model().attributeExists("trainingDto", "categories", "count", "message", "messageType"));
    }

    @Test
    void testShowTrainingData() throws Exception {
        when(trainingService.getAllTrainingData()).thenReturn(List.of());
        when(trainingService.getValidCategories()).thenReturn(List.of("Mokslas", "Sportas"));

        mockMvc.perform(get(HttpEndpoint.ADMIN_AI_TRAINING_DATA))
                .andExpect(status().isOk())
                .andExpect(view().name(HttpEndpoint.VIEW_AI_TRAINING_DATA))
                .andExpect(model().attributeExists("trainingData", "categories"));
    }

    @Test
    void testTrainingDataInfo() throws Exception {
        when(trainingService.getTrainingDataCount()).thenReturn(123L);

        mockMvc.perform(get(HttpEndpoint.ADMIN_AI_TRAINING_DATA_INFO))
                .andExpect(status().isOk())
                .andExpect(content().string("Šiuo metu treniravimo duomenų bazėje yra 123 įrašų."));
    }

    @Test
    void testShowMetrics() throws Exception {
        when(trainingService.getTestMetrics()).thenReturn(Map.of("accuracy", 0.95));

        mockMvc.perform(get(HttpEndpoint.ADMIN_AI_TRAINING_METRICS))
                .andExpect(status().isOk())
                .andExpect(view().name(HttpEndpoint.VIEW_AI_TRAINING_METRICS))
                .andExpect(model().attributeExists("metrics"));
    }

    @Test
    void testTrainModelDirectlySuccess() throws Exception {
        doNothing().when(trainingService).trainModel();

        mockMvc.perform(post(HttpEndpoint.ADMIN_AI_TRAINING_TRAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("Modelis sėkmingai ištreniruotas."));
    }

    @Test
    void testTrainModelDirectlyError() throws Exception {
        doThrow(new RuntimeException("Treniravimo klaida")).when(trainingService).trainModel();

        mockMvc.perform(post(HttpEndpoint.ADMIN_AI_TRAINING_TRAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("Klaida: Treniravimo klaida"));
    }

    @Test
    void testAddTrainingData_InvalidInput_ShouldReturnFormWithErrors() throws Exception {
        mockMvc.perform(post(HttpEndpoint.ADMIN_AI_TRAINING)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("text", "")
                        .param("label", ""))
                .andExpect(status().isOk())
                .andExpect(view().name(HttpEndpoint.VIEW_AI_TRAINING))
                .andExpect(model().attributeHasFieldErrors("trainingDto", "text", "label"))
                .andExpect(model().attributeExists("categories", "message", "messageType"));
    }

    @Test
    void testAddTrainingData_ValidInput_ShouldRedirectAndCallService() throws Exception {
        reset(trainingService); // svarbu – kad būtų švarus mock’as

        doNothing().when(trainingService).handleTrainingData(any(ArticleCategorizationAIModelTrainingDto.class));

        mockMvc.perform(post(HttpEndpoint.ADMIN_AI_TRAINING)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("text", "Tai yra straipsnio tekstas")
                        .param("label", "Mokslas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(HttpEndpoint.ADMIN_AI_TRAINING));

        verify(trainingService, times(1)).handleTrainingData(any());
    }

    @Test
    void testAddTrainingData_ValidInput_ShouldIncludeFlashMessage() throws Exception {
        doNothing().when(trainingService).handleTrainingData(any(ArticleCategorizationAIModelTrainingDto.class));

        mockMvc.perform(post(HttpEndpoint.ADMIN_AI_TRAINING)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("text", "Tai yra straipsnis")
                        .param("label", "Verslas"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(HttpEndpoint.ADMIN_AI_TRAINING))
                .andExpect(flash().attribute("message", "Įrašas pridėtas!"))
                .andExpect(flash().attribute("messageType", "success"));
    }

}