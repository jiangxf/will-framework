package org.will.framework.dlock.local;

import org.slf4j.LoggerFactory;
import org.will.framework.dlock.DLock;
import org.will.framework.dlock.DLockManager;
import org.will.framework.dlock.DLockTemplate;

/**
 * Created with IntelliJ IDEA
 * Description:
 * User: will
 * Date: 2018-07-14
 * Time: 16:30
 */
public class LocalDLockTemplate extends DLockTemplate {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LocalDLockTemplate.class);

    @Override
    protected DLock newDLock(String lockKey) {
        return DLockManager.newRedisLock(lockKey);
    }
}
