package lt.ziniumanas.service.aiservice;

import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.ZooModel;
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
    private final MultiLayerNetwork model;
    private final ParagraphVectors paragraphVectors;
    private final List<String> classes = Arrays.asList("Sportas", "Ekonomika", "Politika");

    @Value("${model.save.path:models/ArticleCategorizationAImodel.zip}")
    private String modelSavePath;

    @Value("${paragraph.vectors.save.path:models/paragraph_vectors.zip}")
    private String paragraphVectorsSavePath;

    public ArticleCategorizationServicebyAI() {
        MultiLayerNetwork loadedModel = null;
        ParagraphVectors loadedParagraphVectors = null;

        // Patikriname modelSavePath
        if (modelSavePath == null || modelSavePath.trim().isEmpty()) {
            logger.error("model.save.path nėra nustatytas arba yra tuščias");
            this.model = null;
            this.paragraphVectors = null;
            return;
        }

        // Patikriname paragraphVectorsSavePath
        if (paragraphVectorsSavePath == null || paragraphVectorsSavePath.trim().isEmpty()) {
            logger.error("paragraph.vectors.save.path nėra nustatytas arba yra tuščias");
            this.model = null;
            this.paragraphVectors = null;
            return;
        }

        // Įkeliame neuroninį tinklą
        File modelFile = new File(modelSavePath);
        if (modelFile.exists() && modelFile.canRead()) {
            try {
                loadedModel = ModelSerializer.restoreMultiLayerNetwork(modelFile);
                logger.info("Neuroninis tinklas sėkmingai įkeltas iš {}", modelSavePath);
            } catch (IOException e) {
                logger.error("Klaida įkeliant neuroninį tinklą iš '{}': {}", modelSavePath, e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Netikėta klaida įkeliant neuroninį tinklą iš '{}': {}", modelSavePath, e.getMessage(), e);
            }
        } else {
            logger.warn("Neuroninio tinklo failas '{}' nerastas arba neįskaitomas. Reikalingas modelio treniravimas.", modelSavePath);
        }

        // Įkeliame ParagraphVectors
        File pvFile = new File(paragraphVectorsSavePath);
        if (pvFile.exists() && pvFile.canRead()) {
            try {
                loadedParagraphVectors = TextVectorizer.loadModel(paragraphVectorsSavePath);
                logger.info("ParagraphVectors sėkmingai įkeltas iš {}", paragraphVectorsSavePath);
            } catch (Exception e) {
                logger.error("Klaida įkeliant ParagraphVectors iš '{}': {}", paragraphVectorsSavePath, e.getMessage(), e);
            }
        } else {
            logger.warn("ParagraphVectors failas '{}' nerastas arba neįskaitomas. Reikalingas modelio treniravimas.", paragraphVectorsSavePath);
        }

        this.model = loadedModel;
        this.paragraphVectors = loadedParagraphVectors;
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