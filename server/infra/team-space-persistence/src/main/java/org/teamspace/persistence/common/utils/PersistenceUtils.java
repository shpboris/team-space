package org.teamspace.persistence.common.utils;

import java.util.List;

/**
 * Created by shpilb on 04/11/2017.
 */
public class PersistenceUtils {
    public static <T> List<T> normalizeJoinedList(List<T> list){
        if(list != null && list.size() == 1){
            if(list.get(0) == null){
                list.remove(0);
            }
        }
        return list;
    }
}
