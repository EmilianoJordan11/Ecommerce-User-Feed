package com.emijor.user_feed.dto.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderDTO(
        String id,
        String status,
        String cartId,
        float totalPrice,
        float totalPayment,
        String updated,
        String created,
        int articles,
        
        List<ArticleDTO> articlesList
) {
    public boolean hasArticle(String articleId) {
        if (articlesList == null) {
            return false;
        }
        return articlesList.stream()
                .anyMatch(article -> article.articleId().equals(articleId));
    }
}
