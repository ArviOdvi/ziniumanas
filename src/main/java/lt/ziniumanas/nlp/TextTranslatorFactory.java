package lt.ziniumanas.nlp;

import ai.djl.Model;
import ai.djl.modality.Classifications;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorFactory;
import ai.djl.util.Pair;
import lt.ziniumanas.config.NlpModelProperties;

import java.lang.reflect.Type;
import java.util.*;

public class TextTranslatorFactory implements TranslatorFactory {
    private final List<String> classes;
    private final NlpModelProperties properties;

    public TextTranslatorFactory(List<String> classes, NlpModelProperties properties) {
        if (classes == null || classes.isEmpty()) {
            throw new IllegalArgumentException("Klasės negali būti null arba tuščios");
        }
        if (properties == null) {
            throw new IllegalArgumentException("NlpModelProperties negali būti null");
        }
        this.classes = Collections.unmodifiableList(new ArrayList<>(classes));
        this.properties = properties;
    }

    @Override
    public <I, O> Translator<I, O> newInstance(Class<I> input, Class<O> output, Model model, Map<String, ?> arguments) {
        if (!input.equals(String.class) || !output.equals(Classifications.class)) {
            throw new IllegalArgumentException("Palaikomi tik String įvestis ir Classifications išvestis");
        }
        return (Translator<I, O>) new TextClassificationTranslator(classes, properties);
    }

    @Override
    public Set<Pair<Type, Type>> getSupportedTypes() {
        return Collections.singleton(new Pair<>(String.class, Classifications.class));
    }
}