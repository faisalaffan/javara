package com.faisalaffan.javara.starter;

import org.springframework.context.annotation.Import;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JavaraAutoConfiguration.class)
public @interface EnableJavara {
}
