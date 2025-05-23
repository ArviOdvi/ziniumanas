import os
from pathlib import Path
from transformers import DistilBertTokenizer, DistilBertTokenizerFast
import logging

# Nustatome logus
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)

def generate_tokenizer_json(model_path: str):
    model_path = Path(model_path)
    try:
        # Patikriname, ar katalogas egzistuoja
        if not model_path.exists():
            logger.error(f"Katalogas {model_path} neegzistuoja")
            raise FileNotFoundError(f"Katalogas {model_path} nerastas")

        # Patikriname būtinuosius failus
        required_files = ["vocab.txt", "tokenizer_config.json", "special_tokens_map.json"]
        for file in required_files:
            file_path = model_path / file
            if not file_path.exists():
                logger.error(f"Trūksta failo: {file_path}")
                raise FileNotFoundError(f"Trūksta failo: {file_path}")

        # Įkeliame lėtą tokenizerį
        logger.info(f"Įkeliame DistilBertTokenizer iš {model_path}")
        slow_tokenizer = DistilBertTokenizer.from_pretrained(model_path)

        # Konvertuojame į greitą tokenizerį
        logger.info("Konvertuojame į DistilBertTokenizerFast")
        fast_tokenizer = DistilBertTokenizerFast.from_pretrained(model_path, from_slow=True)

        # Išsaugome tokenizerį su tokenizer.json
        logger.info(f"Išsaugome tokenizerį į {model_path}")
        fast_tokenizer.save_pretrained(model_path)
        logger.info(f"Tokenizeris sėkmingai išsaugotas su tokenizer.json į {model_path}")

        # Patikriname, ar tokenizer.json sukurtas
        tokenizer_json_path = model_path / "tokenizer.json"
        if tokenizer_json_path.exists():
            logger.info(f"tokenizer.json sukurtas: {tokenizer_json_path}")
        else:
            logger.error(f"tokenizer.json nebuvo sukurtas")
            raise RuntimeError("Nepavyko sukurti tokenizer.json")

    except Exception as e:
        logger.error(f"Klaida generuojant tokenizer.json: {str(e)}", exc_info=True)
        raise

if __name__ == "__main__":
    model_path = "/python-classifier-api/model/custom-distilbert"
    generate_tokenizer_json(model_path)