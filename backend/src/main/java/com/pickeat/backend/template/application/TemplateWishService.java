package com.pickeat.backend.template.application;

import com.pickeat.backend.global.exception.BusinessException;
import com.pickeat.backend.global.exception.ErrorCode;
import com.pickeat.backend.template.application.dto.response.TemplateWishResponse;
import com.pickeat.backend.template.domain.Template;
import com.pickeat.backend.template.domain.TemplateWish;
import com.pickeat.backend.template.domain.repository.TemplateRepository;
import com.pickeat.backend.template.domain.repository.TemplateWishRepository;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TemplateWishService {

    private final TemplateRepository templateRepository;
    private final TemplateWishRepository templateWishRepository;

    public List<TemplateWishResponse> getWishesFromTemplates(Long templateId) {
        Template template = getTemplate(templateId);
        validateTemplateState(template);

        List<TemplateWish> wishes = templateWishRepository.findAllByTemplateId(templateId);
        wishes.sort(Comparator.comparing(TemplateWish::getCreatedAt).reversed());
        return TemplateWishResponse.from(wishes);
    }

    public Template getTemplate(Long templateId) {
        return templateRepository.findById(templateId).orElseThrow(
                () -> new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND));
    }

    public void validateTemplateState(Template template) {
        if (!template.getIsActive()) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
    }
}
