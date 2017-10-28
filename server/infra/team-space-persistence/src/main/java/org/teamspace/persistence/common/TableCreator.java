package org.teamspace.persistence.common;

import java.lang.annotation.*;

/**
 * Created by shpilb on 16/04/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TableCreator {
    int creationOrder();
}
