package org.artifactory.work.queue.mbean.composer;

import org.artifactory.work.queue.mbean.WorkQueueMBean;

/**
 * @author Shay Bagants
 */
public class ComposerExtractorWorkQueue implements ComposerExtractorWorkQueueMBean {
    private WorkQueueMBean workQueueMBean;

    public ComposerExtractorWorkQueue(WorkQueueMBean workQueueMBean) {
        this.workQueueMBean = workQueueMBean;
    }

    @Override
    public int getQueueSize() {
        return workQueueMBean.getQueueSize();
    }

    @Override
    public int getNumberOfWorkers() {
        return workQueueMBean.getNumberOfWorkers();
    }

    @Override
    public int getMaxNumberOfWorkers() {
        return workQueueMBean.getMaxNumberOfWorkers();
    }

    @Override
    public String getName() {
        return workQueueMBean.getName();
    }
}
