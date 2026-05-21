/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.ui.options.pages;

import docking.widgets.MultiLineLabel;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

public final class RomMirrorsPage extends JPanel {
  /**
   * Builds the scaffold page for the ROM Mirrors leaf node.
   */
  public RomMirrorsPage() {
    super(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

    MultiLineLabel label = new MultiLineLabel("ROM Mirrors (scaffold only)");
    label.setAlignment(MultiLineLabel.LEFT);
    add(label, BorderLayout.NORTH);
  }
}
