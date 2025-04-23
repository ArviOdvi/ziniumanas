package lt.ziniumanas.model.enums;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
public enum ArticleStatus {
        DRAFT,
        PENDING_APPROVAL,
        APPROVED,
        PUBLISHED,
        ARCHIVED,
        REJECTED
    }

    // Jūsų Article klasėje:


