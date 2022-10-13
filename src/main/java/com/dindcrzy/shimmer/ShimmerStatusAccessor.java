package com.dindcrzy.shimmer;

public interface ShimmerStatusAccessor {
    default boolean wasShimmering() {
        return false;
    }
    default void setWasShimmering(boolean bool) {
        
    }
}
