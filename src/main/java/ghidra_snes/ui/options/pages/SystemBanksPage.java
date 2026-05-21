/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.ui.options.pages;

import docking.widgets.MultiLineLabel;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public final class SystemBanksPage extends JPanel {
  /**
   * Builds the scaffold page for the System Banks leaf node.
   */
  public SystemBanksPage() {
    super(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

    MultiLineLabel label = new MultiLineLabel("System Banks (scaffold only)");
    label.setAlignment(MultiLineLabel.LEFT);
    add(label, BorderLayout.NORTH);
  }
}
