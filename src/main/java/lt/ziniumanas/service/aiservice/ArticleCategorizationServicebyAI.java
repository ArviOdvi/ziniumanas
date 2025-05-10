package lt.ziniumanas.service.aiservice;


import jakarta.annotation.PostConstruct;
import lt.ziniumanas.nlp.TextVectorizer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Service
public class ArticleCategorizationServicebyAI {
    private static final Logger logger = LoggerFactory.getLogger(ArticleCategorizationServicebyAI.class);
    private MultiLayerNetwork model;
    private ParagraphVectors paragraphVectors;
    private final List<String> classes = Arrays.asList("Sportas", "Ekonomika", "Politika");

    @Value("${model.save.path:models/ArticleCategorizationAImodel.zip}")
    private String modelSavePath;

    @Value("${paragraph.vectors.save.path:models/paragraph_vectors.zip}")
    private String paragraphVectorsSavePath;

    @PostConstruct
    public void init() {
        // Patikriname modelSavePath
        if (modelSavePath == null || modelSavePath.trim().isEmpty()) {
            logger.error("model.save.path nėra nustatytas arba yra tuščias");
            throw new IllegalStateException("model.save.path nėra nustatytas");
        }

        // Patikriname paragraphVectorsSavePath
        if (paragraphVectorsSavePath == null || paragraphVectorsSavePath.trim().isEmpty()) {
            logger.error("paragraph.vectors.save.path nėra nustatytas arba yra tuščias");
            throw new IllegalStateException("paragraph.vectors.save.path nėra nustatytas");
        }

        // Įkeliame neuroninį tinklą
        File modelFile = new File(modelSavePath);
        if (modelFile.exists() && modelFile.canRead()) {
            try {
                model = ModelSerializer.restoreMultiLayerNetwork(modelFile);
                logger.info("Neuroninis tinklas sėkmingai įkeltas iš {}", modelSavePath);
            } catch (IOException e) {
                logger.error("Klaida įkeliant neuroninį tinklą iš '{}': {}", modelSavePath, e.getMessage(), e);
                throw new RuntimeException("Nepavyko įkelti neuroninio tinklo", e);
            }
        } else {
            logger.error("Neuroninio tinklo failas '{}' nerastas arba neįskaitomas", modelSavePath);
            throw new IllegalStateException("Neuroninio tinklo failas nerastas: " + modelSavePath);
        }

        // Įkeliame ParagraphVectors
        File pvFile = new File(paragraphVectorsSavePath);
        if (pvFile.exists() && pvFile.canRead()) {
            try {
                paragraphVectors = TextVectorizer.loadModel(paragraphVectorsSavePath);
                logger.info("ParagraphVectors sėkmingai įkeltas iš {}", paragraphVectorsSavePath);
            } catch (Exception e) {
                logger.error("Klaida įkeliant ParagraphVectors iš '{}': {}", paragraphVectorsSavePath, e.getMessage(), e);
                throw new RuntimeException("Nepavyko įkelti ParagraphVectors", e);
            }
        } else {
            logger.error("ParagraphVectors failas '{}' nerastas arba neįskaitomas", paragraphVectorsSavePath);
            throw new IllegalStateException("ParagraphVectors failas nerastas: " + paragraphVectorsSavePath);
        }
    }

    public String categorizeArticle(String text) {
        if (model == null || paragraphVectors == null) {
            logger.error("Modelis arba ParagraphVectors neįkeltas, negalima atlikti prognozės");
            return "Modelis neįkeltas";
        }

        if (text == null || text.trim().isEmpty()) {
            logger.warn("Pateiktas tuščias tekstas kategorizavimui");
            return "Klaida: tuščias tekstas";
        }

        try {
            // Vektorizuojame tekstą
            INDArray features = TextVectorizer.vectorize(Arrays.asList(text), paragraphVectors);
            logger.debug("Vektorizuotas tekstas: {}", text);

            // Normalizuojame požymius
            DataNormalization normalizer = new NormalizerMinMaxScaler();
            normalizer.fit(new DataSet(features, features));
            normalizer.transform(features);

            // Atliekame prognozę
            INDArray output = model.output(features);
            int predictedIdx = Nd4j.argMax(output, 1).getInt(0);
            String predictedClass = predictedIdx < classes.size() ? classes.get(predictedIdx) : "Nežinoma";
            logger.info("Prognozuota kategorija tekstui '{}': {}", text, predictedClass);
            return predictedClass;
        } catch (Exception e) {
            logger.error("Klaida prognozuojant kategoriją tekstui '{}': {}", text, e.getMessage(), e);
            return "Klaida";
        }
    }
}