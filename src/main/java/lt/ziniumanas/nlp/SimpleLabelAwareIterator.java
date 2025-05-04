package lt.ziniumanas.nlp;

import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.documentiterator.LabelsSource;

import java.util.*;

public class SimpleLabelAwareIterator implements LabelAwareIterator {
    private final Iterator<LabelledDocument> iterator;
    private final LabelsSource labelsSource;

    public SimpleLabelAwareIterator(Iterator<LabelledDocument> documents, Set<String> labels) {
        if (labels == null) {
            throw new IllegalArgumentException("Labels cannot be null");
        }
        this.iterator = documents;
        // Naudojame DL4J pateiktą LabelsSource klasę
        this.labelsSource = new LabelsSource(new java.util.ArrayList<>(labels));
    }

    @Override
    public boolean hasNextDocument() {
        return iterator.hasNext();
    }

    @Override
    public LabelledDocument nextDocument() {
        return iterator.next();
    }

    @Override
    public void reset() {
        // Jei reikia resetinti ir LabelsSource, galite tai padaryti čia
        this.labelsSource.reset();
        throw new UnsupportedOperationException("Reset not fully implemented");
    }

    @Override
    public void shutdown() {}

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
