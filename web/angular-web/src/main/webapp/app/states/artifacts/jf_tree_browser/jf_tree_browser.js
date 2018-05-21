import JFCommonBrowser from "../jf_common_browser/jf_common_browser";
import types from '../constants/artifact_browser_icons.constant';

export function jfTreeBrowser() {
    return {
        scope: {
            browserController: '=',
            simpleMode: '=?'
        },
        restrict: 'E',
        controller: JFTreeBrowserController,
        controllerAs: 'jfTreeBrowser',
        templateUrl: 'states/artifacts/jf_tree_browser/jf_tree_browser.html',
        bindToController: true
    }
}

class JFTreeBrowserController extends JFCommonBrowser {
    constructor($state,$timeout, JFrogEventBus, $scope, TreeBrowserDao, $stateParams, $q,
                ArtifactoryState, ArtifactActions, JFrogNotifications, NativeBrowser, User,
                JFrogUIUtils, JFTreeApi) {

        super(JFTreeApi, $scope, ArtifactActions, ArtifactoryState);

        this.$state = $state;
        this.$scope = $scope;
        this.$timeout = $timeout;
        this.$stateParams = $stateParams;
        this.$q = $q;
        this.artifactoryNotifications = JFrogNotifications;
        this.JFrogEventBus = JFrogEventBus;
        this.JFrogUIUtils = JFrogUIUtils;
        this.EVENTS = JFrogEventBus.getEventsDefinition();
        this.treeBrowserDao = TreeBrowserDao;
        this.artifactoryState = ArtifactoryState;
        this.user = User.currentUser;
        this.nativeBrowser = NativeBrowser;
        this.types = types;

        this._registerEvents();

        this._setUpTree();

        this._watchModeChange();

    }

    _setUpTree() {
        let driver = {
            uniqueId: node => {
                if (!node.data) console.log(node);
                return node.data.fullpath
            },
            nodeById: this.nodeByPathGetter.bind(this),
            text: node => node.text,
            childrenChecker: this.childrenChecker.bind(this),
            children: this.childrenGetter.bind(this),
            parent: this.parentGetter.bind(this),
            classes: node => {
                let classes = [];
                if (node.data.isTrashcan()) classes.push('trashcan');
                else if (node.data.isInTrashcan()) classes.push('in-trashcan');
                if (node.data.isFavorite()) classes.push('favorite');
                return classes;
            },
            pane: node => {
                if (!this.simpleMode && this.user.isAdmin() && this.isTrashPinned() && node.data.isTrashcan()) return 'trash';
                else return 'default';
            },
            contextMenuItems: this._getContextMenuItems.bind(this)
        }

        this.treeApi
                .setNodeTemplate(node => this.getNodeTemplate(node))
                .setFilterCallback(node => this.filterCallback(node))
                .setDataDriver(driver)
                .on('item.selected', item => {
                    item.data.load().then(() => {
                        this.JFrogEventBus.dispatch(this.EVENTS.TREE_NODE_SELECT, item)
                    });
                })
                .on('keydown', e => {
                    this.JFrogEventBus.dispatch(this.EVENTS.ACTIVATE_TREE_SEARCH, e.key);
                })
                .on('ready', () => this.onTreeReady());

        this.mainViewPane = this.treeApi.createViewPane()
                .setItemHeight('30px')
                .setItemsPerPage('auto');

        this.trashViewPane = this.treeApi.createViewPane('trash')
                .setItemHeight('30px')
                .setItemsPerPage(5);

        this.treeApi.freeze();

    }

    _watchModeChange() {
        let firstTimeSwitch = true;
        this.$scope.$watch('jfTreeBrowser.simpleMode', simpleMode => {
            this.type = simpleMode ? 'simple' : 'tree';
            this.treeApi.freeze();
            this.treeApi.refreshPaneSelection();
            this.treeApi.refreshFilter();
            this.treeApi.setDrillDownMode(simpleMode).then(() => {
                if (!firstTimeSwitch) this.treeApi.unFreeze();
                firstTimeSwitch = false;
            })
        })
    }

    getNodeTemplate(node) {
        return `<div>
                    <i class="tree-node-icon ${this.types[node.type] ? this.types[node.type].icon : this.types['default'].icon}"></i>
                    <span class="node-text" style="margin-left: 5px">${node.text}</span>
                </div>`
    }

    onTreeReady() {
        this._checkIfFilterHasNoMatches();

        if (!this.$state.params.artifact) {
            this.treeApi.unFreeze();
            this.treeApi.selectFirst();
        }
        else {
            this.spinnerTimeout = this.$timeout(()=>{
                if (this.$state.current.name === 'artifacts.browsers.path') {
                    this.JFrogEventBus.dispatch(this.EVENTS.SHOW_SPINNER);
                }
            },600);
            this.treeBrowserDao.findNodeByFullPath(this.$state.params.artifact).then(node => {
                if (node === this.treeBrowserDao) {
                    this.$timeout.cancel(this.spinnerTimeout);
                    this.JFrogEventBus.dispatch(this.EVENTS.HIDE_SPINNER);
                    this.treeApi.unFreeze();
                    this.treeApi.selectFirst();
                }
                else {
                    node = this._transformData([node])[0];
                    let repo = this.treeApi.findNodeByUniqueId(node.data.repoKey);
                    let openDeepNode = () => {
                        this.treeApi.openDeepNode(node).then(() => {
                            this.treeApi.centerOnSelected();
                            this.treeApi.unFreeze();
                            this.$timeout.cancel(this.spinnerTimeout);
                            this.JFrogEventBus.dispatch(this.EVENTS.HIDE_SPINNER);
                        })
                    }
                    if (!this.treeApi.isNodeFiltered(repo)) {
                        this.afterNextFilterRefresh = () => openDeepNode();
                        this.browserController.resetAllFilters();
                    }
                    else openDeepNode();
                }
            });
        }
        this.mainViewPane.focus();

    }

    childrenGetter(parent) {
        let defer = this.$q.defer();
        if (!parent) {
            this.treeBrowserDao.getRoots().then(roots => {
                let hasArtifactsData = roots.length > 0;
                this.artifactoryState.setState("hasArtifactsData",hasArtifactsData);
                this.JFrogEventBus.dispatch(this.EVENTS.TREE_DATA_IS_SET,hasArtifactsData);

                defer.resolve(this._transformData(roots));
            }).catch(() => defer.resolve([]));
        }
        else {
            parent.data.getChildren().then(children => {
                defer.resolve(this._transformData(children));
            }).catch(() => defer.resolve([]));
        }
        return defer.promise;
    }

    childrenChecker(parent) {
        return parent.children;
    }

    parentGetter(node) {
        return node.data.parent ? this._transformData([node.data.parent])[0] : undefined;
    }

    nodeByPathGetter(path) {
        let defer = this.$q.defer();
        this.treeBrowserDao.findNodeByFullPath(path).then(node => {
            if (node !== this.treeBrowserDao) {
                defer.resolve(this._transformData([node])[0]);
            }
            else defer.reject();
        });

        return defer.promise;
    }

    _transformData(data) {
        data = data || [];
        return data.map((node) => {
            let item = {};
            item.children = node.hasChild;
            item.text = node.isTrashcan() ? '<span ng-init="appScope.jfTreeBrowser.initTrashPin()" class="trashcan-node">Trash Can<i ng-show="appScope.jfTreeBrowser.type === \'tree\' && !appScope.jfTreeBrowser.isTrashPinned()" ng-click="appScope.jfTreeBrowser.toggleTrashPin($event)" class="icon icon-pin trash-pin" jf-tooltip="Pin Trash Can"></i><i ng-show="appScope.jfTreeBrowser.type === \'tree\' && appScope.jfTreeBrowser.isTrashPinned()" ng-click="appScope.jfTreeBrowser.toggleTrashPin($event)" class="icon icon-unpin trash-pin" jf-tooltip="Unpin Trash Can"></i></span>'
                    : this.JFrogUIUtils.getSafeHtml(node.text);
            item.data = node;
            item.type = node.iconType;
            if (node.isTrashcan())
                item.li_attr={class:"-the-trashcan"};

            let type = (typeof node.fileType != 'undefined' ? node.fileType : node.type);
            // replace the node icon type to the package type if necessary
            if(this._iconsShouldBeReplacedWithRepoPkgTypeIcon(type,node.repoPkgType,node.fullpath)){
                item.type = node.iconType = node.repoPkgType.toLocaleLowerCase();
            }

            return item;
        });
    }

    _registerEvents() {

        this.JFrogEventBus.registerOnScope(this.$scope, [this.EVENTS.ACTION_WATCH, this.EVENTS.ACTION_UNWATCH], node => this._refreshContextMenu(node))

        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_FAVORITES, () => {
            this.JFrogEventBus.dispatch(this.EVENTS.TREE_REFRESH_FAVORITES);
        })

        this.treeApi.registerEventOnNode(this.EVENTS.TREE_REFRESH_FAVORITES, (node, params) => {
            if (node.data) node.data.updateFavoriteState()
        })

        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_SEARCH_CHANGE, text => this._searchTree(text)
        )
        ;
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_SEARCH_CANCEL, text => this._clear_search()
        )
        ;
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_SEARCH_KEYDOWN, e => {
            this.treeApi.handleKeyEvent(e);
            this._searchTreeKeyDown(e.keyCode)
        });
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_DEPLOY, (eventArgs) => {
            let repoKey = eventArgs[0];
            let repoNode = this.treeApi.findNodeByUniqueId(repoKey);
            this._refreshFolder(repoNode).then(() => {
                this.treeApi.openNode(repoNode);
            })
        });
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_REFRESH, node => this._refreshFolder(node)
        )
        ;
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_NODE_CM_REFRESH, node => this._refreshContextMenu(node)
        )
        ;
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_REFRESH, (node) => node ? this._refreshFolder(node) : this._refreshTree()
        )
        ;
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_DELETE, (node) => {
            if (this.simpleMode) {
                this._refreshParentFolder(node).then(() => {
                    let parentNode = node.data.parent ? this.treeApi.findNodeByUniqueId(node.data.parent.fullpath) : null;
                    if (parentNode && !parentNode.$childrenCache.length) {
                        this.treeApi.selectNode(parentNode);
                        this.treeApi.drillUp();
                    }
                })
            }
            else {
                this._refreshParentFolder(node).then(() => {
                    let selected = this.treeApi.getSelectedNode();
                    if (selected === node) {
                        let parent = this.treeApi.findNodeByUniqueId(node.data.parent.fullpath);
                        this.treeApi.selectNode(parent)
                    }
                    else {
                        let updated = this.treeApi.findNodeByUniqueId(selected.data.fullpath);
                        this.treeApi.selectNode(updated)
                    }
                })
                if ((!node.data || !node.data.isInTrashcan || !node.data.isTrashcan) || (!node.data.isInTrashcan() && !node.data.isTrashcan())) {
                    this.refreshTrashCan();
                }
            }
        });
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_DELETE_CONTENT, (node) => {
            node.data.invalidateParent();
            this._refreshFolder(node);
            this.refreshTrashCan();
        });

        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_MOVE, (options) => {
            if (!this.simpleMode) this._refreshParentFolder(options.node);
            this._refreshFolderPath(options);
        });
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.ACTION_COPY, (options) => {
            this._refreshFolderPath(options);
        });

        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_NODE_OPEN, path => {
            this._openTreeNode(path)
        });

        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_COLLAPSE_ALL, () => {
            this._collapseAll();
        });

        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_COMPACT, () => {
            this._toggleCompactFolders();
        });

        // URL changed (like back button / forward button / someone input a URL)
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TABS_URL_CHANGED, (stateParams) => {
            // Check if it's already the current selected node (to prevent opening the same tree node twice)
            let selectedNode = this.treeApi.getSelectedNode();
            selectedNode = selectedNode ? selectedNode.data : null;
            if (selectedNode && selectedNode.fullpath === stateParams.artifact) return;
            this.treeBrowserDao.findNodeByFullPath(stateParams.artifact)
                    .then(node => {
                        if (node && node !== this.treeBrowserDao) {
                            node = this._transformData([node])[0];
                            this.treeApi.openDeepNode(node);
                        }
                    });
        });

        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.REFRESH_PAGE_CONTENT,()=>{
            this._refreshTree();
        });

        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_REFRESH_SORTING, ()=>this._refreshTreeSorting());
        this.JFrogEventBus.registerOnScope(this.$scope, this.EVENTS.TREE_REFRESH_FILTER, ()=>this._refreshTreeSorting());

    }

    _toggleCompactFolders() {
        this.treeBrowserDao.invalidateRoots();

        this.spinnerTimeout = this.$timeout(()=>{
            this.JFrogEventBus.dispatch(this.EVENTS.SHOW_SPINNER);
        },600);

        this.treeApi.refreshTree(true, false).then(() => {
            this.$timeout.cancel(this.spinnerTimeout);
            this.JFrogEventBus.dispatch(this.EVENTS.HIDE_SPINNER);
        })
    }

    _refreshTreeSorting() {
        this.treeApi.freeze();
        this.treeApi.refreshFilter();
        this.treeApi.refreshTree(true, false).then(() => {
            this._checkIfFilterHasNoMatches().then(() => {
                let selected = this.treeApi.getSelectedNode();
                if (selected) {
                    if (!this.treeApi.isNodeFiltered(selected)) {
                        this.$timeout(() => this.treeApi.selectFirst());
                    }
                }
                else {
                    this.treeApi.selectFirst();
                }
                this.treeApi.centerOnSelected();
                this.treeApi.unFreeze();
                if (this.afterNextFilterRefresh) {
                    this.afterNextFilterRefresh();
                    delete this.afterNextFilterRefresh;
                }
            })
        })
    }

    _checkIfFilterHasNoMatches() {
        let defer = this.$q.defer();

        if (this.mainViewPane.getFilteredNodesCount() === 0 && this.mainViewPane.getNodesCount() !== 0) {
            if (this.simpleMode && this.treeApi.getCurrentParent()) {
                this.treeApi.drillUpToRoot();
                this.$timeout(() => {
                    this._checkIfFilterHasNoMatches().then(() => {
                        defer.resolve();
                    })
                });
            }
            else {
                this.browserController.filterHasNoMatches = true;
                defer.resolve();
            }
        }
        else {
            this.browserController.filterHasNoMatches = false;
            defer.resolve();
        }

        return defer.promise;
    }

    toggleTrashPin(e) {
        e.stopImmediatePropagation()
        e.preventDefault();

        if (this.isTrashPinned()) {
            this.unpinTrash();
        }
        else {
            this.pinTrash();
        }
    }

    pinTrash() {
        localStorage.pinnedTrash = true;
        this.treeApi.refreshPaneSelection();
        this._checkIfFilterHasNoMatches();
    }

    unpinTrash() {
        localStorage.pinnedTrash = false;
        this.treeApi.refreshPaneSelection();
        let trashCanNode = this.treeApi.findNode(n => n.data.isTrashcan());
        this.$timeout(() => this.treeApi.bringNodeToView(trashCanNode));
        this._checkIfFilterHasNoMatches();
    }

    isTrashPinned() {
        return localStorage.pinnedTrash === "true";
    }

    _refreshFolder(node) {
        if (node.data) {
            node.data.invalidateChildren();
            if (node.data.isRepo()) {
                this.treeBrowserDao.invalidateRoots();
            }
        }
        else this.treeBrowserDao.invalidateRoots();
        return this.treeApi.refreshNode(node);
    }

    _refreshTree(node) {
        this.treeBrowserDao.invalidateRoots();
        return this.treeApi.refreshTree();
    }

    _refreshContextMenu(node) {
        this.treeApi.refreshNodeContextMenu(node);
    }

    _refreshParentFolder(node) {
        node.data.invalidateParent();
        let parentNode = this.treeApi.getParentNode(node);
        if (parentNode) return this._refreshFolder(parentNode);
        else {
            if (node.data && node.data.isRepo()) {
                return this._refreshFolder(node);
            }
            else {
                return this.$q.when();
            }
        }
    }

    refreshTrashCan() {
        let trashNode = this.treeApi.findNode(node => node.data.repoType === 'trash' && node.data.repoKey === 'auto-trashcan');
        if (trashNode) this.JFrogEventBus.dispatch(this.EVENTS.TREE_REFRESH, trashNode);
    }

    _refreshFolderPath(option) {
        let targetPath = _.compact(option.target.targetPath.split('/'));
        let path = [option.target.targetRepoKey].concat(targetPath).join('/');
        this.treeApi.freeze();
        if (this.simpleMode) this.treeApi.drillUpToRoot();
        let repo = this.treeApi.findNodeByUniqueId(option.target.targetRepoKey);
        this._refreshFolder(repo).then(() => {
            this.treeApi.openDeepNodeByUniqueId(path).then(() => {
                this.treeApi.centerOnSelected();
                this.treeApi.unFreeze();
            })
        })

    }

    initTrashPin() {
        this.$timeout(() => {
            let hScroll = $('.trash-pin').parents('.h-scroll-wrapper');
            hScroll.on('scroll', () => this._resizePinnedTrash());
            $(window).on('resize', () => this._resizePinnedTrash());
            this.$scope.$on('ui-layout.resize', () => this._resizePinnedTrash());
            this._resizePinnedTrash();
        })
    }
    _resizePinnedTrash() {
        let trashPin = $('.trash-pin');

        let hScroll = trashPin.parents('.h-scroll-wrapper');

        trashPin.css('left', hScroll.scrollLeft() + hScroll.parent().width() - 50 + 'px');
    }
}
