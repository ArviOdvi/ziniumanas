package lt.ziniumanas.nlp;

import ai.djl.Model;
import ai.djl.modality.Classifications;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorFactory;
import ai.djl.util.Pair;
import lt.ziniumanas.config.NlpModelProperties;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class TextTranslatorFactory implements TranslatorFactory {
    private final List<String> classes;
    private final NlpModelProperties properties;

    public TextTranslatorFactory(List<String> classes, NlpModelProperties properties) {
        this.classes = classes;
        this.properties = properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I, O> Translator<I, O> newInstance(Class<I> input, Class<O> output, Model model, Map<String, ?> arguments) {
        return (Translator<I, O>) new TextClassificationTranslator(classes, properties);
    }

    @Override
    public Set<Pair<Type, Type>> getSupportedTypes() {
        return Collections.singleton(new Pair<>(String.class, Classifications.class));
    }
}
