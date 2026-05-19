/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.ui.options;

import docking.DialogComponentProvider;
import docking.widgets.MultiLineLabel;
import docking.widgets.label.GDLabel;
import docking.widgets.tree.GTree;
import docking.widgets.tree.GTreeLazyNode;
import docking.widgets.tree.GTreeNode;
import ghidra.program.model.listing.Program;
import ghidra.util.Swing;
import ghidra.util.layout.PairLayout;
import ghidra_snes.options.SnesOptions;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public final class SnesOptionsDialog extends DialogComponentProvider {
  private static final String ROOT_PAGE = "/";
  private static final String MEMORY_PAGE = "/memory/";
  private static final String SYSTEM_BANKS_PAGE = "/memory/system_banks";
  private static final String ROM_MIRRORS_PAGE = "/memory/rom_mirrors";

  private final Program currentProgram;
  private final CardLayout pageLayout = new CardLayout();
  private final JPanel pagePanel = new JPanel(pageLayout);
  private final Map<GTreeNode, String> nodeToPage = new HashMap<>();

  public SnesOptionsDialog(Program currentProgram) {
    super("SNES Options", true);
    this.currentProgram = currentProgram;

    addWorkPanel(buildMainPanel());
    addDismissButton();
    setPreferredSize(980, 680);
  }

  private JComponent buildMainPanel() {
    GTree tree = buildTree();

    pagePanel.add(buildRomDetailsPage(), ROOT_PAGE);
    pagePanel.add(buildPlaceholderPage("Memory Map"), MEMORY_PAGE);
    pagePanel.add(buildPlaceholderPage("System Banks (scaffold only)"), SYSTEM_BANKS_PAGE);
    pagePanel.add(buildPlaceholderPage("ROM Mirrors (scaffold only)"), ROM_MIRRORS_PAGE);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tree, pagePanel);
    splitPane.setResizeWeight(0.0);
    splitPane.setDividerLocation(280);
    splitPane.setDividerSize(6);
    splitPane.setOneTouchExpandable(true);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(splitPane, BorderLayout.CENTER);
    return panel;
  }

  private GTree buildTree() {
    SnesOptionsTreeNode rootNode = SnesOptionsTreeNode.folder("SNES Options", null, false);
    SnesOptionsTreeNode romDetailsNode = SnesOptionsTreeNode.leaf("ROM Details", ROOT_PAGE);
    SnesOptionsTreeNode memoryMapNode = SnesOptionsTreeNode.folder("Memory Map", MEMORY_PAGE, false);
    SnesOptionsTreeNode systemBanksNode =
      SnesOptionsTreeNode.leaf("System Banks", SYSTEM_BANKS_PAGE);
    SnesOptionsTreeNode romMirrorsNode =
      SnesOptionsTreeNode.leaf("ROM Mirrors", ROM_MIRRORS_PAGE);

    rootNode.addChild(romDetailsNode);
    rootNode.addChild(memoryMapNode);
    memoryMapNode.addChild(systemBanksNode);
    memoryMapNode.addChild(romMirrorsNode);

    registerPage(romDetailsNode);
    registerPage(memoryMapNode);
    registerPage(systemBanksNode);
    registerPage(romMirrorsNode);

    GTree tree = new GTree(rootNode);
    tree.setRootVisible(false);
    tree.setShowsRootHandles(true);
    tree.setRootNodeAllowedToCollapse(false);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.expandPath(memoryMapNode);
    tree.addGTreeSelectionListener(event -> showSelectedPage(tree));
    tree.addTreeExpansionListener(createExpansionListener(tree));
    tree.setSelectedNode(romDetailsNode);

    return tree;
  }

  private TreeExpansionListener createExpansionListener(GTree tree) {
    return new TreeExpansionListener() {
      @Override
      public void treeExpanded(TreeExpansionEvent event) {
        // no-op
      }

      @Override
      public void treeCollapsed(TreeExpansionEvent event) {
        Object node = event.getPath().getLastPathComponent();
        if (!(node instanceof SnesOptionsTreeNode optionsNode)) {
          return;
        }
        if (optionsNode.isCollapsible()) {
          return;
        }

        TreePath collapsedPath = event.getPath();
        Swing.runLater(() -> tree.expandPath(collapsedPath));
      }
    };
  }

  private void registerPage(SnesOptionsTreeNode node) {
    String pageKey = node.getPageKey();
    if (pageKey != null) {
      nodeToPage.put(node, pageKey);
    }
  }

  private void showSelectedPage(GTree tree) {
    TreePath selectionPath = tree.getSelectionPath();
    if (selectionPath == null) {
      pageLayout.show(pagePanel, ROOT_PAGE);
      return;
    }

    Object selectedNode = selectionPath.getLastPathComponent();
    if (!(selectedNode instanceof GTreeNode treeNode)) {
      pageLayout.show(pagePanel, ROOT_PAGE);
      return;
    }

    String page = nodeToPage.getOrDefault(treeNode, ROOT_PAGE);
    pageLayout.show(pagePanel, page);
  }

  private JComponent buildRomDetailsPage() {
    JPanel page = new JPanel(new BorderLayout());
    page.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

    GDLabel titleLabel = new GDLabel("ROM Details");
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
    page.add(titleLabel, BorderLayout.NORTH);

    if (currentProgram == null) {
      MultiLineLabel emptyState = new MultiLineLabel("No open program.");
      emptyState.setAlignment(MultiLineLabel.LEFT);
      JPanel emptyPanel = new JPanel(new BorderLayout());
      emptyPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
      emptyPanel.add(emptyState, BorderLayout.NORTH);
      page.add(emptyPanel, BorderLayout.CENTER);
      return page;
    }

    JPanel detailsPanel = new JPanel(new PairLayout(8, 16));
    detailsPanel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createTitledBorder("Current Program"),
      BorderFactory.createEmptyBorder(10, 10, 10, 10)));

    addInfoRow(detailsPanel, "Program", currentProgram.getName());
    addInfoRow(detailsPanel, "Executable path", normalized(currentProgram.getExecutablePath()));
    addInfoRow(detailsPanel, "Mapper", SnesOptions.getCartMapper(currentProgram));
    addInfoRow(detailsPanel, "ROM title", normalized(SnesOptions.getCartTitle(currentProgram)));
    addInfoRow(detailsPanel, "ROM size", formatBytes(SnesOptions.getRomSizeBytes(currentProgram)));
    addInfoRow(detailsPanel, "SRAM size", formatBytes(SnesOptions.getCartSramSize(currentProgram)));
    addInfoRow(detailsPanel, "SMC header", String.valueOf(SnesOptions.hasSmcHeader(currentProgram)));
    addInfoRow(detailsPanel, "ROM offset", formatHex(SnesOptions.getFileRomOffset(currentProgram)));
    addInfoRow(detailsPanel, "Header location", formatHex(SnesOptions.getRomHeader(currentProgram).location()));
    addInfoRow(detailsPanel, "Metadata source", SnesOptions.getMetadataSource(currentProgram));

    JPanel content = new JPanel(new BorderLayout());
    content.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
    content.add(detailsPanel, BorderLayout.NORTH);
    page.add(content, BorderLayout.CENTER);
    return page;
  }

  private JComponent buildPlaceholderPage(String title) {
    JPanel page = new JPanel(new BorderLayout());
    page.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

    MultiLineLabel label = new MultiLineLabel(title);
    label.setAlignment(MultiLineLabel.LEFT);
    page.add(label, BorderLayout.NORTH);
    return page;
  }

  private void addInfoRow(JPanel detailsPanel, String key, String value) {
    GDLabel keyLabel = new GDLabel(key + ":");
    keyLabel.setFont(keyLabel.getFont().deriveFont(Font.BOLD));
    detailsPanel.add(keyLabel);
    detailsPanel.add(new GDLabel(value));
  }

  private static String normalized(String value) {
    if (value == null || value.isBlank()) {
      return "(not set)";
    }
    return value;
  }

  private static String formatHex(long value) {
    return String.format("0x%X", value);
  }

  private static String formatBytes(long value) {
    return String.format("%d bytes (%s)", value, formatHex(value));
  }

  private static final class SnesOptionsTreeNode extends GTreeLazyNode {
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

    static SnesOptionsTreeNode folder(String name, String pageKey, boolean collapsible) {
      return new SnesOptionsTreeNode(name, pageKey, false, collapsible);
    }

    static SnesOptionsTreeNode leaf(String name, String pageKey) {
      return new SnesOptionsTreeNode(name, pageKey, true, true);
    }

    void addChild(SnesOptionsTreeNode node) {
      children.add(node);
    }

    String getPageKey() {
      return pageKey;
    }

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
}
