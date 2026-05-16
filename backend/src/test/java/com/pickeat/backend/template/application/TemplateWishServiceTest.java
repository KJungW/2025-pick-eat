package com.pickeat.backend.template.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.pickeat.backend.global.configuration.cache.CacheKey;
import com.pickeat.backend.global.exception.type.BusinessException;
import com.pickeat.backend.support.DatabaseSliceTest;
import com.pickeat.backend.support.fixture.TemplateFixture;
import com.pickeat.backend.support.fixture.TemplateWishFixture;
import com.pickeat.backend.template.application.dto.response.TemplateWishResponse;
import com.pickeat.backend.template.domain.Template;
import com.pickeat.backend.template.domain.TemplateWish;
import com.pickeat.backend.template.domain.repository.TemplateWishRepository;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

@Import(value = {TemplateWishService.class})
class TemplateWishServiceTest extends DatabaseSliceTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private TemplateWishRepository templateWishRepository;

    @Autowired
    private TemplateWishService templateWishService;

    @Nested
    class 템플릿_목록_조회_케이스 {

        @Test
        void 템플릿_목록_조회_성공() {
            // given
            Template template = entityManager.persist(TemplateFixture.create());
            List<TemplateWish> templateWishes = List.of(
                    entityManager.persist(TemplateWishFixture.create(template.getId())),
                    entityManager.persist(TemplateWishFixture.create(template.getId())));
            entityManager.flush();
            entityManager.clear();

            // when
            List<TemplateWishResponse> response = templateWishService.getWishesFromTemplates(template.getId());

            // then
            List<Long> templateWishIds = templateWishes.stream()
                    .sorted(Comparator.comparing(TemplateWish::getCreatedAt).reversed())
                    .map(TemplateWish::getId)
                    .toList();
            assertThat(response)
                    .extracting(TemplateWishResponse::id)
                    .containsExactlyElementsOf(templateWishIds);
        }

        @Test
        void 존재하지_않는_템플릿일_경우_예외발생() {
            // given
            Long invalidId = 9999L;

            // when & then
            assertThatThrownBy(() -> templateWishService.getWishesFromTemplates(invalidId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("템플릿을 찾을 수 없습니다.");
        }

        @Test
        void 비활성_상태의_템플릿일_경우_예외발생() {
            // given
            Template inactiveTemplate = TemplateFixture.create(false);
            entityManager.persist(inactiveTemplate);

            entityManager.flush();
            entityManager.clear();

            // when & then
            assertThatThrownBy(() -> templateWishService.getWishesFromTemplates(inactiveTemplate.getId()))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("템플릿을 찾을 수 없습니다.");
        }

        @Test
        void 템플릿_위시_조회_시_캐시가_적용된다() {
            // given
            Template template = entityManager.persist(TemplateFixture.create(true));
            TemplateWish wish = entityManager.persist(TemplateWishFixture.create(template.getId()));
            entityManager.flush();
            entityManager.clear();

            Long templateId = template.getId();

            // when
            List<TemplateWishResponse> serviceResponse = templateWishService.getWishesFromTemplates(templateId);

            // then
            Cache cache = cacheManager.getCache(CacheKey.Holder.TEMPLATE_WISH_CACHE_KEY);
            assertAll(
                    () -> assertThat(cache).isNotNull(),
                    () -> assertThat(cache.get(templateId)).isNotNull(),
                    () -> assertThat(cache.get(templateId).get()).isEqualTo(serviceResponse)
            );
        }
    }
}
