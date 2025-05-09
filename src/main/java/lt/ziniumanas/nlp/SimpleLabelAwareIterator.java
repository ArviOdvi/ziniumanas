package lt.ziniumanas.nlp;

import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.LabelsSource;

import java.util.*;

public class SimpleLabelAwareIterator implements LabelAwareIterator {
    private final List<LabelledDocument> documents;
    private final LabelsSource labelsSource;
    private Iterator<LabelledDocument> currentIterator;

    public SimpleLabelAwareIterator(Iterator<LabelledDocument> documents, Set<String> labels) {
        if (labels == null) {
            throw new IllegalArgumentException("Labels cannot be null");
        }
        if (documents == null) {
            throw new IllegalArgumentException("Documents iterator cannot be null");
        }

        // Saugome dokumentus sąraše, kad galėtume resetinti
        this.documents = new ArrayList<>();
        documents.forEachRemaining(this.documents::add);
        this.labelsSource = new LabelsSource(new ArrayList<>(labels));
        this.currentIterator = this.documents.iterator();
    }

    @Override
    public boolean hasNextDocument() {
        return currentIterator.hasNext();
    }

    @Override
    public LabelledDocument nextDocument() {
        return currentIterator.next();
    }

    @Override
    public void reset() {
        // Grąžiname iteratorių į pradžią
        this.currentIterator = documents.iterator();
        // Resetiname LabelsSource, jei reikia
        this.labelsSource.reset();
    }

    @Override
    public void shutdown() {
        // Nereikia specialaus uždarymo
    }

    @Override
    public boolean hasNext() {
        return hasNextDocument();
    }

    @Override
    public LabelledDocument next() {
        return nextDocument();
    }

    @Override
    public LabelsSource getLabelsSource() {
        return labelsSource;
    }
}
