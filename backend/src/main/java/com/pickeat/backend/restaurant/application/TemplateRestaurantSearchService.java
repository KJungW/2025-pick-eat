package com.pickeat.backend.restaurant.application;

import com.pickeat.backend.global.exception.code.ClientErrorCode;
import com.pickeat.backend.global.exception.type.ClientException;
import com.pickeat.backend.restaurant.application.dto.RestaurantInfoDto;
import com.pickeat.backend.restaurant.application.dto.request.TemplateRestaurantRequest;
import com.pickeat.backend.template.domain.Template;
import com.pickeat.backend.template.domain.TemplateWish;
import com.pickeat.backend.template.domain.repository.TemplateRepository;
import com.pickeat.backend.template.domain.repository.TemplateWishRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TemplateRestaurantSearchService {

    private final TemplateRepository templateRepository;
    private final TemplateWishRepository templateWishRepository;

    public List<RestaurantInfoDto> searchByTemplate(TemplateRestaurantRequest request) {
        Template template = getTemplateById(request.templateId());
        validateTemplateActive(template);
        List<TemplateWish> templateWishes = getTemplateWishById(template);
        return RestaurantInfoDto.fromTemplateWish(templateWishes);
    }

    private Template getTemplateById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new ClientException(ClientErrorCode.TEMPLATE_NOT_FOUND));
    }

    private List<TemplateWish> getTemplateWishById(Template template) {
        return templateWishRepository.findAllByTemplateId(template.getId());
    }

    private void validateTemplateActive(Template template) {
        if (!template.getIsActive()) {
            throw new ClientException(ClientErrorCode.TEMPLATE_NOT_FOUND);
        }
    }
}
