package com.emijor.user_feed.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderDetailDTO(
        String id,
        String orderId,
        String status,
        String userId,
        String cartId,
        List<ArticleDTO> articles,
        String created,
        String updated
) {
    public boolean hasArticle(String articleId) {
        if (articles == null) {
            return false;
        }
        return articles.stream()
                .anyMatch(article -> article.articleId().equals(articleId));
    }
}
