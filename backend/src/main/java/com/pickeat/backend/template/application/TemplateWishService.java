package com.pickeat.backend.template.application;

import com.pickeat.backend.global.configuration.cache.CacheKey.Holder;
import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.template.application.dto.response.TemplateWishResponse;
import com.pickeat.backend.template.domain.Template;
import com.pickeat.backend.template.domain.TemplateWish;
import com.pickeat.backend.template.domain.repository.TemplateRepository;
import com.pickeat.backend.template.domain.repository.TemplateWishRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TemplateWishService {

    private final TemplateRepository templateRepository;
    private final TemplateWishRepository templateWishRepository;

    @Cacheable(value = Holder.TEMPLATE_WISH_CACHE_KEY, key = "#templateId")
    public List<TemplateWishResponse> getWishesFromTemplates(Long templateId) {
        canAccessTemplate(templateId);
        List<TemplateWish> wishes = templateWishRepository.findAllByTemplateId(templateId);
        wishes.sort(Comparator.comparing(TemplateWish::getCreatedAt).reversed());
        return TemplateWishResponse.from(wishes);
    }

    private void canAccessTemplate(Long templateId) {
        Template template = getTemplate(templateId);
        if (!template.getIsActive()) {
            throw new ClientException(ClientErrorCode.TEMPLATE_NOT_FOUND);
        }
    }

    private Template getTemplate(Long templateId) {
        return templateRepository.findById(templateId).orElseThrow(
                () -> new ClientException(ClientErrorCode.TEMPLATE_NOT_FOUND));
    }
}
