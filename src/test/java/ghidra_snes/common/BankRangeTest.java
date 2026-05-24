/* (C) Arnaud 'red' Rouyer 2026 */
package ghidra_snes.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BankRangeTest {
  @Test
  @DisplayName("Computes derived values for a high-half bank range")
  void computesDerivedValues() {
    BankRange range = new BankRange(0x80, 0xbf, BankWindow.HIGH);

    assertEquals(0x80, range.startBank());
    assertEquals(0xbf, range.endBank());
    assertEquals(BankWindow.HIGH, range.window());
    assertEquals(64, range.bankCount());
    assertEquals(0x8000, range.chunkSize());
    assertEquals(0xffff, range.addressEndInclusive());
    assertEquals(0, range.fileSkip());
  }
}
