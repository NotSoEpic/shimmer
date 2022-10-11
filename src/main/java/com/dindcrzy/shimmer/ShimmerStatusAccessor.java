package com.dindcrzy.shimmer;

public interface ShimmerStatusAccessor {
    default boolean wasShimmering() {
        return false;
    }
}
