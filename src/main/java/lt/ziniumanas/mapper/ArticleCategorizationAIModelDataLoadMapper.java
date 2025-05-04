package lt.ziniumanas.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.*;

public class ArticleCategorizationAIModelDataLoadMapper {
    public static List<Map<String, String>> loadArticles(String filePath) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), List.class);
    }
}

