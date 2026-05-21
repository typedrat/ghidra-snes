/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.ui.options;

import docking.widgets.tree.GTreeLazyNode;
import docking.widgets.tree.GTreeNode;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;

final class SnesOptionsTreeNode extends GTreeLazyNode {
  private final String name;
  private final String pageKey;
  private final boolean leaf;
  private final boolean collapsible;
  private final List<GTreeNode> children = new ArrayList<>();

  private SnesOptionsTreeNode(String name, String pageKey, boolean leaf, boolean collapsible) {
    this.name = name;
    this.pageKey = pageKey;
    this.leaf = leaf;
    this.collapsible = collapsible;
  }

  /**
   * Creates a tree folder node.
   *
   * @param name label shown in the navigation tree
   * @param pageKey right-panel page key associated with this node (nullable)
   * @param collapsible whether this folder can stay collapsed
   * @return folder node
   */
  static SnesOptionsTreeNode folder(String name, String pageKey, boolean collapsible) {
    return new SnesOptionsTreeNode(name, pageKey, false, collapsible);
  }

  /**
   * Creates a leaf node that points to a concrete page.
   *
   * @param name label shown in the navigation tree
   * @param pageKey right-panel page key associated with this leaf
   * @return leaf node
   */
  static SnesOptionsTreeNode leaf(String name, String pageKey) {
    return new SnesOptionsTreeNode(name, pageKey, true, true);
  }

  /**
   * Adds a child node to this tree node.
   *
   * @param node child node to append
   */
  void addChild(SnesOptionsTreeNode node) {
    children.add(node);
  }

  /**
   * Returns the card-layout page key used by the options component.
   *
   * @return page key, or null when the node has no bound page
   */
  String getPageKey() {
    return pageKey;
  }

  /**
   * Indicates whether this node may remain collapsed.
   *
   * @return true if collapsible, false when the UI should force it expanded
   */
  boolean isCollapsible() {
    return collapsible;
  }

  @Override
  protected List<GTreeNode> generateChildren() {
    return new ArrayList<>(children);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Icon getIcon(boolean isExpanded) {
    return null;
  }

  @Override
  public String getToolTip() {
    return pageKey;
  }

  @Override
  public boolean isLeaf() {
    return leaf;
  }

  @Override
  public int compareTo(GTreeNode other) {
    return name.compareTo(other.getName());
  }
}
