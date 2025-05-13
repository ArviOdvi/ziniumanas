package lt.ziniumanas.nlp;
// Pagalbinė klasė treniravimo duomenims
import ai.djl.Model;
import ai.djl.metric.Metrics;
import ai.djl.modality.Classifications;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.nn.Block;
import ai.djl.training.dataset.Record;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.Progress;
import ai.djl.training.dataset.ArrayDataset;
import java.util.List;

import ai.djl.training.dataset.RandomAccessDataset;


public class TextClassificationDataset extends RandomAccessDataset {

    private final List<String> texts;
    private final List<String> labels;
    private final List<String> uniqueLabels;
    private final Translator<String, Classifications> translator;

    public TextClassificationDataset(List<String> texts, List<String> labels, List<String> uniqueLabels, int batchSize) {
        super(new ArrayDataset.Builder().setSampling(batchSize, false));
        if (texts == null || labels == null || uniqueLabels == null) {
            throw new IllegalArgumentException("Tekstai, etiketės ir unikalios etiketės negali būti null");
        }
        if (texts.size() != labels.size()) {
            throw new IllegalArgumentException("Tekstų ir etikečių skaičius turi sutapti: " + texts.size() + " vs " + labels.size());
        }
        if (uniqueLabels.isEmpty()) {
            throw new IllegalArgumentException("Unikalių etikečių sąrašas negali būti tuščias");
        }
        for (String label : labels) {
            if (!uniqueLabels.contains(label)) {
                throw new IllegalArgumentException("Etiketė '" + label + "' nėra unikalių etikečių sąraše");
            }
        }

        this.texts = texts;
        this.labels = labels;
        this.uniqueLabels = uniqueLabels;
        this.translator = new TextClassificationTranslator(uniqueLabels);
    }

    @Override
    public Record get(NDManager manager, long index) {
        String text = texts.get((int) index);
        String label = labels.get((int) index);
        NDList data;
        try {
            data = translator.processInput(new TranslatorContext() {
                @Override
                public NDManager getNDManager() {
                    return manager;
                }

                @Override
                public NDManager getPredictorManager() {
                    return null;
                }

                @Override
                public Block getBlock() {
                    return null;
                }

                @Override
                public Metrics getMetrics() {
                    return null;
                }

                @Override
                public Object getAttachment(String s) {
                    return null;
                }

                @Override
                public void setAttachment(String s, Object o) {

                }

                @Override
                public void close() {

                }

                @Override
                public Model getModel() {
                    return null; // Modelis nėra reikalingas tokenizacijai
                }
            }, text);

        } catch (Exception e) {
            throw new RuntimeException("Klaida apdorojant tekstą: " + text, e);
        }
        NDList labelData = new NDList(manager.create(uniqueLabels.indexOf(label)));
        return new Record(data, labelData);
    }

    @Override
    public long size() {
        return texts.size();
    }

    @Override
    public long availableSize() {
        return size();
    }
    protected Batchifier getBatchifier() {
        return Batchifier.STACK;
    }
    @Override
    public void prepare(Progress progress) {
        // Šį metodą galima naudoti pasiruošti papildomus duomenis
    }
}