package com.github.megallo.markoverator.utils;

import com.github.megallo.markoverator.bigrammer.Bigrammer;

/**
 * Hi, what does this class do?
 **/
public class ModelSharedContext {
    private static Bigrammer bigrammer;
    private static Bigrammer poetryBigrammer;

    private static final class LazyModelSharedContext {
        static final ModelSharedContext INSTANCE = new ModelSharedContext();
    }

    public static ModelSharedContext getInstance() {
        return LazyModelSharedContext.INSTANCE;
    }



}
