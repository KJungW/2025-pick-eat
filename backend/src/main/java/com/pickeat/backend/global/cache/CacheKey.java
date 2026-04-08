package com.pickeat.backend.global.cache;

import lombok.Getter;

@Getter
public enum CacheKey {

    TEMPLATE_LIST_CACHE_KEY(Holder.TEMPLATE_LIST_CACHE_KEY),
    TEMPLATE_WISH_CACHE_KEY(Holder.TEMPLATE_WISH_CACHE_KEY);

    private final String value;

    CacheKey(String value) {
        this.value = value;
    }

    public static class Holder {

        public static final String TEMPLATE_LIST_CACHE_KEY = "templateList";
        public static final String TEMPLATE_WISH_CACHE_KEY = "templateWishes";
    }
}
