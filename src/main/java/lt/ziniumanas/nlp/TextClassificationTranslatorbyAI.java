package lt.ziniumanas.nlp;

import ai.djl.modality.Classifications;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

import java.util.List;

public class TextClassificationTranslatorbyAI implements Translator<String, Classifications> {
    private List<String> classes = List.of("Politika", "Verslas", "Sportas", "Technologijos", "Kultūra"); // Pakeisk pagal savo modelį

    @Override
    public NDList processInput(TranslatorContext ctx, String input) throws Exception {
        NDManager manager = ctx.getNDManager();

        // Paprastas žodžių skaičiavimas (tik pavyzdys – tavo modelis gali tikėtis kitokio formato)
        float[] features = extractFeatures(input);

        return new NDList(manager.create(features));
    }

    @Override
    public Classifications processOutput(TranslatorContext ctx, NDList list) throws Exception {
        return new Classifications(classes, list.singletonOrThrow());
    }

    @Override
    public Batchifier getBatchifier() {
        return null; // Nenaudojame "batch'inimo"
    }

    private float[] extractFeatures(String input) {
        // Supaprastinta: grąžina tekstą ilgio ir simbolių dažnių pagrindu
        // Pritaikyk pagal savo modelio treniravimo metodą!
        float length = input.length();
        float upper = input.chars().filter(Character::isUpperCase).count();
        float lower = input.chars().filter(Character::isLowerCase).count();
        return new float[]{length, upper, lower};
    }
}

