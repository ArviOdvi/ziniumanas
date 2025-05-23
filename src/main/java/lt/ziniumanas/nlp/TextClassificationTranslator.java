package lt.ziniumanas.nlp;
// Translatorius tekstų klasifikavimui
import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.modality.Classifications;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.types.DataType;
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

public class TextClassificationTranslator implements Translator<String, Classifications>, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(TextClassificationTranslator.class);
    private final HuggingFaceTokenizer tokenizer;
    private final List<String> classes;
    private final int maxLength;

    public TextClassificationTranslator(List<String> classes, NlpModelProperties properties) {
        this.classes = classes;
        this.maxLength = properties.getMaxLength();
        try {
            Path tokenizerPath = Path.of(properties.getPath());
            if (!Files.exists(tokenizerPath) || !Files.isDirectory(tokenizerPath)) {
                throw new IOException("Tokenizerio katalogas nerastas arba netinkamas: " + tokenizerPath);
            }

            this.tokenizer = HuggingFaceTokenizer.newInstance(
                    "distilbert-base-multilingual-cased",
                    Map.of(
                            "path", properties.getPath(),
                            "maxLength", String.valueOf(maxLength),
                            "doLowerCase", "false",
                            "padding", "max_length",
                            "truncation", "true"
                    )
            );
            logger.info("DistilBERT tokenizeris įkeltas iš {}", tokenizerPath);
        } catch (IOException e) {
            logger.error("Nepavyko įkelti tokenizerio iš {}: {}", properties.getPath(), e.getMessage(), e);
            throw new RuntimeException("Tokenizerio įkėlimo klaida", e);
        }
    }

    @Override
    public NDList processInput(TranslatorContext ctx, String input) {
        if (input == null || input.trim().isEmpty()) {
            logger.error("Įvestis negali būti null arba tuščia: {}", input);
            throw new IllegalArgumentException("Įvestis negali būti null arba tuščia");
        }
        try {
            Encoding encoding = tokenizer.encode(input);
            NDArray inputIdsArray = ctx.getNDManager()
                    .create(encoding.getIds(), new Shape(1, maxLength))
                    .toType(DataType.INT32, false);
            NDArray attentionMaskArray = ctx.getNDManager()
                    .create(encoding.getAttentionMask(), new Shape(1, maxLength))
                    .toType(DataType.FLOAT32, false);

            inputIdsArray.setName("input_ids");
            attentionMaskArray.setName("attention_mask");

            logger.debug("Input text: {}", input);
            logger.debug("Input IDs shape: {}, first 10 tokens: {}",
                    inputIdsArray.getShape(),
                    Arrays.toString(Arrays.copyOf(encoding.getIds(), Math.min(10, encoding.getIds().length))));
            logger.debug("Attention Mask shape: {}, first 10 values: {}",
                    attentionMaskArray.getShape(),
                    Arrays.toString(Arrays.copyOf(encoding.getAttentionMask(), Math.min(10, encoding.getAttentionMask().length))));

            return new NDList(inputIdsArray, attentionMaskArray);
        } catch (Exception e) {
            logger.error("Klaida apdorojant įvestį '{}': {}", input, e.getMessage(), e);
            throw new RuntimeException("Nepavyko apdoroti įvesties", e);
        }
    }

    @Override
    public Classifications processOutput(TranslatorContext ctx, NDList logits) {
        try {
            NDArray logitsArray = logits.singletonOrThrow();
            logger.debug("Logits shape: {}, values: {}",
                    logitsArray.getShape(),
                    Arrays.toString(logitsArray.toFloatArray()));
            NDArray probsArray = logitsArray.softmax(-1);
            logger.debug("Probabilities shape: {}, values: {}",
                    probsArray.getShape(),
                    Arrays.toString(probsArray.toFloatArray()));
            return new Classifications(classes, probsArray);
        } catch (Exception e) {
            logger.error("Klaida apdorojant modelio išvestį: {}", e.getMessage(), e);
            throw new RuntimeException("Nepavyko apdoroti išvesties", e);
        }
    }

    @Override
    public void close() {
        if (tokenizer != null) {
            tokenizer.close();
            logger.info("DistilBERT tokenizeris uždarytas");
        }
    }
}