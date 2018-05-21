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

package org.artifactory.storage.db.aql.parser.elements.high.level.domain.build;

import com.google.common.collect.Lists;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlLogicalFieldEnum;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.domain.DomainProviderElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.language.RealFieldElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalNameElement;
import org.artifactory.storage.db.aql.parser.elements.low.level.LazyParserElement;

import java.util.List;

import static org.artifactory.aql.model.AqlDomainEnum.buildPromotions;
import static org.artifactory.aql.model.AqlDomainEnum.buildProperties;
import static org.artifactory.aql.model.AqlDomainEnum.builds;
import static org.artifactory.aql.model.AqlDomainEnum.modules;
import static org.artifactory.storage.db.aql.parser.AqlParser.buildModuleLogicalFields;
import static org.artifactory.storage.db.aql.parser.AqlParser.buildPromotionsLogicalFields;
import static org.artifactory.storage.db.aql.parser.AqlParser.buildPropertiesLogicalFields;
import static org.artifactory.storage.db.aql.parser.AqlParser.dot;

/**
 * @author Gidi Shabat
 *         Acepts the builds fields and its sub domain
 */
public class BuildLogicalFieldsElement extends LazyParserElement implements DomainProviderElement {
    @Override
    protected ParserElement init() {
        List<ParserElement> list = Lists.newArrayList();
        fillWithDomainFields(list);
        fillWithSubDomains(list);
        return fork(list.toArray(new ParserElement[list.size()]));
    }

    private void fillWithDomainFields(List<ParserElement> list) {
        AqlLogicalFieldEnum[] fields = builds.getLogicalFields();
        for (AqlLogicalFieldEnum field1 : fields) {
            list.add(new RealFieldElement(field1.getSignature(), builds));
        }
    }

    private void fillWithSubDomains(List<ParserElement> list) {
        list.add(forward(new InternalNameElement(buildProperties.signature), dot, buildPropertiesLogicalFields));
        list.add(forward(new InternalNameElement(buildPromotions.signature), dot, buildPromotionsLogicalFields));
        list.add(forward(new InternalNameElement(modules.signature), dot, buildModuleLogicalFields));
    }

    @Override
    public boolean isVisibleInResult() {
        return true;
    }

    @Override
    public AqlDomainEnum getDomain() {
        return builds;
    }
}
