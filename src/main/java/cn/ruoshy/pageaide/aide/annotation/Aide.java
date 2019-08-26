package cn.ruoshy.pageaide.aide.annotation;


import cn.ruoshy.pageaide.aide.AideSign;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aide {
    AideSign value();
}
