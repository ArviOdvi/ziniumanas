package lt.ziniumanas.nlp;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.wordstore.inmemory.AbstractCache;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.SimpleLabelAwareIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TextVectorizer {
    private static final Logger logger = LoggerFactory.getLogger(TextVectorizer.class);
    private static TokenizerFactory tokenizerFactory;

    static {
        // Inicializuojame TokenizerFactory statiniame bloke
        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
        logger.info("TokenizerFactory inicializuotas: {}", tokenizerFactory.getClass().getSimpleName());
    }

    public static ParagraphVectors trainModel(Map<String, String> textsWithLabels, String savePath,
                                              int minWordFrequency, int layerSize, int epochs,
                                              double learningRate, int windowSize) {
        if (textsWithLabels == null || textsWithLabels.isEmpty()) {
            logger.error("Tuščias tekstų ir etikečių žemėlapis");
            throw new IllegalArgumentException("Tekstų ir etikečių žemėlapis negali būti tuščias");
        }

        // Sukuriame LabelledDocument sąrašą
        List<LabelledDocument> documents = new ArrayList<>();
        for (Map.Entry<String, String> entry : textsWithLabels.entrySet()) {
            if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                logger.warn("Praleistas tuščias tekstas su etikete: {}", entry.getKey());
                continue;
            }
            LabelledDocument document = new LabelledDocument();
            document.setContent(entry.getValue());
            document.addLabel(entry.getKey());
            documents.add(document);
        }

        if (documents.isEmpty()) {
            logger.error("Nėra galiojančių dokumentų treniravimui");
            throw new IllegalArgumentException("Nėra galiojančių dokumentų treniravimui");
        }

        // Sukuriame iterator'į
        LabelAwareIterator iterator = new SimpleLabelAwareIterator(documents);

        // Konfigūruojame ParagraphVectors
        ParagraphVectors paragraphVectors = new ParagraphVectors.Builder()
                .minWordFrequency(minWordFrequency)
                .layerSize(layerSize)
                .epochs(epochs)
                .learningRate(learningRate)
                .windowSize(windowSize)
                .iterate(iterator)
                .tokenizerFactory(tokenizerFactory)
                .build();

        // Treniruojame modelį
        try {
            logger.info("Pradedamas ParagraphVectors treniravimas su {} dokumentais", documents.size());
            paragraphVectors.fit();
            logger.info("ParagraphVectors treniravimas baigtas");

            // Išsaugome modelį
            File saveFile = new File(savePath);
            if (!saveFile.getParentFile().exists() && !saveFile.getParentFile().mkdirs()) {
                throw new RuntimeException("Nepavyko sukurti katalogo: " + saveFile.getParentFile().getAbsolutePath());
            }
            WordVectorSerializer.writeParagraphVectors(paragraphVectors, saveFile);
            logger.info("ParagraphVectors modelis išsaugotas į {}", savePath);
        } catch (Exception e) {
            logger.error("Klaida treniruojant ParagraphVectors: {}", e.getMessage(), e);
            throw new RuntimeException("ParagraphVectors treniravimo klaida", e);
        }

        return paragraphVectors;
    }

    public static ParagraphVectors loadModel(String path) {
        try {
            logger.info("Įkeliamas ParagraphVectors modelis iš {}", path);
            return WordVectorSerializer.readParagraphVectors(path);
        } catch (Exception e) {
            logger.error("Klaida įkeliant ParagraphVectors modelį: {}", e.getMessage(), e);
            throw new RuntimeException("Nepavyko įkelti ParagraphVectors modelio", e);
        }
    }

    public static INDArray vectorize(List<String> texts, ParagraphVectors paragraphVectors) {
        if (texts == null || texts.isEmpty()) {
            logger.warn("Tuščias tekstų sąrašas, grąžinamas nulinis vektorius");
            return Nd4j.zeros(1, paragraphVectors.getLayerSize());
        }

        List<String> validTexts = new ArrayList<>();
        for (String text : texts) {
            if (text != null && !text.trim().isEmpty()) {
                validTexts.add(text);
            }
        }

        if (validTexts.isEmpty()) {
            logger.warn("Nėra galiojančių tekstų vektorizavimui");
            return Nd4j.zeros(1, paragraphVectors.getLayerSize());
        }

        try {
            INDArray features = Nd4j.zeros(validTexts.size(), paragraphVectors.getLayerSize());
            for (int i = 0; i < validTexts.size(); i++) {
                String text = validTexts.get(i);
                try {
                    // Vektorizuojame tekstą
                    INDArray vector = paragraphVectors.inferVector(text);
                    features.putRow(i, vector);
                } catch (Exception e) {
                    logger.error("Klaida vektorizuojant tekstą indeksu {}: '{}'. Klaida: {}", i, text, e.getMessage(), e);
                    features.putRow(i, Nd4j.zeros(paragraphVectors.getLayerSize()));
                }
            }
            logger.info("Vektorizuota {} tekstų iš {}", validTexts.size(), texts.size());
            return features;
        } catch (Exception e) {
            logger.error("Bendroji vektorizacijos klaida: {}", e.getMessage(), e);
            throw new RuntimeException("Nepavyko vektorizuoti tekstų", e);
        }
    }
}