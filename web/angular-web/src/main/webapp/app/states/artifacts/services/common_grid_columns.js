export function commonGridColumns() {
    let nextId = 0;
    return {
        repoPathColumn: function(specialClass) {
            return '<div ng-if="row.entity.repoKey" class="ui-grid-cell-contents '+specialClass+'">{{row.entity.repoKey}}/{{row.entity.path}}</div>' +
                    '<div ng-if="!row.entity.repoKey" class="ui-grid-cell-contents '+specialClass+'">{{row.entity.path}}</div>';
        },

        downloadableColumn: function(specialClass) {
            return '<div ng-if="row.entity.downloadLink"" class="ui-grid-cell-contents '+specialClass+'">{{row.entity.name}}</div>' +
                    '<div ng-if="!row.entity.downloadLink" class="ui-grid-cell-contents '+specialClass+'">{{row.entity.name}}</div>';
        },

        booleanColumn: function(model) {
            return '<div class="grid-checkbox"><input ng-model="' +
                    model + '" type="checkbox" disabled/><span class="icon icon-v"></span></div>';
        },
        checkboxColumn: function(model, click, disabled) {
            return '<div ng-if="!row.entity._emptyRow" class="grid-cell-checkbox"><jf-checkbox><input ng-model="' + model + '"' +
                    (click && click.length ? ' ng-click="' + click + '"' : '') +
                    (disabled && disabled.length ? ' ng-disabled="' + disabled + '"' : '') +
                    ' type="checkbox"/></jf-checkbox></div>';
        },
        listableColumn: function(listModel,rowNameModel,displayModel,alwaysShow,testIdPrefix=null,showAsyncData, externalCountModel) {

            testIdPrefix = testIdPrefix ? testIdPrefix + '-' : '';

            displayModel = displayModel ? `{{${listModel}.length}} | {{${displayModel}}}` : `{{${externalCountModel} ? ${externalCountModel} : ${listModel}.length}} | {{${listModel}.join(\', \')}}{{${externalCountModel} && ${externalCountModel} > ${listModel}.length ? ',...' : ''}}`;

            let id = `${testIdPrefix}{{row.uid}}_${nextId}`;

            let template =  `<div ng-if="${listModel}.length" 
                                   ng-class="{'always-show': ${showAsyncData} || ${alwaysShow} }" 
                                   class="ui-grid-cell-contents no-tooltip" id="${id}">
                                <span class="gridcell-content-text">${displayModel}</span>
                                 <a class="gridcell-showall" ng-if="!(${showAsyncData}) && (grid.options.isOverflowing('${testIdPrefix}'+row.uid+'_'+${nextId}) || ${alwaysShow})" href ng-click="grid.options.showAll(${listModel},${rowNameModel},col)"> (See All)</a>
                                 <a class="gridcell-showall" ng-if="${showAsyncData}" href ng-click="grid.options.asyncShowAll(${rowNameModel},col)"> (See All)</a>
                             </div>
                             <div ng-if="!${listModel}.length" class="ui-grid-cell-contents no-tooltip" id="${id}">-</div>`;

            nextId++;
            return template;
        },
        iconColumn: function(cellText, cellIcon, iconClass) {
            return '<div class="ui-grid-cell-contents" id="type"><i class="icon icon-{{' + cellIcon + '}}' + (iconClass ? ' ' + iconClass : '') + '"></i>{{' + cellText + '}}</div>';
        },
        ajaxColumn: function () {
            return '<div class="ui-grid-cell-contents status-grid"><div class="icon-hourglass" ng-if="!row.entity.status"></div>{{row.entity.status}}</div>';
        }
    }
}