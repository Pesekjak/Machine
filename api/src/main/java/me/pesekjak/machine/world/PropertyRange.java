package me.pesekjak.machine.world;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used by special block data classes, indicates which property values
 * are available for each special block data.
 */
// This class is used by the code generators, edit with caution.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertyRange {

    /**
     * @return available states of property for this block data
     */
    String @NotNull [] available() default {};

}
