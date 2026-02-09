package com.pickeat.backend.support.fixture;

import com.pickeat.backend.template.domain.Template;

public class TemplateFixture {

    public static Template create() {
        return new Template("템플릿");
    }
}
