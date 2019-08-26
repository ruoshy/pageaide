package cn.ruoshy.pageaide.aide.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface NumberParam {
    String value();
}
