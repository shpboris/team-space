package org.teamspace.client.common.config.annotations;

import java.lang.annotation.*;

/**
 * Created by shpilb on 01/05/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PropertiesSource {
    String value();
}
