package org.will.framework.common.spring;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Created by will on 02/12/2016.
 */
public abstract class AbstractContextLoadListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null)//root application context 没有parent，他就是老大.
        {
            afterAppContextLoad();
        }
        if (event.getApplicationContext().getDisplayName().equals("Root WebApplicationContext")) {
            afterWebContextLoad();
        }
    }

    protected abstract void afterAppContextLoad();

    protected abstract void afterWebContextLoad();
}
