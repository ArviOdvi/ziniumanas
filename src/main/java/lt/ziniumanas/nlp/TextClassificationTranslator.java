package lt.ziniumanas.nlp;
// Translatorius tekstų klasifikavimui
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.modality.Classifications;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TextClassificationTranslator implements Translator<String, Classifications> {
    private final List<String> classes;
    private final HuggingFaceTokenizer tokenizer;

    public TextClassificationTranslator(List<String> classes) {
        this.classes = classes;
        this.tokenizer = HuggingFaceTokenizer.newInstance("distilbert-base-uncased");
    }

    @Override
    public NDList processInput(TranslatorContext ctx, String input) {
        NDManager manager = ctx.getNDManager();
        var encoded = tokenizer.encode(input);
        long[] inputIds = encoded.getIds();
        long[] attentionMask = encoded.getAttentionMask();

        // Apribojame iki 512 žetonų (distilbert maksimumas)
        int maxLength = Math.min(inputIds.length, 512);
        long[] truncatedInputIds = Arrays.copyOf(inputIds, maxLength);
        long[] truncatedAttentionMask = Arrays.copyOf(attentionMask, maxLength);

        NDArray inputIdsArray = manager.create(truncatedInputIds).expandDims(0);
        NDArray attentionMaskArray = manager.create(truncatedAttentionMask).expandDims(0);
        return new NDList(inputIdsArray, attentionMaskArray);
    }

    @Override
    public Classifications processOutput(TranslatorContext ctx, NDList list) {
        NDArray probabilities = list.get(0).softmax(0);
        float[] probs = probabilities.toFloatArray();
        List<Double> probList = new java.util.ArrayList<>();
        for (float prob : probs) {
            probList.add((double) prob);
        }
        return new Classifications(classes, probList);
    }

    @Override
    public Batchifier getBatchifier() {
        return Batchifier.STACK;
    }
}