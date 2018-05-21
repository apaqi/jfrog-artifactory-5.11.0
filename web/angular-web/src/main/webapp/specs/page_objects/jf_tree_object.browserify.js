'use strict';
function JFTreeObject(jfTreeBrowserElement) {
  this.ctrl = angular.element(jfTreeBrowserElement).controller('jfTreeBrowser');
  this.treeApi = this.ctrl.treeApi;

  this.getNode = function(node) {
    return node;
  };
  
  this.expandFirstItem = function() {
    $('.jf-tree-item:first-child .node-expander .action-icon').click();
  };

  this.loadNodeItem = function(text) {
      $(this.getNodeWithText(text)).parent().click();
  };

  this.getNodeWithText = function(text) {
    return _.find($('.jf-tree-item .node-text'), function(el) {
      return $(el).text().match(new RegExp(text));
    });
  };
  this.getRootItem = function() {
    return {children: this.treeApi.$root};
  }
  this.findNodeWithData = function(data) {
    return this.treeApi.findNode(n => n.data === data);
  }
}
module.exports = JFTreeObject;