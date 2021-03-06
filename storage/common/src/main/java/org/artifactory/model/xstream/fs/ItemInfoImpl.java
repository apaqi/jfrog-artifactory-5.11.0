/*
 *
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2016 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.artifactory.model.xstream.fs;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.artifactory.fs.ItemInfo;
import org.artifactory.fs.MutableItemInfo;
import org.artifactory.repo.RepoPath;
import org.artifactory.storage.db.DbService;
import org.artifactory.util.RepoPathUtils;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author yoavl
 */
public abstract class ItemInfoImpl implements InternalItemInfo {

    private final RepoPath repoPath;
    private final String name;
    private long created;
    protected long lastModified;

    /**
     * Unique database identifier. {@link org.artifactory.storage.db.DbService#NO_DB_ID} if not persisted yet.
     */
    @XStreamOmitField
    protected long id = DbService.NO_DB_ID;

    ItemInfoImpl(RepoPath repoPath) {
        Objects.requireNonNull(repoPath, "RepoPath cannot be null");
        if (isFolder()) {
            this.repoPath = RepoPathUtils.toFolderRepoPath(repoPath);
        } else {
            this.repoPath = RepoPathUtils.toFileRepoPath(repoPath);
        }
        this.name = repoPath.getName();
        this.created = System.currentTimeMillis();
        this.lastModified = this.created;
    }

    ItemInfoImpl(RepoPath repoPath, long id) {
        this(repoPath);
        this.id = id;
    }

    ItemInfoImpl(org.artifactory.fs.ItemInfo info) {
        this(info.getRepoPath(), info.getId());
        this.created = info.getCreated();
        setLastModified(info.getLastModified());
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public RepoPath getRepoPath() {
        return repoPath;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRepoKey() {
        return repoPath.getRepoKey();
    }

    /**
     * @return The path part of the repo path of this item (ie, path without the repository key).
     */
    @Override
    public String getRelPath() {
        return repoPath.getPath();
    }

    @Override
    public long getCreated() {
        return created;
    }

    @Override
    public void setCreated(long created) {
        this.created = created;
    }

    @Override
    public long getLastModified() {
        return lastModified;
    }

    @Override
    public final void setLastModified(long lastModified) {
        if (lastModified > 0L) {
            this.lastModified = lastModified;
        } else {
            this.lastModified = this.created;
        }
    }

    @Override
    public String getModifiedBy() {
        return getAdditionalInfo().getModifiedBy();
    }

    @Override
    public void setModifiedBy(String name) {
        getAdditionalInfo().setModifiedBy(name);
    }

    @Override
    public String getCreatedBy() {
        return getAdditionalInfo().getCreatedBy();
    }

    @Override
    public void setCreatedBy(String name) {
        getAdditionalInfo().setCreatedBy(name);
    }

    @Override
    public long getLastUpdated() {
        return getAdditionalInfo().getLastUpdated();
    }

    @Override
    public void setLastUpdated(long lastUpdated) {
        getAdditionalInfo().setLastUpdated(lastUpdated);
    }

    @Override
    public boolean isIdentical(ItemInfo info) {
        return this.getClass() == info.getClass() &&
                this.lastModified == info.getLastModified() &&
                this.created == info.getCreated() &&
                this.repoPath.equals(info.getRepoPath()) &&
                this.name.equals(info.getName()) &&
                this.getAdditionalInfo().isIdentical(((InternalItemInfo) info).getAdditionalInfo());
    }

    @Override
    public boolean merge(MutableItemInfo itemInfo) {
        if (this == itemInfo || this.isIdentical(itemInfo)) {
            // They are equal nothing to do
            return false;
        }
        boolean modified = false;
        if (itemInfo.getLastModified() > 0) {
            this.lastModified = itemInfo.getLastModified();
            modified = true;
        }
        if (itemInfo.getCreated() > 0 && itemInfo.getCreated() < getCreated()) {
            this.created = itemInfo.getCreated();
            modified = true;
        }
        modified |= this.getAdditionalInfo().merge(((InternalItemInfo) itemInfo).getAdditionalInfo());
        return modified;
    }

    @Override
    public int compareTo(@Nonnull ItemInfo item) {
        return getName().compareTo(item.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ItemInfoImpl info = (ItemInfoImpl) o;
        return repoPath.equals(info.repoPath);
    }

    @Override
    public int hashCode() {
        return repoPath.hashCode();
    }

    @Override
    public String toString() {
        return "ItemInfo{" +
                "repoPath=" + repoPath +
                ", created=" + created +
                ", lastModified=" + lastModified +
                '}';
    }
}
