package lt.ziniumanas.nlp;

import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TextVectorizer {
    private static final Logger log = LoggerFactory.getLogger(TextVectorizer.class);

    public static ParagraphVectors trainModel(Map<String, String> textsWithLabels, String modelSavePath,
                                              int minWordFrequency, int layerSize, int epochs,
                                              double learningRate, int windowSize) {
        if (textsWithLabels.isEmpty()) {
            throw new IllegalArgumentException("Įvesties žemėlapis negali būti tuščias");
        }

        for (Map.Entry<String, String> entry : textsWithLabels.entrySet()) {
            if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                throw new IllegalArgumentException("Tekstas negali būti tuščias: " + entry.getKey());
            }
        }

        // Sukuriame dokumentus ir etiketes
        List<LabelledDocument> documents = new ArrayList<>();
        Set<String> allLabels = new HashSet<>();
        for (Map.Entry<String, String> entry : textsWithLabels.entrySet()) {
            LabelledDocument doc = new LabelledDocument();
            doc.setContent(entry.getValue());
            String label = entry.getKey();
            doc.addLabel(label);
            documents.add(doc);
            allLabels.add(label);
        }

        log.info("Sukurta {} dokumentų treniravimui su {} unikaliomis etiketėmis", documents.size(), allLabels.size());

        // Sukuriame iteratorų ir tokenizatorių
        LabelAwareIterator iterator = new SimpleLabelAwareIterator(documents.iterator(), allLabels);
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();

        // Loguojame pirmojo dokumento žetonus
        if (!documents.isEmpty()) {
            String firstDocContent = documents.get(0).getContent();
            List<String> tokens = tokenizerFactory.create(firstDocContent).getTokens();
            log.info("Pirmojo dokumento žetonai: {}", tokens);
        }

        // Konfigūruojame ParagraphVectors
        ParagraphVectors paragraphVectors;
        try {
            paragraphVectors = new ParagraphVectors.Builder()
                    .iterate(iterator)
                    .tokenizerFactory(tokenizerFactory)
                    .minWordFrequency(minWordFrequency)
                    .layerSize(layerSize)
                    .epochs(epochs)
                    .learningRate(learningRate)
                    .windowSize(windowSize)
                    .build();
        } catch (Exception e) {
            log.error("Nepavyko sukonfigūruoti ParagraphVectors: {}", e.getMessage());
            throw new RuntimeException("ParagraphVectors konfigūracijos klaida", e);
        }

        // Apmokame modelį
        long startTime = System.currentTimeMillis();
        try {
            paragraphVectors.fit();
        } catch (Exception e) {
            log.error("ParagraphVectors treniravimo klaida: {}", e.getMessage());
            throw new RuntimeException("Nepavyko apmokyti ParagraphVectors modelio", e);
        }

        // Tikriname modelio žodyną
        int vocabSize = 0;
        try {
            vocabSize = paragraphVectors.vocab().numWords();
            log.info("ParagraphVectors mokymas baigtas per {} ms, žodyno dydis: {}, dokumentų skaičius: {}",
                    System.currentTimeMillis() - startTime, vocabSize, documents.size());
            if (vocabSize == 0) {
                throw new IllegalStateException("ParagraphVectors modelio žodynas yra tuščias. Patikrinkite treniravimo duomenis arba sumažinkite minWordFrequency.");
            }
        } catch (Exception e) {
            log.warn("Nepavyko patikrinti žodyno dydžio: {}. Tęsiame be žodyno patikrinimo.", e.getMessage());
        }

        // Išsaugome modelį
        try {
            File modelFile = new File(modelSavePath);
            if (!modelFile.getParentFile().exists() && !modelFile.getParentFile().mkdirs()) {
                throw new IOException("Nepavyko sukurti katalogo: " + modelFile.getParentFile().getAbsolutePath());
            }
            WordVectorSerializer.writeParagraphVectors(paragraphVectors, modelFile);
            log.info("ParagraphVectors modelis išsaugotas į {}", modelFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Nepavyko išsaugoti ParagraphVectors modelio: " + e.getMessage(), e);
        }

        return paragraphVectors;
    }

    public static ParagraphVectors loadModel(String modelPath) {
        try {
            File modelFile = new File(modelPath);
            if (!modelFile.exists()) {
                throw new IllegalArgumentException("Modelio failas neegzistuoja: " + modelPath);
            }
            ParagraphVectors model = WordVectorSerializer.readParagraphVectors(modelFile);
            try {
                int vocabSize = model.vocab().numWords();
                log.info("ParagraphVectors modelis įkeltas iš {}, žodyno dydis: {}", modelPath, vocabSize);
            } catch (Exception e) {
                log.warn("Nepavyko patikrinti įkelto modelio žodyno dydžio: {}", e.getMessage());
            }
            return model;
        } catch (IOException e) {
            throw new RuntimeException("Nepavyko įkelti ParagraphVectors modelio: " + e.getMessage(), e);
        }
    }

    public static INDArray vectorize(List<String> texts, ParagraphVectors model) {
        if (texts.isEmpty()) {
            throw new IllegalArgumentException("Įvesties tekstai negali būti tušti");
        }
        if (model == null) {
            throw new IllegalArgumentException("ParagraphVectors modelis negali būti null");
        }

        // Patikriname modelio žodyną
        int vocabSize = 0;
        try {
            vocabSize = model.vocab().numWords();
            if (vocabSize == 0) {
                throw new IllegalStateException("ParagraphVectors modelio žodynas yra tuščias. Treniruokite modelį su tinkamais duomenimis.");
            }
        } catch (Exception e) {
            log.warn("Nepavyko patikrinti modelio žodyno dydžio: {}. Tęsiame be žodyno patikrinimo.", e.getMessage());
        }

        // Validuojame tekstus
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            if (text == null || text.trim().isEmpty()) {
                log.warn("Tuščias arba null tekstas indeksu {}. Naudojamas tuščias tekstas.", i);
                texts.set(i, ""); // Užtikriname, kad tekstas nebūtų null
            }
        }

        int vectorSize = model.getLayerSize();
        INDArray features = Nd4j.zeros(texts.size(), vectorSize);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            try {
                INDArray vector = model.inferVector(text);
                if (vector == null || vector.isEmpty()) {
                    log.warn("Nepavyko sukurti vektoriaus tekstui indeksu {}: '{}'. Naudojamas nulinis vektorius.", i, text);
                    features.putRow(i, Nd4j.zeros(vectorSize));
                } else {
                    features.putRow(i, vector);
                }
            } catch (Exception e) {
                log.error("Klaida vektorizuojant tekstą indeksu {}: '{}'. Klaida: {}. Naudojamas nulinis vektorius.",
                        i, text, e.getMessage());
                features.putRow(i, Nd4j.zeros(vectorSize)); // Naudojame nulinį vektorių klaidos atveju
            }
        }
        log.info("Vektorizuota {} tekstų per {} ms, forma: {}",
                texts.size(), System.currentTimeMillis() - startTime, Arrays.toString(features.shape()));

        return features;
    }
}