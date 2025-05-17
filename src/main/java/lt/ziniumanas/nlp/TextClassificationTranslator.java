package lt.ziniumanas.nlp;
// Translatorius tekstų klasifikavimui
import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.modality.Classifications;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.Shape;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import lt.ziniumanas.config.NlpModelProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TextClassificationTranslator implements Translator<String, Classifications>, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(TextClassificationTranslator.class);
    private final HuggingFaceTokenizer tokenizer;
    private final List<String> classes;
    private static final int MAX_LENGTH = 128;

    public TextClassificationTranslator(List<String> classes, NlpModelProperties properties) {
        this.classes = classes;
        try {
            Path tokenizerPath = Path.of(properties.getPath());

            if (!Files.exists(tokenizerPath)) {
                throw new IOException("Tokenizerio katalogas nerastas: " + tokenizerPath);
            }

            List<String> requiredFiles = Arrays.asList("vocab.txt", "tokenizer_config.json", "special_tokens_map.json");
            for (String file : requiredFiles) {
                if (!Files.exists(tokenizerPath.resolve(file))) {
                    throw new IOException("Trūksta tokenizerio failo: " + file);
                }
            }

            this.tokenizer = HuggingFaceTokenizer.newInstance(
                    tokenizerPath,
                    Map.of(
                            "maxLength", String.valueOf(MAX_LENGTH),
                            "doLowerCase", "false",
                            "padding", "max_length",
                            "truncation", "true",
                            "tokenizerType", "slow"
                    )
            );
        } catch (IOException e) {
            logger.error("Nepavyko įkelti tokenizerio", e);
            throw new RuntimeException("Tokenizerio įkėlimo klaida", e);
        }
    }

    @Override
    public NDList processInput(TranslatorContext ctx, String input) {
        Encoding encoding = tokenizer.encode(input);
        long[] ids = encoding.getIds();
        long[] attentionMask = encoding.getAttentionMask();

        long[] paddedIds = new long[MAX_LENGTH];
        long[] paddedAttentionMask = new long[MAX_LENGTH];
        int len = Math.min(ids.length, MAX_LENGTH);
        System.arraycopy(ids, 0, paddedIds, 0, len);
        System.arraycopy(attentionMask, 0, paddedAttentionMask, 0, len);

        NDArray inputIdsArray = ctx.getNDManager().create(paddedIds, new Shape(1, MAX_LENGTH));
        NDArray attentionMaskArray = ctx.getNDManager().create(paddedAttentionMask, new Shape(1, MAX_LENGTH));
        inputIdsArray.setName("input_ids");
        attentionMaskArray.setName("attention_mask");

        return new NDList(inputIdsArray, attentionMaskArray);
    }

    @Override
    public Classifications processOutput(TranslatorContext ctx, NDList logits) {
        float[] logitsData = logits.singletonOrThrow().toFloatArray();

        // Softmax skaičiavimas
        double[] expValues = new double[logitsData.length];
        double sum = 0.0;
        for (int i = 0; i < logitsData.length; i++) {
            expValues[i] = Math.exp(logitsData[i]);
            sum += expValues[i];
        }

        List<Double> probs = new ArrayList<>();
        for (double val : expValues) {
            probs.add(val / sum);
        }

        return new Classifications(classes, probs);
    }
}