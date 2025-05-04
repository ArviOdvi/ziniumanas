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
import java.util.*;

public class TextVectorizer {
    private static final Logger log = LoggerFactory.getLogger(TextVectorizer.class);

    public static ParagraphVectors trainModel(Map<String, String> textsWithLabels, String modelSavePath) {
        if (textsWithLabels.isEmpty()) {
            throw new IllegalArgumentException("Įvesties žemėlapis negali būti tuščias");
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

        // Sukuriame iteratorų ir tokenizatorių
        LabelAwareIterator iterator = new SimpleLabelAwareIterator(documents.iterator(), allLabels);
        TokenizerFactory tokenizerFactory = new DefaultTokenizerFactory();

        // Konfigūruojame ParagraphVectors
        ParagraphVectors paragraphVectors = new ParagraphVectors.Builder()
                .iterate(iterator)
                .tokenizerFactory(tokenizerFactory)
                .minWordFrequency(5) // Ignoruoja retus žodžius
                .layerSize(100) // Vektoriaus dydis
                .epochs(10) // Mokymo epochos
                .learningRate(0.025) // Mokymosi greitis
                .windowSize(5) // Konteksto lango dydis
                .build();

        // Apmokame modelį
        paragraphVectors.fit();
        log.info("ParagraphVectors mokymas baigtas su {} dokumentais", documents.size());

        // Išsaugome modelį
        try {
            File modelFile = new File(modelSavePath);
            if (!modelFile.getParentFile().mkdirs() && !modelFile.getParentFile().exists()) {
                throw new RuntimeException("Nepavyko sukurti katalogo: " + modelFile.getParentFile().getAbsolutePath());
            }
            log.info("Katalogas modeliui paruoštas: {}", modelFile.getParentFile().getAbsolutePath());
            WordVectorSerializer.writeParagraphVectors(paragraphVectors, modelFile);
            log.info("ParagraphVectors modelis išsaugotas į {}", modelFile.getAbsolutePath());
        } catch (Exception e) {
            throw new RuntimeException("Nepavyko išsaugoti ParagraphVectors modelio", e);
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
            log.info("ParagraphVectors modelis įkeltas iš {}", modelPath);
            return model;
        } catch (Exception e) {
            throw new RuntimeException("Nepavyko įkelti ParagraphVectors modelio", e);
        }
    }

    public static INDArray vectorize(List<String> texts, ParagraphVectors model) {
        if (texts.isEmpty()) {
            throw new IllegalArgumentException("Įvesties tekstai negali būti tušti");
        }
        if (model == null) {
            throw new IllegalArgumentException("ParagraphVectors modelis negali būti null");
        }

        int vectorSize = model.getLayerSize();
        INDArray features = Nd4j.zeros(texts.size(), vectorSize);

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            INDArray vector = model.inferVector(text);
            features.putRow(i, vector);
        }

        log.info("Vektorizuota {} tekstų į INDArray formą {}", texts.size(), Arrays.toString(features.shape()));
        return features;
    }
}